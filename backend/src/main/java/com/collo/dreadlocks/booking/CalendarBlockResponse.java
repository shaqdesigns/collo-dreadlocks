package com.collo.dreadlocks.booking;

import java.time.LocalDateTime;

public record CalendarBlockResponse(
        String kind,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String label,
        Integer bookingId
) {
}
