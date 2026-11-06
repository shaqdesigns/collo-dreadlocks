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
@RequestMapping("/api/me/notifications")
public class MeNotificationController {

    private final NotificationService notificationService;

    public MeNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(notificationService.listForCustomer(current.getCustomerId()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> read(
            @AuthenticationPrincipal AuthUser current,
            @PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markReadForCustomer(id, current.getCustomerId()));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal AuthUser current) {
        notificationService.markAllReadForCustomer(current.getCustomerId());
        return ResponseEntity.noContent().build();
    }
}
