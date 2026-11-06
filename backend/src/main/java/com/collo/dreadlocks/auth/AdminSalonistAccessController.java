package com.collo.dreadlocks.auth;

import com.collo.dreadlocks.salonist.AdminSalonistResponse;
import com.collo.dreadlocks.salonist.CreateSalonistRequest;
import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.salonist.SalonistResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/salonists")
public class AdminSalonistAccessController {

    private final AuthService authService;
    private final SalonistCatalogService salonistCatalogService;

    public AdminSalonistAccessController(
            AuthService authService,
            SalonistCatalogService salonistCatalogService) {
        this.authService = authService;
        this.salonistCatalogService = salonistCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<AdminSalonistResponse>> list() {
        return ResponseEntity.ok(authService.listSalonists());
    }

    @PostMapping
    public ResponseEntity<AdminSalonistResponse> create(@Valid @RequestBody CreateSalonistRequest request) {
        SalonistResponse created = salonistCatalogService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AdminSalonistResponse(
                created.id(),
                created.name(),
                created.phone(),
                created.email(),
                created.bio(),
                created.isActive(),
                false
        ));
    }

    @PostMapping(value = "/{salonistId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SalonistResponse> uploadPhoto(
            @PathVariable Integer salonistId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(salonistCatalogService.updatePhoto(salonistId, file));
    }

    @DeleteMapping("/{salonistId}")
    public ResponseEntity<Void> delete(@PathVariable Integer salonistId) {
        salonistCatalogService.delete(salonistId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{salonistId}/access")
    public ResponseEntity<AuthResponse> createAccess(
            @PathVariable Integer salonistId,
            @Valid @RequestBody SalonistAccessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.createSalonistAccess(salonistId, request));
    }
}
