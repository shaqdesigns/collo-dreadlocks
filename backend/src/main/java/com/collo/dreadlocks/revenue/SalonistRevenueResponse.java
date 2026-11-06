package com.collo.dreadlocks.revenue;

import java.math.BigDecimal;

public record SalonistRevenueResponse(
        Integer salonistId,
        String salonistName,
        long completedBookings,
        long manualWorks,
        BigDecimal revenue,
        BigDecimal payout
) {
}
