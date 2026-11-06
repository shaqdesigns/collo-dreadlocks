package com.collo.dreadlocks.unavailability;

import java.time.LocalDateTime;

public record UnavailabilityResponse(
        Integer id,
        Integer salonistId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String reason,
        UnavailabilityStatus status,
        String adminNote
) {
}
