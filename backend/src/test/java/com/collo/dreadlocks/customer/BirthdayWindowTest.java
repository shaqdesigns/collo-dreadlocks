package com.collo.dreadlocks.customer;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BirthdayWindowTest {

    @Test
    void includesBirthdayInsideTheWindow() {
        assertTrue(BirthdayWindow.fallsWithin(
                LocalDate.of(1995, 11, 5),
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 11, 10)
        ));
    }

    @Test
    void excludesBirthdayOutsideTheWindow() {
        assertFalse(BirthdayWindow.fallsWithin(
                LocalDate.of(1990, 6, 18),
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 11, 10)
        ));
    }

    @Test
    void wrapsAroundTheEndOfTheYear() {
        assertTrue(BirthdayWindow.fallsWithin(
                LocalDate.of(1988, 1, 2),
                LocalDate.of(2026, 12, 28),
                LocalDate.of(2027, 1, 4)
        ));
        assertFalse(BirthdayWindow.fallsWithin(
                LocalDate.of(1988, 1, 8),
                LocalDate.of(2026, 12, 28),
                LocalDate.of(2027, 1, 4)
        ));
    }

    @Test
    void ignoresMissingDateOfBirth() {
        assertFalse(BirthdayWindow.fallsWithin(
                null,
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 11, 10)
        ));
    }
}
