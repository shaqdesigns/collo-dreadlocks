package com.collo.dreadlocks.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerRequest(
        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "phone is required")
        @Size(max = 30)
        String phone,

        @Email(message = "email is invalid")
        @Size(max = 150)
        String email,

        @PastOrPresent(message = "dateOfBirth cannot be in the future")
        LocalDate dateOfBirth,

        Boolean isActive
) {
}
