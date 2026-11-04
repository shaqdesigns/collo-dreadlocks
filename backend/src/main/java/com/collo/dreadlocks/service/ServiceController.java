
package com.collo.dreadlocks.service;

import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.salonist.SalonistResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;
    private final SalonistCatalogService salonistCatalogService;

    public ServiceController(
            ServiceCatalogService serviceCatalogService,
            SalonistCatalogService salonistCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
        this.salonistCatalogService = salonistCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getServices() {
        return ResponseEntity.ok(
                serviceCatalogService.getActiveServices()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getService(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                serviceCatalogService.getActiveService(id)
        );
    }

    @GetMapping("/{id}/salonists")
    public ResponseEntity<List<SalonistResponse>> getSalonistsForService(
            @PathVariable Integer id) {
        // Validate that the service exists and is active.
        serviceCatalogService.getActiveService(id);

        return ResponseEntity.ok(
                salonistCatalogService.getSalonistsForService(id)
        );
    }
}
