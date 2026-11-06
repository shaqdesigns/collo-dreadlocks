package com.collo.dreadlocks.revenue;

import com.collo.dreadlocks.auth.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salonist/revenue")
public class SalonistRevenueController {

    private final RevenueService revenueService;

    public SalonistRevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping
    public ResponseEntity<SalonistEarningsResponse> earnings(@AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(revenueService.earningsFor(current.getSalonistId()));
    }

    @GetMapping("/manual-works")
    public ResponseEntity<List<ManualWorkResponse>> manualWorks(@AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(revenueService.listManualForSalonist(current.getSalonistId()));
    }

    @PostMapping("/manual-works")
    public ResponseEntity<ManualWorkResponse> record(
            @AuthenticationPrincipal AuthUser current,
            @Valid @RequestBody ManualWorkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(revenueService.record(request, current.getSalonistId()));
    }
}
