package com.collo.dreadlocks.revenue;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ManualWorkRequest(
        Integer salonistId,

        @NotNull(message = "serviceId is required")
        Integer serviceId,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "workedAt is required")
        LocalDateTime workedAt,

        @Size(max = 150)
        String customerName,

        @Size(max = 500)
        String note
) {
}
