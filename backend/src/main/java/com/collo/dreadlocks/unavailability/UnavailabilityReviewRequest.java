package com.collo.dreadlocks.unavailability;

import jakarta.validation.constraints.Size;

public record UnavailabilityReviewRequest(
        @Size(max = 500)
        String adminNote
) {
}
