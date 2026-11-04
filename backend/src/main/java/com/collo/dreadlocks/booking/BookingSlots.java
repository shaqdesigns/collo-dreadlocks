package com.collo.dreadlocks.booking;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class BookingSlots {

    public static final int STEP_MINUTES = 15;

    private BookingSlots() {
    }

    public static List<LocalTime> starts(
            LocalTime windowStart,
            LocalTime windowEnd,
            int durationMinutes,
            LocalTime notBefore) {
        List<LocalTime> starts = new ArrayList<>();
        if (windowStart == null || windowEnd == null || durationMinutes <= 0) {
            return starts;
        }

        LocalTime slotStart = windowStart;
        while (!slotStart.plusMinutes(durationMinutes).isAfter(windowEnd)) {
            if (notBefore == null || !slotStart.isBefore(notBefore)) {
                starts.add(slotStart);
            }
            slotStart = slotStart.plusMinutes(STEP_MINUTES);
        }
        return starts;
    }

    public static boolean overlaps(
            LocalDateTime startA,
            LocalDateTime endA,
            LocalDateTime startB,
            LocalDateTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }
}
