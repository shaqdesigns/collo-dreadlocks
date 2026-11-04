package com.collo.dreadlocks.customer;

import java.time.LocalDate;

public record CustomerResponse(
        Integer id,
        String name,
        String phone,
        String email,
        LocalDate dateOfBirth,
        boolean isActive
) {
}
