package com.collo.dreadlocks.crm;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClientSummaryResponse(
        Integer id,
        String name,
        String phone,
        String email,
        boolean active,
        int visits,
        BigDecimal spent,
        LocalDateTime lastVisitAt,
        LocalDateTime nextVisitAt
) {
}
