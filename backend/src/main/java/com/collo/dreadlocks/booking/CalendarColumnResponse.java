package com.collo.dreadlocks.booking;

import java.util.List;

public record CalendarColumnResponse(
        Integer salonistId,
        String salonistName,
        boolean active,
        List<CalendarBlockResponse> blocks
) {
}
