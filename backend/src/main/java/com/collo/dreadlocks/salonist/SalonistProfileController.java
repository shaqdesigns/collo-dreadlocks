package com.collo.dreadlocks.salonist;

import com.collo.dreadlocks.auth.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/salonist/profile")
public class SalonistProfileController {

    private final SalonistCatalogService salonistCatalogService;

    public SalonistProfileController(SalonistCatalogService salonistCatalogService) {
        this.salonistCatalogService = salonistCatalogService;
    }

    @GetMapping
    public ResponseEntity<SalonistResponse> profile(@AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(salonistCatalogService.requireSalonist(current.getSalonistId()));
    }
}
