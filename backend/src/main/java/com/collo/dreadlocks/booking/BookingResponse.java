package com.collo.dreadlocks.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        Integer id,
        UUID reference,
        BookingStatus status,
        Integer customerId,
        String customerName,
        String customerPhone,
        Integer serviceId,
        String serviceName,
        Integer salonistId,
        String salonistName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer durationMinutes,
        BigDecimal price,
        String paymentMethod,
        String paymentStatus,
        String paymentPhone,
        String mpesaReceipt,
        String paymentMessage
) {
}
