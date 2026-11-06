package com.collo.dreadlocks.service;

import java.time.LocalDateTime;

public record ServicePhotoResponse(
        Integer id,
        Integer serviceId,
        String imageUrl,
        LocalDateTime createdAt
) {
}
