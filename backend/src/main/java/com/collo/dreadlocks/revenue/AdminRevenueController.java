package com.collo.dreadlocks.revenue;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/revenue")
public class AdminRevenueController {

    private final RevenueService revenueService;

    public AdminRevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping
    public ResponseEntity<RevenueReportResponse> report() {
        return ResponseEntity.ok(revenueService.report());
    }

    @GetMapping("/manual-works")
    public ResponseEntity<List<ManualWorkResponse>> manualWorks() {
        return ResponseEntity.ok(revenueService.listManual());
    }

    @PostMapping("/manual-works")
    public ResponseEntity<ManualWorkResponse> record(@Valid @RequestBody ManualWorkRequest request) {
        if (request.salonistId() == null) {
            throw new InvalidManualWorkException("salonistId is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(revenueService.record(request, request.salonistId()));
    }
}
