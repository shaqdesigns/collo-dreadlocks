package com.collo.dreadlocks.remark;

import com.collo.dreadlocks.auth.ForbiddenOperationException;
import com.collo.dreadlocks.booking.Booking;
import com.collo.dreadlocks.booking.BookingNotFoundException;
import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.booking.BookingStatus;
import com.collo.dreadlocks.customer.CustomerService;
import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.service.ServiceNotFoundException;
import com.collo.dreadlocks.service.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RemarkService {

    private final BookingRemarkRepository remarkRepository;
    private final BookingRepository bookingRepository;
    private final CustomerService customerService;
    private final SalonistCatalogService salonistCatalogService;
    private final ServiceRepository serviceRepository;

    public RemarkService(
            BookingRemarkRepository remarkRepository,
            BookingRepository bookingRepository,
            CustomerService customerService,
            SalonistCatalogService salonistCatalogService,
            ServiceRepository serviceRepository) {
        this.remarkRepository = remarkRepository;
        this.bookingRepository = bookingRepository;
        this.customerService = customerService;
        this.salonistCatalogService = salonistCatalogService;
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public RemarkResponse create(Integer customerId, Integer bookingId, RemarkCreateRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));
        if (!booking.getCustomerId().equals(customerId)) {
            throw new ForbiddenOperationException("This booking does not belong to the client");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidRemarkException("A remark can be left after the appointment is completed");
        }
        if (remarkRepository.existsByBookingId(bookingId)) {
            throw new RemarkConflictException("A remark has already been left for this appointment");
        }

        BookingRemark remark = new BookingRemark();
        remark.setBookingId(bookingId);
        remark.setCustomerId(customerId);
        remark.setSalonistId(booking.getSalonistId());
        remark.setBody(request.body().trim());
        return toResponse(remarkRepository.save(remark), booking);
    }

    public List<RemarkResponse> listForAdmin() {
        return remarkRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RemarkResponse> listForSalonist(Integer salonistId) {
        return remarkRepository.findBySalonistIdOrderByCreatedAtDesc(salonistId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RemarkResponse> listForCustomer(Integer customerId) {
        return remarkRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RemarkResponse toResponse(BookingRemark remark) {
        Booking booking = bookingRepository.findById(remark.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + remark.getBookingId()));
        return toResponse(remark, booking);
    }

    private RemarkResponse toResponse(BookingRemark remark, Booking booking) {
        var customer = customerService.requireCustomer(remark.getCustomerId());
        var salonist = salonistCatalogService.requireSalonistEntity(remark.getSalonistId());
        var service = serviceRepository.findById(booking.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + booking.getServiceId()));
        return new RemarkResponse(
                remark.getId(),
                remark.getBookingId(),
                customer.getId(),
                customer.getName(),
                salonist.getId(),
                salonist.getName(),
                service.getId(),
                service.getName(),
                booking.getStartAt(),
                remark.getBody(),
                remark.getCreatedAt()
        );
    }
}
