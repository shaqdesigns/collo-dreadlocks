package com.collo.dreadlocks.unavailability;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/unavailability-requests")
public class AdminUnavailabilityController {

    private final UnavailabilityService unavailabilityService;

    public AdminUnavailabilityController(UnavailabilityService unavailabilityService) {
        this.unavailabilityService = unavailabilityService;
    }

    @GetMapping
    public ResponseEntity<List<UnavailabilityResponse>> list(
            @RequestParam(required = false) UnavailabilityStatus status) {
        return ResponseEntity.ok(unavailabilityService.listForAdmin(status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<UnavailabilityResponse> approve(
            @PathVariable Integer id,
            @RequestBody(required = false) UnavailabilityReviewRequest review) {
        return ResponseEntity.ok(unavailabilityService.approve(
                id,
                review == null ? new UnavailabilityReviewRequest(null) : review
        ));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<UnavailabilityResponse> reject(
            @PathVariable Integer id,
            @RequestBody(required = false) UnavailabilityReviewRequest review) {
        return ResponseEntity.ok(unavailabilityService.reject(
                id,
                review == null ? new UnavailabilityReviewRequest(null) : review
        ));
    }
}
