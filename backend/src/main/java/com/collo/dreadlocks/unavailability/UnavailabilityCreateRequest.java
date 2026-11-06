package com.collo.dreadlocks.unavailability;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UnavailabilityCreateRequest(
        @NotNull(message = "startAt is required")
        LocalDateTime startAt,

        @NotNull(message = "endAt is required")
        LocalDateTime endAt,

        @Size(max = 500)
        String reason
) {
}
