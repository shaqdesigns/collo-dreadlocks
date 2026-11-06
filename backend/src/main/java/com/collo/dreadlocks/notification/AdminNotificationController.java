package com.collo.dreadlocks.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list() {
        return ResponseEntity.ok(notificationService.listForAdmin());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> read(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markReadForAdmin(id));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> readAll() {
        notificationService.markAllReadForAdmin();
        return ResponseEntity.noContent().build();
    }
}
