package com.collo.dreadlocks.booking;

import com.collo.dreadlocks.auth.ForbiddenOperationException;
import com.collo.dreadlocks.availability.SalonistAvailability;
import com.collo.dreadlocks.availability.SalonistAvailabilityRepository;
import com.collo.dreadlocks.customer.Customer;
import com.collo.dreadlocks.customer.CustomerService;
import com.collo.dreadlocks.notification.NotificationService;
import com.collo.dreadlocks.salonist.Salonist;
import com.collo.dreadlocks.salonist.SalonistNotFoundException;
import com.collo.dreadlocks.salonist.SalonistRepository;
import com.collo.dreadlocks.service.ServiceNotFoundException;
import com.collo.dreadlocks.service.ServiceRepository;
import com.collo.dreadlocks.unavailability.UnavailabilityRequest;
import com.collo.dreadlocks.unavailability.UnavailabilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {

    private static final Set<BookingStatus> OCCUPYING_STATUSES =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final SalonistRepository salonistRepository;
    private final SalonistAvailabilityRepository availabilityRepository;
    private final CustomerService customerService;
    private final UnavailabilityService unavailabilityService;
    private final NotificationService notificationService;
    private final Clock clock;

    public BookingService(
            BookingRepository bookingRepository,
            ServiceRepository serviceRepository,
            SalonistRepository salonistRepository,
            SalonistAvailabilityRepository availabilityRepository,
            CustomerService customerService,
            UnavailabilityService unavailabilityService,
            NotificationService notificationService,
            Clock clock) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.salonistRepository = salonistRepository;
        this.availabilityRepository = availabilityRepository;
        this.customerService = customerService;
        this.unavailabilityService = unavailabilityService;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    public List<BookingSlotResponse> getSlots(Integer serviceId, LocalDate date, Integer salonistId) {
        if (date == null) {
            throw new InvalidBookingException("date is required");
        }

        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today)) {
            throw new InvalidBookingException("date cannot be in the past");
        }

        com.collo.dreadlocks.service.Service service = requireActiveService(serviceId);
        List<Salonist> salonists = candidateSalonists(serviceId, salonistId);
        LocalDateTime now = LocalDateTime.now(clock);

        List<BookingSlotResponse> slots = new ArrayList<>();
        for (Salonist salonist : salonists) {
            slots.addAll(slotsForSalonist(salonist, service, date, now));
        }
        slots.sort(Comparator
                .comparing(BookingSlotResponse::startAt)
                .thenComparing(BookingSlotResponse::salonistName));
        return slots;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        LocalDateTime startAt = request.startAt();
        if (startAt == null) {
            throw new InvalidBookingException("startAt is required");
        }

        List<BookingSlotResponse> slots = getSlots(
                request.serviceId(),
                startAt.toLocalDate(),
                request.salonistId()
        );

        BookingSlotResponse selected = slots.stream()
                .filter(slot -> slot.startAt().equals(startAt))
                .findFirst()
                .orElseThrow(() -> new InvalidBookingException(
                        "The selected time is not available"
                ));

        LocalDateTime endAt = selected.endAt();
        List<Booking> overlapping = bookingRepository.lockOverlapping(
                selected.salonistId(),
                OCCUPYING_STATUSES,
                startAt,
                endAt
        );
        if (!overlapping.isEmpty()
                || !unavailabilityService.approvedOverlapping(
                selected.salonistId(),
                startAt,
                endAt
        ).isEmpty()) {
            throw new BookingConflictException("This appointment slot is no longer available");
        }

        Customer customer = customerService.findOrCreateGuest(
                request.customer().name(),
                request.customer().phone(),
                request.customer().email()
        );

        com.collo.dreadlocks.service.Service service = requireActiveService(request.serviceId());

        Booking booking = new Booking();
        booking.setCustomerId(customer.getId());
        booking.setServiceId(service.getId());
        booking.setSalonistId(selected.salonistId());
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPrice(service.getPrice());
        booking.setDurationMinutes(service.getDurationMinutes());

        BookingResponse response = toResponse(bookingRepository.save(booking));
        notificationService.notifyNewBooking(response);
        return response;
    }

    public BookingResponse getByReference(UUID reference) {
        return toResponse(bookingRepository.findByReference(reference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + reference)));
    }

    public BookingResponse getById(Integer id) {
        return toResponse(requireBooking(id));
    }

    public List<BookingResponse> search(BookingStatus status, LocalDate date) {
        LocalDateTime from = date == null ? null : date.atStartOfDay();
        LocalDateTime to = date == null ? null : date.plusDays(1).atStartOfDay();
        List<Booking> bookings;
        if (status == null && from == null) {
            bookings = bookingRepository.findAllByOrderByStartAtAsc();
        } else if (status == null) {
            bookings = bookingRepository.findByStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(from, to);
        } else if (from == null) {
            bookings = bookingRepository.findByStatusOrderByStartAtAsc(status);
        } else {
            bookings = bookingRepository.findByStatusAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
                    status, from, to);
        }
        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<BookingResponse> getByCustomer(Integer customerId) {
        customerService.getCustomer(customerId);
        return bookingRepository.findByCustomerIdOrderByStartAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<BookingResponse> getBySalonist(Integer salonistId, LocalDate date) {
        return bookingRepository.findBySalonistIdOrderByStartAtDesc(salonistId)
                .stream()
                .filter(booking -> date == null || booking.getStartAt().toLocalDate().equals(date))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse completeForSalonist(Integer bookingId, Integer salonistId) {
        Booking booking = requireBooking(bookingId);
        if (!booking.getSalonistId().equals(salonistId)) {
            throw new ForbiddenOperationException("This booking does not belong to the salonist");
        }
        return transition(bookingId, BookingStatus.COMPLETED);
    }

    @Transactional
    public BookingResponse confirm(Integer id) {
        return transition(id, BookingStatus.CONFIRMED);
    }

    @Transactional
    public BookingResponse cancel(Integer id) {
        return transition(id, BookingStatus.CANCELLED);
    }

    @Transactional
    public BookingResponse complete(Integer id) {
        return transition(id, BookingStatus.COMPLETED);
    }

    @Transactional
    public BookingResponse markNoShow(Integer id) {
        return transition(id, BookingStatus.NO_SHOW);
    }

    private BookingResponse transition(Integer id, BookingStatus target) {
        Booking booking = requireBooking(id);
        if (target == BookingStatus.CONFIRMED) {
            ensureSlotFree(booking);
        }
        if (!BookingStatusTransitions.canTransition(booking.getStatus(), target)) {
            throw new InvalidBookingException(
                    "Cannot change booking from " + booking.getStatus() + " to " + target
            );
        }
        booking.setStatus(target);
        BookingResponse response = toResponse(bookingRepository.save(booking));
        if (target == BookingStatus.CONFIRMED) {
            notificationService.notifyBookingConfirmed(response);
        }
        return response;
    }

    private void ensureSlotFree(Booking booking) {
        boolean taken = bookingRepository.lockOverlapping(
                booking.getSalonistId(),
                OCCUPYING_STATUSES,
                booking.getStartAt(),
                booking.getEndAt()
        ).stream().anyMatch(existing -> !existing.getId().equals(booking.getId()));
        boolean off = !unavailabilityService.approvedOverlapping(
                booking.getSalonistId(),
                booking.getStartAt(),
                booking.getEndAt()
        ).isEmpty();
        if (taken || off) {
            throw new BookingConflictException("This salonist is already booked or off at that time");
        }
    }

    private List<BookingSlotResponse> slotsForSalonist(
            Salonist salonist,
            com.collo.dreadlocks.service.Service service,
            LocalDate date,
            LocalDateTime now) {
        List<SalonistAvailability> windows = availabilityRepository
                .findBySalonistIdAndDayOfWeekAndIsActiveTrue(salonist.getId(), date.getDayOfWeek());
        if (windows.isEmpty()) {
            return List.of();
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Booking> occupying = bookingRepository
                .findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        salonist.getId(),
                        OCCUPYING_STATUSES,
                        dayEnd,
                        dayStart
                );
        List<UnavailabilityRequest> blocked = unavailabilityService.approvedOverlapping(
                salonist.getId(),
                dayStart,
                dayEnd
        );

        LocalTime notBefore = date.equals(now.toLocalDate()) ? now.toLocalTime() : null;
        List<BookingSlotResponse> slots = new ArrayList<>();
        for (SalonistAvailability window : windows) {
            for (LocalTime start : BookingSlots.starts(
                    window.getStartTime(),
                    window.getEndTime(),
                    service.getDurationMinutes(),
                    notBefore
            )) {
                LocalDateTime startAt = date.atTime(start);
                LocalDateTime endAt = startAt.plusMinutes(service.getDurationMinutes());
                boolean taken = occupying.stream().anyMatch(existing ->
                        BookingSlots.overlaps(startAt, endAt, existing.getStartAt(), existing.getEndAt()));
                boolean unavailable = blocked.stream().anyMatch(existing ->
                        BookingSlots.overlaps(startAt, endAt, existing.getStartAt(), existing.getEndAt()));
                if (!taken && !unavailable) {
                    slots.add(new BookingSlotResponse(
                            salonist.getId(),
                            salonist.getName(),
                            startAt,
                            endAt
                    ));
                }
            }
        }
        return slots;
    }

    private List<Salonist> candidateSalonists(Integer serviceId, Integer salonistId) {
        List<Salonist> assigned = salonistRepository.findActiveSalonistsByServiceId(serviceId);
        if (salonistId == null) {
            return assigned;
        }
        return assigned.stream()
                .filter(salonist -> salonist.getId().equals(salonistId))
                .findFirst()
                .map(List::of)
                .orElseThrow(() -> new SalonistNotFoundException(salonistId));
    }

    private com.collo.dreadlocks.service.Service requireActiveService(Integer serviceId) {
        return serviceRepository.findByIdAndIsActiveTrue(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + serviceId));
    }

    private Booking requireBooking(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + id));
    }

    private BookingResponse toResponse(Booking booking) {
        Customer customer = customerService.requireCustomer(booking.getCustomerId());
        com.collo.dreadlocks.service.Service service = serviceRepository.findById(booking.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + booking.getServiceId()));
        Salonist salonist = salonistRepository.findById(booking.getSalonistId())
                .orElseThrow(() -> new SalonistNotFoundException(booking.getSalonistId()));

        return new BookingResponse(
                booking.getId(),
                booking.getReference(),
                booking.getStatus(),
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                service.getId(),
                service.getName(),
                salonist.getId(),
                salonist.getName(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getDurationMinutes(),
                booking.getPrice()
        );
    }
}
