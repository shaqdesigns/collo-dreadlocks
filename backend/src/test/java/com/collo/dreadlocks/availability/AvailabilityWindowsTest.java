package com.collo.dreadlocks.availability;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvailabilityWindowsTest {

    @Test
    void endTimeMustBeAfterStartTime() {
        assertTrue(AvailabilityWindows.isValidRange(LocalTime.of(9, 0), LocalTime.of(17, 0)));
        assertFalse(AvailabilityWindows.isValidRange(LocalTime.of(17, 0), LocalTime.of(9, 0)));
        assertFalse(AvailabilityWindows.isValidRange(LocalTime.of(9, 0), LocalTime.of(9, 0)));
    }

    @Test
    void adjacentWindowsDoNotOverlap() {
        assertFalse(AvailabilityWindows.overlaps(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                LocalTime.of(12, 0),
                LocalTime.of(17, 0)
        ));
    }

    @Test
    void overlappingWindowsAreDetected() {
        assertTrue(AvailabilityWindows.overlaps(
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                LocalTime.of(12, 0),
                LocalTime.of(17, 0)
        ));
        assertTrue(AvailabilityWindows.overlaps(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        ));
    }
}
