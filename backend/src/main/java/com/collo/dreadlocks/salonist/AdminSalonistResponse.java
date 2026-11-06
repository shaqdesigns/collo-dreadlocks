package com.collo.dreadlocks.salonist;

public record AdminSalonistResponse(
        Integer id,
        String name,
        String phone,
        String email,
        String bio,
        boolean isActive,
        boolean hasPortalAccess
) {
}
