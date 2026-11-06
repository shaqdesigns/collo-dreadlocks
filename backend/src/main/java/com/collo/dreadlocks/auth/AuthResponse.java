package com.collo.dreadlocks.auth;

public record AuthResponse(
        String token,
        Integer userId,
        String email,
        UserRole role,
        Integer salonistId,
        Integer customerId
) {
}
