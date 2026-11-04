package com.collo.dreadlocks.availability;

import java.time.LocalTime;

public final class AvailabilityWindows {

    private AvailabilityWindows() {
    }

    public static boolean isValidRange(LocalTime startTime, LocalTime endTime) {
        return startTime != null && endTime != null && endTime.isAfter(startTime);
    }

    public static boolean overlaps(
            LocalTime startA,
            LocalTime endA,
            LocalTime startB,
            LocalTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }
}
