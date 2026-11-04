package com.collo.dreadlocks.booking;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingSlotsTest {

    @Test
    void generatesStartsThatFitTheServiceDuration() {
        List<LocalTime> starts = BookingSlots.starts(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                60,
                null
        );

        assertEquals(LocalTime.of(9, 0), starts.getFirst());
        assertEquals(LocalTime.of(11, 0), starts.getLast());
        assertFalse(starts.contains(LocalTime.of(11, 15)));
    }

    @Test
    void skipsTimesBeforeNotBefore() {
        List<LocalTime> starts = BookingSlots.starts(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                60,
                LocalTime.of(10, 5)
        );

        assertEquals(LocalTime.of(10, 15), starts.getFirst());
    }

    @Test
    void adjacentAppointmentsDoNotOverlap() {
        assertFalse(BookingSlots.overlaps(
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 12, 0),
                LocalDateTime.of(2026, 11, 10, 12, 0),
                LocalDateTime.of(2026, 11, 10, 15, 0)
        ));
    }

    @Test
    void overlappingAppointmentsAreDetected() {
        assertTrue(BookingSlots.overlaps(
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 12, 0),
                LocalDateTime.of(2026, 11, 10, 11, 0),
                LocalDateTime.of(2026, 11, 10, 14, 0)
        ));
    }
}
