package com.collo.dreadlocks.notification;

import com.collo.dreadlocks.auth.AuthProperties;
import com.collo.dreadlocks.booking.BookingResponse;
import com.collo.dreadlocks.customer.CustomerRepository;
import com.collo.dreadlocks.auth.UserRepository;
import com.collo.dreadlocks.salonist.SalonistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM, HH:mm");

    private final NotificationRepository notificationRepository;
    private final BookingMailer bookingMailer;
    private final AuthProperties authProperties;
    private final CustomerRepository customerRepository;
    private final SalonistRepository salonistRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            BookingMailer bookingMailer,
            AuthProperties authProperties,
            CustomerRepository customerRepository,
            SalonistRepository salonistRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.bookingMailer = bookingMailer;
        this.authProperties = authProperties;
        this.customerRepository = customerRepository;
        this.salonistRepository = salonistRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyNewBooking(BookingResponse booking) {
        StudioNotification notification = new StudioNotification();
        notification.setAudience(NotificationAudience.ADMIN);
        notification.setBookingId(booking.id());
        String message = "New booking: " + booking.serviceName()
                + " with " + booking.salonistName()
                + " for " + booking.customerName()
                + " at " + booking.startAt().format(WHEN);
        notification.setMessage(message);
        notificationRepository.save(notification);
        bookingMailer.send(authProperties.getAdmin().getEmail(), "New booking at Collo Dreadlocks", message);

        String chairMessage = "New request: " + booking.serviceName()
                + " for " + booking.customerName()
                + " at " + booking.startAt().format(WHEN);
        saveForSalonist(booking, chairMessage);
        for (String email : salonistEmails(booking.salonistId())) {
            bookingMailer.send(email, "New booking request at Collo Dreadlocks", chairMessage);
        }

        String clientMessage = "Hello " + booking.customerName()
                + ", we received your request for " + booking.serviceName()
                + " with " + booking.salonistName()
                + " at " + booking.startAt().format(WHEN)
                + ". The studio will confirm it shortly.";
        saveForCustomer(booking, clientMessage);
        for (String email : customerEmails(booking.customerId())) {
            bookingMailer.send(email, "Your booking request at Collo Dreadlocks", clientMessage);
        }
    }

    @Transactional
    public void notifyBookingConfirmed(BookingResponse booking) {
        String message = "Confirmed: " + booking.serviceName()
                + " for " + booking.customerName()
                + " at " + booking.startAt().format(WHEN);
        saveForSalonist(booking, message);
        for (String email : salonistEmails(booking.salonistId())) {
            bookingMailer.send(email, "Booking confirmed at Collo Dreadlocks", message);
        }
        String clientMessage = "Hello " + booking.customerName()
                + ", your " + booking.serviceName()
                + " with " + booking.salonistName()
                + " at " + booking.startAt().format(WHEN)
                + " is confirmed.";
        saveForCustomer(booking, clientMessage);
        for (String email : customerEmails(booking.customerId())) {
            bookingMailer.send(email, "Your visit is confirmed at Collo Dreadlocks", clientMessage);
        }
    }

    public List<NotificationResponse> listForAdmin() {
        return notificationRepository
                .findByAudienceAndSalonistIdIsNullOrderByCreatedAtDesc(NotificationAudience.ADMIN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> listForSalonist(Integer salonistId) {
        return notificationRepository
                .findByAudienceAndSalonistIdOrderByCreatedAtDesc(NotificationAudience.SALONIST, salonistId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> listForCustomer(Integer customerId) {
        return notificationRepository
                .findByAudienceAndCustomerIdOrderByCreatedAtDesc(NotificationAudience.CUSTOMER, customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markReadForAdmin(Integer id) {
        StudioNotification notification = notificationRepository
                .findByIdAndAudienceAndSalonistIdIsNull(id, NotificationAudience.ADMIN)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        return markRead(notification);
    }

    @Transactional
    public NotificationResponse markReadForSalonist(Integer id, Integer salonistId) {
        StudioNotification notification = notificationRepository
                .findByIdAndAudienceAndSalonistId(id, NotificationAudience.SALONIST, salonistId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        return markRead(notification);
    }

    @Transactional
    public void markAllReadForAdmin() {
        notificationRepository
                .findByAudienceAndSalonistIdIsNullOrderByCreatedAtDesc(NotificationAudience.ADMIN)
                .forEach(this::markRead);
    }

    @Transactional
    public void markAllReadForSalonist(Integer salonistId) {
        notificationRepository
                .findByAudienceAndSalonistIdOrderByCreatedAtDesc(NotificationAudience.SALONIST, salonistId)
                .forEach(this::markRead);
    }

    @Transactional
    public NotificationResponse markReadForCustomer(Integer id, Integer customerId) {
        StudioNotification notification = notificationRepository
                .findByIdAndAudienceAndCustomerId(id, NotificationAudience.CUSTOMER, customerId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        return markRead(notification);
    }

    @Transactional
    public void markAllReadForCustomer(Integer customerId) {
        notificationRepository
                .findByAudienceAndCustomerIdOrderByCreatedAtDesc(NotificationAudience.CUSTOMER, customerId)
                .forEach(this::markRead);
    }

    private void saveForSalonist(BookingResponse booking, String message) {
        StudioNotification notification = new StudioNotification();
        notification.setAudience(NotificationAudience.SALONIST);
        notification.setSalonistId(booking.salonistId());
        notification.setBookingId(booking.id());
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    private void saveForCustomer(BookingResponse booking, String message) {
        StudioNotification notification = new StudioNotification();
        notification.setAudience(NotificationAudience.CUSTOMER);
        notification.setCustomerId(booking.customerId());
        notification.setBookingId(booking.id());
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    private NotificationResponse markRead(StudioNotification notification) {
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    private Set<String> customerEmails(Integer customerId) {
        Set<String> emails = new LinkedHashSet<>();
        customerRepository.findById(customerId)
                .map(customer -> customer.getEmail())
                .ifPresent(emails::add);
        userRepository.findByCustomerId(customerId)
                .map(user -> user.getEmail())
                .ifPresent(emails::add);
        return emails;
    }

    private Set<String> salonistEmails(Integer salonistId) {
        Set<String> emails = new LinkedHashSet<>();
        salonistRepository.findById(salonistId)
                .map(salonist -> salonist.getEmail())
                .ifPresent(emails::add);
        userRepository.findBySalonistId(salonistId)
                .map(user -> user.getEmail())
                .ifPresent(emails::add);
        return emails;
    }

    private NotificationResponse toResponse(StudioNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getBookingId(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
