package com.collo.dreadlocks.booking;

import java.util.Map;
import java.util.Set;

public final class BookingStatusTransitions {

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED = Map.of(
            BookingStatus.PENDING, Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
            BookingStatus.CONFIRMED, Set.of(
                    BookingStatus.COMPLETED,
                    BookingStatus.CANCELLED,
                    BookingStatus.NO_SHOW
            ),
            BookingStatus.COMPLETED, Set.of(),
            BookingStatus.CANCELLED, Set.of(),
            BookingStatus.NO_SHOW, Set.of()
    );

    private BookingStatusTransitions() {
    }

    public static boolean canTransition(BookingStatus from, BookingStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}
