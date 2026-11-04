package com.collo.dreadlocks.booking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingStatusTransitionsTest {

    @Test
    void allowsPendingToConfirmedOrCancelled() {
        assertTrue(BookingStatusTransitions.canTransition(BookingStatus.PENDING, BookingStatus.CONFIRMED));
        assertTrue(BookingStatusTransitions.canTransition(BookingStatus.PENDING, BookingStatus.CANCELLED));
        assertFalse(BookingStatusTransitions.canTransition(BookingStatus.PENDING, BookingStatus.COMPLETED));
    }

    @Test
    void allowsConfirmedToCompletedCancelledOrNoShow() {
        assertTrue(BookingStatusTransitions.canTransition(BookingStatus.CONFIRMED, BookingStatus.COMPLETED));
        assertTrue(BookingStatusTransitions.canTransition(BookingStatus.CONFIRMED, BookingStatus.CANCELLED));
        assertTrue(BookingStatusTransitions.canTransition(BookingStatus.CONFIRMED, BookingStatus.NO_SHOW));
        assertFalse(BookingStatusTransitions.canTransition(BookingStatus.CONFIRMED, BookingStatus.PENDING));
    }

    @Test
    void terminalStatusesCannotChange() {
        assertFalse(BookingStatusTransitions.canTransition(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
        assertFalse(BookingStatusTransitions.canTransition(BookingStatus.CANCELLED, BookingStatus.CONFIRMED));
        assertFalse(BookingStatusTransitions.canTransition(BookingStatus.NO_SHOW, BookingStatus.COMPLETED));
    }
}
