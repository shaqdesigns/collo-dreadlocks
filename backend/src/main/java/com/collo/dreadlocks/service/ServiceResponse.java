package com.collo.dreadlocks.service;

import java.math.BigDecimal;

public record ServiceResponse(
        Integer id,
        String name,
        String slug,
        String category,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        String imageUrl,
        Boolean isActive
) {
}
