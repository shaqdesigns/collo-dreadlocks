package com.collo.dreadlocks.booking;

import java.time.LocalDateTime;

public record BookingSlotResponse(
        Integer salonistId,
        String salonistName,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
