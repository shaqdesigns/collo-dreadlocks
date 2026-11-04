package com.collo.dreadlocks.salonist;

import com.collo.dreadlocks.service.ServiceCatalogService;
import com.collo.dreadlocks.service.ServiceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salonists")
public class SalonistController {

    private final SalonistCatalogService salonistCatalogService;
    private final ServiceCatalogService serviceCatalogService;

    public SalonistController(
            SalonistCatalogService salonistCatalogService,
            ServiceCatalogService serviceCatalogService) {

        this.salonistCatalogService = salonistCatalogService;
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<SalonistResponse>> getSalonists() {
        return ResponseEntity.ok(
                salonistCatalogService.getActiveSalonists()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalonistResponse> getSalonist(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                salonistCatalogService.getActiveSalonist(id)
        );
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceResponse>> getServicesForSalonist(
            @PathVariable Integer id) {

        // Confirm the salonist exists and is active.
        salonistCatalogService.getActiveSalonist(id);

        return ResponseEntity.ok(
                serviceCatalogService.getServicesForSalonist(id)
        );
    }
}