package com.collo.dreadlocks.booking;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayResponse(
        LocalDate date,
        List<CalendarColumnResponse> salonists
) {
}
