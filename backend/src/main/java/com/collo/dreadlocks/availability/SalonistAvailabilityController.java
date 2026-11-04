package com.collo.dreadlocks.availability;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salonists/{salonistId}/availability")
public class SalonistAvailabilityController {

    private final AvailabilityService availabilityService;

    public SalonistAvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponse>> getAvailability(
            @PathVariable Integer salonistId) {
        return ResponseEntity.ok(
                availabilityService.getActiveAvailability(salonistId)
        );
    }
}
