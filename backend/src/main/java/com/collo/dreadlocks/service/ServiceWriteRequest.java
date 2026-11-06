package com.collo.dreadlocks.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceWriteRequest(
        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "category is required")
        @Size(max = 50)
        String category,

        @Size(max = 1000)
        String description,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", message = "price must be zero or more")
        BigDecimal price,

        @NotNull(message = "duration is required")
        @Min(value = 1, message = "duration must be at least 1 minute")
        @Max(value = 600, message = "duration must be 600 minutes or less")
        Integer durationMinutes
) {
}
