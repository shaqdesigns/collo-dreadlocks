package com.collo.dreadlocks.salonist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSalonistRequest(
        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "phone is required")
        @Size(max = 30)
        String phone,

        @Size(max = 150)
        String email,

        @Size(max = 1000)
        String bio,

        List<Integer> serviceIds
) {
}
