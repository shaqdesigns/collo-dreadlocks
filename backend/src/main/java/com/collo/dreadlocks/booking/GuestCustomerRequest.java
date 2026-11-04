package com.collo.dreadlocks.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestCustomerRequest(
        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "phone is required")
        @Size(max = 30)
        String phone,

        @Email(message = "email is invalid")
        @Size(max = 150)
        String email
) {
}
