package com.collo.dreadlocks.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> search(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(bookingService.search(status, date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirm(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.cancel(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> complete(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.complete(id));
    }

    @PostMapping("/{id}/no-show")
    public ResponseEntity<BookingResponse> markNoShow(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.markNoShow(id));
    }
}
