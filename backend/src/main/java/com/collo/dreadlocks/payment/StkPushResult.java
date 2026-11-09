package com.collo.dreadlocks.payment;

public record StkPushResult(
        String merchantRequestId,
        String checkoutRequestId,
        String customerMessage
) {
}
