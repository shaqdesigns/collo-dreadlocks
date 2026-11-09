package com.collo.dreadlocks.booking;

import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.salonist.SalonistResponse;
import com.collo.dreadlocks.unavailability.UnavailabilityResponse;
import com.collo.dreadlocks.unavailability.UnavailabilityService;
import com.collo.dreadlocks.unavailability.UnavailabilityStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {

    private final BookingService bookingService;
    private final UnavailabilityService unavailabilityService;
    private final SalonistCatalogService salonistCatalogService;

    public CalendarService(
            BookingService bookingService,
            UnavailabilityService unavailabilityService,
            SalonistCatalogService salonistCatalogService) {
        this.bookingService = bookingService;
        this.unavailabilityService = unavailabilityService;
        this.salonistCatalogService = salonistCatalogService;
    }

    public CalendarDayResponse day(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.plusDays(1).atStartOfDay();

        List<BookingResponse> bookings = bookingService.search(null, day).stream()
                .filter(booking -> booking.status() == BookingStatus.PENDING
                        || booking.status() == BookingStatus.CONFIRMED
                        || booking.status() == BookingStatus.COMPLETED)
                .toList();

        List<UnavailabilityResponse> timeOff = unavailabilityService.listForAdmin(UnavailabilityStatus.APPROVED)
                .stream()
                .filter(request -> request.startAt().isBefore(to) && from.isBefore(request.endAt()))
                .toList();

        List<CalendarColumnResponse> columns = new ArrayList<>();
        for (SalonistResponse salonist : salonistCatalogService.listAll()) {
            List<CalendarBlockResponse> blocks = new ArrayList<>();
            bookings.stream()
                    .filter(booking -> booking.salonistId().equals(salonist.id()))
                    .forEach(booking -> blocks.add(new CalendarBlockResponse(
                            "BOOKING",
                            booking.status().name(),
                            booking.startAt(),
                            booking.endAt(),
                            booking.serviceName() + " · " + booking.customerName()
                                    + (booking.paymentMethod() == null
                                    ? ""
                                    : " · " + booking.paymentMethod() + " " + booking.paymentStatus()),
                            booking.id()
                    )));
            timeOff.stream()
                    .filter(request -> request.salonistId().equals(salonist.id()))
                    .forEach(request -> blocks.add(new CalendarBlockResponse(
                            "OFF",
                            request.status().name(),
                            request.startAt(),
                            request.endAt(),
                            request.reason() == null || request.reason().isBlank()
                                    ? "Time off"
                                    : "Off · " + request.reason(),
                            null
                    )));
            columns.add(new CalendarColumnResponse(
                    salonist.id(),
                    salonist.name(),
                    salonist.isActive(),
                    blocks
            ));
        }
        return new CalendarDayResponse(day, columns);
    }
}
