package com.collo.dreadlocks.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalonistAccessRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email is invalid")
        @Size(max = 150)
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password
) {
}
