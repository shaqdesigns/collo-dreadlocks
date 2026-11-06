package com.collo.dreadlocks.auth;

import com.collo.dreadlocks.booking.BookingResponse;
import com.collo.dreadlocks.booking.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final BookingService bookingService;

    public MeController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> myBookings(
            @AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(bookingService.getByCustomer(current.getCustomerId()));
    }
}
