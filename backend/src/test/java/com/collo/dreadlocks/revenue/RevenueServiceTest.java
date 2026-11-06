package com.collo.dreadlocks.revenue;

import com.collo.dreadlocks.booking.Booking;
import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.booking.BookingStatus;
import com.collo.dreadlocks.salonist.Salonist;
import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.service.Service;
import com.collo.dreadlocks.service.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ManualWorkRepository manualWorkRepository;
    @Mock
    private SalonistCatalogService salonistCatalogService;
    @Mock
    private ServiceRepository serviceRepository;

    private RevenueService revenueService;

    @BeforeEach
    void setUp() {
        revenueService = new RevenueService(
                bookingRepository,
                manualWorkRepository,
                salonistCatalogService,
                serviceRepository
        );
    }

    @Test
    void reportPaysSalonistsFortyPercentAndRanksServices() {
        when(bookingRepository.findByStatusIn(any())).thenReturn(List.of(
                booking(1, BookingStatus.COMPLETED, new BigDecimal("1000.00")),
                booking(1, BookingStatus.COMPLETED, new BigDecimal("1000.00")),
                booking(2, BookingStatus.PENDING, new BigDecimal("2500.00")),
                booking(2, BookingStatus.PENDING, new BigDecimal("2500.00")),
                booking(2, BookingStatus.PENDING, new BigDecimal("2500.00"))
        ));
        when(manualWorkRepository.findAllByOrderByWorkedAtDesc()).thenReturn(List.of());
        when(serviceRepository.findById(1)).thenReturn(Optional.of(service(1, "Retouch")));
        when(serviceRepository.findById(2)).thenReturn(Optional.of(service(2, "Installation")));
        when(salonistCatalogService.requireSalonistEntity(4)).thenReturn(salonist());

        RevenueReportResponse report = revenueService.report();

        assertEquals(new BigDecimal("2000.00"), report.totalRevenue());
        assertEquals(new BigDecimal("800.00"), report.salonistPayout());
        assertEquals(new BigDecimal("1200.00"), report.studioShare());
        assertEquals("Installation", report.mostSought().serviceName());
        assertEquals(3, report.mostSought().soughtCount());
        assertEquals("Retouch", report.highestRevenue().serviceName());
        assertEquals(new BigDecimal("800.00"), report.salonists().getFirst().payout());
    }

    private Booking booking(int serviceId, BookingStatus status, BigDecimal price) {
        Booking booking = new Booking();
        booking.setServiceId(serviceId);
        booking.setSalonistId(4);
        booking.setStatus(status);
        booking.setPrice(price);
        return booking;
    }

    private Service service(int id, String name) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        return service;
    }

    private Salonist salonist() {
        Salonist salonist = new Salonist();
        salonist.setId(4);
        salonist.setName("Collo");
        return salonist;
    }
}
