package com.collo.dreadlocks.availability;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/salonists/{salonistId}/availability")
public class AdminAvailabilityController {

    private final AvailabilityService availabilityService;

    public AdminAvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponse>> getAvailability(
            @PathVariable Integer salonistId) {
        return ResponseEntity.ok(
                availabilityService.getAvailabilityForAdmin(salonistId)
        );
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponse> create(
            @PathVariable Integer salonistId,
            @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.create(salonistId, request));
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponse> update(
            @PathVariable Integer salonistId,
            @PathVariable Integer availabilityId,
            @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(
                availabilityService.update(salonistId, availabilityId, request)
        );
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponse> deactivate(
            @PathVariable Integer salonistId,
            @PathVariable Integer availabilityId) {
        return ResponseEntity.ok(
                availabilityService.deactivate(salonistId, availabilityId)
        );
    }
}
