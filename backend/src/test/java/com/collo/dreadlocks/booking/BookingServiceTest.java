package com.collo.dreadlocks.booking;

import com.collo.dreadlocks.availability.SalonistAvailability;
import com.collo.dreadlocks.availability.SalonistAvailabilityRepository;
import com.collo.dreadlocks.customer.Customer;
import com.collo.dreadlocks.customer.CustomerService;
import com.collo.dreadlocks.salonist.Salonist;
import com.collo.dreadlocks.salonist.SalonistNotFoundException;
import com.collo.dreadlocks.salonist.SalonistRepository;
import com.collo.dreadlocks.service.Service;
import com.collo.dreadlocks.service.ServiceNotFoundException;
import com.collo.dreadlocks.service.ServiceRepository;
import com.collo.dreadlocks.payment.PaymentMethod;
import com.collo.dreadlocks.payment.PaymentService;
import com.collo.dreadlocks.unavailability.UnavailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Nairobi");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T08:00:00Z"), ZONE);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private SalonistRepository salonistRepository;
    @Mock
    private SalonistAvailabilityRepository availabilityRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private UnavailabilityService unavailabilityService;
    @Mock
    private PaymentService paymentService;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                serviceRepository,
                salonistRepository,
                availabilityRepository,
                customerService,
                unavailabilityService,
                org.mockito.Mockito.mock(com.collo.dreadlocks.notification.NotificationService.class),
                paymentService,
                CLOCK
        );
    }

    @Test
    void getSlotsRejectsPastDates() {
        assertThrows(
                InvalidBookingException.class,
                () -> bookingService.getSlots(1, LocalDate.of(2026, 11, 2), 1)
        );
    }

    @Test
    void getSlotsRejectsUnknownService() {
        when(serviceRepository.findByIdAndIsActiveTrue(99)).thenReturn(Optional.empty());

        assertThrows(
                ServiceNotFoundException.class,
                () -> bookingService.getSlots(99, LocalDate.of(2026, 11, 10), null)
        );
    }

    @Test
    void getSlotsReturnsWindowsMinusExistingBookings() {
        when(serviceRepository.findByIdAndIsActiveTrue(1)).thenReturn(Optional.of(service()));
        when(salonistRepository.findActiveSalonistsByServiceId(1)).thenReturn(List.of(salonist()));
        when(availabilityRepository.findBySalonistIdAndDayOfWeekAndIsActiveTrue(1, DayOfWeek.TUESDAY))
                .thenReturn(List.of(window()));

        Booking existing = new Booking();
        existing.setStartAt(LocalDateTime.of(2026, 11, 10, 9, 0));
        existing.setEndAt(LocalDateTime.of(2026, 11, 10, 12, 0));
        when(bookingRepository.findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                eq(1),
                any(),
                any(),
                any()
        )).thenReturn(List.of(existing));
        when(unavailabilityService.approvedOverlapping(eq(1), any(), any())).thenReturn(List.of());

        List<BookingSlotResponse> slots = bookingService.getSlots(1, LocalDate.of(2026, 11, 10), 1);

        assertTrue(slots.stream().noneMatch(slot -> slot.startAt().equals(LocalDateTime.of(2026, 11, 10, 9, 0))));
        assertTrue(slots.stream().anyMatch(slot -> slot.startAt().equals(LocalDateTime.of(2026, 11, 10, 12, 0))));
    }

    @Test
    void createUsesGuestCustomerWithoutAnAccount() {
        when(serviceRepository.findByIdAndIsActiveTrue(1)).thenReturn(Optional.of(service()));
        when(serviceRepository.findById(1)).thenReturn(Optional.of(service()));
        when(salonistRepository.findActiveSalonistsByServiceId(1)).thenReturn(List.of(salonist()));
        when(salonistRepository.findById(1)).thenReturn(Optional.of(salonist()));
        when(availabilityRepository.findBySalonistIdAndDayOfWeekAndIsActiveTrue(1, DayOfWeek.TUESDAY))
                .thenReturn(List.of(window()));
        when(bookingRepository.findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                eq(1), any(), any(), any()
        )).thenReturn(List.of());
        when(unavailabilityService.approvedOverlapping(eq(1), any(), any())).thenReturn(List.of());
        when(bookingRepository.lockOverlapping(eq(1), any(), any(), any())).thenReturn(List.of());
        when(customerService.findOrCreateGuest("Guest Client", "0799000001", null))
                .thenReturn(guest());
        when(customerService.requireCustomer(8)).thenReturn(guest());
        when(paymentService.findForBooking(20)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(20);
            saved.setReference(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"));
            return saved;
        });

        CreateBookingRequest request = new CreateBookingRequest(
                1,
                1,
                LocalDateTime.of(2026, 11, 10, 9, 0),
                new GuestCustomerRequest("Guest Client", "0799000001", null),
                PaymentMethod.CASH,
                null
        );

        BookingResponse response = bookingService.create(request);

        assertEquals(BookingStatus.PENDING, response.status());
        assertEquals("Guest Client", response.customerName());
        assertEquals("0799000001", response.customerPhone());
        assertEquals(20, response.id());
    }

    @Test
    void createRejectsSalonistNotAssignedToService() {
        when(serviceRepository.findByIdAndIsActiveTrue(1)).thenReturn(Optional.of(service()));
        when(salonistRepository.findActiveSalonistsByServiceId(1)).thenReturn(List.of(salonist()));

        CreateBookingRequest request = new CreateBookingRequest(
                1,
                2,
                LocalDateTime.of(2026, 11, 10, 9, 0),
                new GuestCustomerRequest("Guest Client", "0799000001", null),
                PaymentMethod.CASH,
                null
        );

        assertThrows(SalonistNotFoundException.class, () -> bookingService.create(request));
    }

    private static Service service() {
        Service service = new Service();
        service.setId(1);
        service.setName("Dreadlock Installation");
        service.setPrice(new BigDecimal("2500.00"));
        service.setDurationMinutes(180);
        service.setIsActive(true);
        return service;
    }

    private static Salonist salonist() {
        Salonist salonist = new Salonist();
        salonist.setId(1);
        salonist.setName("Collo Dreadlocks Stylist 1");
        salonist.setActive(true);
        return salonist;
    }

    private static SalonistAvailability window() {
        SalonistAvailability availability = new SalonistAvailability();
        availability.setSalonistId(1);
        availability.setDayOfWeek(DayOfWeek.TUESDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setActive(true);
        return availability;
    }

    private static Customer guest() {
        Customer customer = new Customer();
        customer.setId(8);
        customer.setName("Guest Client");
        customer.setPhone("0799000001");
        customer.setActive(true);
        return customer;
    }
}
