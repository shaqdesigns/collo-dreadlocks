package com.collo.dreadlocks.payment;

import com.collo.dreadlocks.booking.InvalidBookingException;

public final class MpesaPhone {

    private MpesaPhone() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            throw new InvalidBookingException("Enter the M-Pesa phone number that should receive the PIN prompt");
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            digits = "254" + digits.substring(1);
        } else if (digits.length() == 9) {
            digits = "254" + digits;
        }
        if (!digits.matches("254(7|1)\\d{8}")) {
            throw new InvalidBookingException("Enter a Safaricom number like 07XXXXXXXX");
        }
        return digits;
    }
}
