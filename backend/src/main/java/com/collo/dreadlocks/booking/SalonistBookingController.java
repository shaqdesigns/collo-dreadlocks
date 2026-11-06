package com.collo.dreadlocks.booking;

import com.collo.dreadlocks.auth.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/salonist/bookings")
public class SalonistBookingController {

    private final BookingService bookingService;

    public SalonistBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> list(
            @AuthenticationPrincipal AuthUser current,
            @RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(bookingService.getBySalonist(current.getSalonistId(), date));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> complete(
            @AuthenticationPrincipal AuthUser current,
            @PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.completeForSalonist(id, current.getSalonistId()));
    }
}
