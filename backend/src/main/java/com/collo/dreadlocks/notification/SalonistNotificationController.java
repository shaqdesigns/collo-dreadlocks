package com.collo.dreadlocks.notification;

import com.collo.dreadlocks.auth.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salonist/notifications")
public class SalonistNotificationController {

    private final NotificationService notificationService;

    public SalonistNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(notificationService.listForSalonist(current.getSalonistId()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> read(
            @AuthenticationPrincipal AuthUser current,
            @PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markReadForSalonist(id, current.getSalonistId()));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal AuthUser current) {
        notificationService.markAllReadForSalonist(current.getSalonistId());
        return ResponseEntity.noContent().build();
    }
}
