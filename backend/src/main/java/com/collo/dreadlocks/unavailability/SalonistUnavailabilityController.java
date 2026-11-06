package com.collo.dreadlocks.unavailability;

import com.collo.dreadlocks.auth.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salonist/unavailability-requests")
public class SalonistUnavailabilityController {

    private final UnavailabilityService unavailabilityService;

    public SalonistUnavailabilityController(UnavailabilityService unavailabilityService) {
        this.unavailabilityService = unavailabilityService;
    }

    @GetMapping
    public ResponseEntity<List<UnavailabilityResponse>> list(
            @AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(unavailabilityService.listForSalonist(current.getSalonistId()));
    }

    @PostMapping
    public ResponseEntity<UnavailabilityResponse> create(
            @AuthenticationPrincipal AuthUser current,
            @Valid @RequestBody UnavailabilityCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unavailabilityService.create(current.getSalonistId(), request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<UnavailabilityResponse> cancel(
            @AuthenticationPrincipal AuthUser current,
            @PathVariable Integer id) {
        return ResponseEntity.ok(unavailabilityService.cancel(current.getSalonistId(), id));
    }
}
