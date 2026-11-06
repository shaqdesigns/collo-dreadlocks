package com.collo.dreadlocks.crm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientNoteRequest(
        @NotBlank(message = "note is required")
        @Size(max = 1000)
        String body
) {
}
