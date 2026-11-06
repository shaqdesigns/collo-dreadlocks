package com.collo.dreadlocks.revenue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ManualWorkResponse(
        Integer id,
        Integer salonistId,
        String salonistName,
        Integer serviceId,
        String serviceName,
        String customerName,
        BigDecimal price,
        LocalDateTime workedAt,
        String note
) {
}
