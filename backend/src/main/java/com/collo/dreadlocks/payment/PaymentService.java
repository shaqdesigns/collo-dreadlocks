package com.collo.dreadlocks.payment;

import com.collo.dreadlocks.booking.Booking;
import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.booking.BookingStatus;
import com.collo.dreadlocks.booking.InvalidBookingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final DarajaClient darajaClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            DarajaClient darajaClient) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.darajaClient = darajaClient;
    }

    @Transactional
    public Payment open(Booking booking, PaymentMethod method, String mpesaPhone) {
        if (method == null) {
            throw new InvalidBookingException("Choose M-Pesa or cash");
        }
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.AWAITING);
        payment.setAmount(booking.getPrice());
        if (method == PaymentMethod.MPESA) {
            String phone = MpesaPhone.normalize(mpesaPhone);
            payment.setPhone(phone);
            paymentRepository.save(payment);
            StkPushResult prompt = darajaClient.push(
                    phone,
                    booking.getPrice(),
                    "B" + booking.getId(),
                    "Collo salon"
            );
            payment.setMerchantRequestId(prompt.merchantRequestId());
            payment.setCheckoutRequestId(prompt.checkoutRequestId());
            payment.setResultDescription(prompt.customerMessage());
            return paymentRepository.save(payment);
        }
        payment.setResultDescription("Cash is collected at the studio on the day of the visit.");
        return paymentRepository.save(payment);
    }

    public Optional<Payment> findForBooking(Integer bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public void assertApprovable(Integer bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId).orElse(null);
        if (payment == null) {
            return;
        }
        boolean cashReady = payment.getMethod() == PaymentMethod.CASH
                && payment.getStatus() == PaymentStatus.AWAITING;
        boolean mpesaReady = payment.getMethod() == PaymentMethod.MPESA
                && payment.getStatus() == PaymentStatus.PAID;
        if (cashReady || mpesaReady) {
            return;
        }
        if (payment.getMethod() == PaymentMethod.MPESA && payment.getStatus() == PaymentStatus.AWAITING) {
            throw new InvalidBookingException("The client has not entered their M-Pesa PIN yet");
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new InvalidBookingException("This M-Pesa payment failed, so the visit cannot be sent to the salonist");
        }
        throw new InvalidBookingException("This payment cannot be approved");
    }

    @Transactional
    public void markApproved(Integer bookingId) {
        paymentRepository.findByBookingId(bookingId).ifPresent(payment -> {
            if (payment.getMethod() == PaymentMethod.CASH) {
                payment.setResultDescription("Confirmed. The client pays cash at the studio on the day of the visit.");
                paymentRepository.save(payment);
                return;
            }
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setApprovedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        });
    }

    public boolean visibleToSalonist(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING) {
            return false;
        }
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            return true;
        }
        return paymentRepository.findByBookingId(booking.getId())
                .map(payment -> payment.getStatus() == PaymentStatus.APPROVED)
                .orElse(true);
    }

    @Transactional
    public MpesaCallbackOutcome applyCallback(JsonNode body) {
        MpesaCallbackOutcome outcome = MpesaCallbackParser.parse(body);
        if (outcome.checkoutRequestId() == null) {
            return outcome;
        }
        Payment payment = paymentRepository.findByCheckoutRequestId(outcome.checkoutRequestId()).orElse(null);
        if (payment == null
                || payment.getStatus() == PaymentStatus.APPROVED
                || payment.getStatus() == PaymentStatus.PAID
                || payment.getStatus() == PaymentStatus.FAILED) {
            return MpesaCallbackOutcome.ignored();
        }
        payment.setResultDescription(trim(outcome.description()));
        if (outcome.paid()) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setMpesaReceipt(outcome.receipt());
            paymentRepository.save(payment);
            return outcome.withBooking(payment.getBookingId());
        }
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        bookingRepository.findById(payment.getBookingId()).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
            }
        });
        return outcome.withBooking(payment.getBookingId());
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 255 ? value : value.substring(0, 255);
    }
}
