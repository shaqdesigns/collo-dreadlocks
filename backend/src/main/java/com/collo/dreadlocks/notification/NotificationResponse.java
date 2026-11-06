package com.collo.dreadlocks.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Integer id,
        String message,
        Integer bookingId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
