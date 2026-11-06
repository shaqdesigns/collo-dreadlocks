package com.collo.dreadlocks.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "phone is required")
        @Size(max = 30)
        String phone,

        @NotBlank(message = "email is required")
        @Email(message = "email is invalid")
        @Size(max = 150)
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password,

        @PastOrPresent(message = "dateOfBirth cannot be in the future")
        LocalDate dateOfBirth
) {
}
