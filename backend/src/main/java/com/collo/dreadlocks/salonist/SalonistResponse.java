package com.collo.dreadlocks.salonist;

public record SalonistResponse(
        Integer id,
        String name,
        String phone,
        String email,
        String bio,
        String profileImageUrl,
        boolean isActive
) {
}
