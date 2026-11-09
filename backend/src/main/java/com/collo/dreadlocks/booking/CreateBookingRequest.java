package com.collo.dreadlocks.booking;

import com.collo.dreadlocks.payment.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull(message = "serviceId is required")
        Integer serviceId,

        Integer salonistId,

        @NotNull(message = "startAt is required")
        LocalDateTime startAt,

        @NotNull(message = "customer is required")
        @Valid
        GuestCustomerRequest customer,

        @NotNull(message = "Choose M-Pesa or cash")
        PaymentMethod paymentMethod,

        String mpesaPhone
) {
}
