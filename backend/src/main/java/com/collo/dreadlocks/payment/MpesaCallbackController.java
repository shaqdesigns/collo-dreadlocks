package com.collo.dreadlocks.payment;

import com.collo.dreadlocks.booking.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/mpesa")
public class MpesaCallbackController {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MpesaCallbackController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestBody String body) {
        try {
            bookingService.handleMpesaCallback(objectMapper.readTree(body));
        } catch (com.fasterxml.jackson.core.JacksonException exception) {
            return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
        }
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }
}
