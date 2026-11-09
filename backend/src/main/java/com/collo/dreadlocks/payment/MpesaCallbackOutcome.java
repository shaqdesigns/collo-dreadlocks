package com.collo.dreadlocks.payment;

public record MpesaCallbackOutcome(
        String checkoutRequestId,
        boolean paid,
        boolean failed,
        String description,
        String receipt,
        Integer bookingId
) {

    public static MpesaCallbackOutcome ignored() {
        return new MpesaCallbackOutcome(null, false, false, null, null, null);
    }

    public MpesaCallbackOutcome withBooking(Integer bookingId) {
        return new MpesaCallbackOutcome(checkoutRequestId, paid, failed, description, receipt, bookingId);
    }
}
