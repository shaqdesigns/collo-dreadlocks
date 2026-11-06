package com.collo.dreadlocks.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingMailerTest {

    @Test
    void skipsAddressesThatCannotBeDelivered() {
        assertFalse(BookingMailer.deliverable(null));
        assertFalse(BookingMailer.deliverable("stylist1@collodreadlocks.local"));
        assertTrue(BookingMailer.deliverable("davidsimiyu341@gmail.com"));
    }
}
