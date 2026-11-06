package com.collo.dreadlocks.revenue;

import java.math.BigDecimal;

public record ServiceRevenueResponse(
        Integer serviceId,
        String serviceName,
        long soughtCount,
        BigDecimal revenue
) {
}
