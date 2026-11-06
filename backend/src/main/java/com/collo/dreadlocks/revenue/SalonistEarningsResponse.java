package com.collo.dreadlocks.revenue;

import java.math.BigDecimal;

public record SalonistEarningsResponse(
        BigDecimal payoutRate,
        BigDecimal revenue,
        BigDecimal payout,
        long completedBookings,
        long manualWorks
) {
}
