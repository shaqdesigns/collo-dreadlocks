package com.collo.dreadlocks.customer;

import java.time.LocalDate;
import java.time.MonthDay;

public final class BirthdayWindow {

    private BirthdayWindow() {
    }

    public static boolean fallsWithin(LocalDate dateOfBirth, LocalDate from, LocalDate to) {
        if (dateOfBirth == null || from == null || to == null) {
            return false;
        }

        MonthDay birthday = MonthDay.from(dateOfBirth);
        MonthDay start = MonthDay.from(from);
        MonthDay end = MonthDay.from(to);

        if (!start.isAfter(end)) {
            return !birthday.isBefore(start) && !birthday.isAfter(end);
        }

        return !birthday.isBefore(start) || !birthday.isAfter(end);
    }
}
