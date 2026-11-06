package com.collo.dreadlocks.crm;

import java.time.LocalDateTime;

public record ClientNoteResponse(
        Integer id,
        Integer customerId,
        String body,
        LocalDateTime createdAt
) {
}
