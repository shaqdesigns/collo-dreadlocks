package com.collo.dreadlocks.booking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/slots")
    public ResponseEntity<List<BookingSlotResponse>> getSlots(
            @RequestParam Integer serviceId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Integer salonistId) {
        return ResponseEntity.ok(bookingService.getSlots(serviceId, date, salonistId));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(request));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<BookingResponse> getByReference(@PathVariable UUID reference) {
        return ResponseEntity.ok(bookingService.getByReference(reference));
    }
}
