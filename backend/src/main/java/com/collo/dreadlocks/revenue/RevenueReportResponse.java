package com.collo.dreadlocks.revenue;

import java.math.BigDecimal;
import java.util.List;

public record RevenueReportResponse(
        BigDecimal payoutRate,
        BigDecimal totalRevenue,
        BigDecimal salonistPayout,
        BigDecimal studioShare,
        BigDecimal bookingRevenue,
        BigDecimal manualRevenue,
        ServiceRevenueResponse mostSought,
        ServiceRevenueResponse highestRevenue,
        List<ServiceRevenueResponse> services,
        List<SalonistRevenueResponse> salonists,
        List<ManualWorkResponse> manualWorks
) {
}
