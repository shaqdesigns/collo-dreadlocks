package com.collo.dreadlocks.remark;

import java.time.LocalDateTime;

public record RemarkResponse(
        Integer id,
        Integer bookingId,
        Integer customerId,
        String customerName,
        Integer salonistId,
        String salonistName,
        Integer serviceId,
        String serviceName,
        LocalDateTime appointmentAt,
        String body,
        LocalDateTime createdAt
) {
}
