package com.collo.dreadlocks.payment;

import com.collo.dreadlocks.booking.InvalidBookingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MpesaCallbackParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void paidCallbackKeepsTheReceipt() throws Exception {
        MpesaCallbackOutcome outcome = MpesaCallbackParser.parse(objectMapper.readTree("""
                {
                  "Body": {
                    "stkCallback": {
                      "CheckoutRequestID": "ws_CO_123",
                      "ResultCode": 0,
                      "ResultDesc": "The service request is processed successfully.",
                      "CallbackMetadata": {
                        "Item": [
                          {"Name": "MpesaReceiptNumber", "Value": "NLJ7RT61SV"}
                        ]
                      }
                    }
                  }
                }
                """));

        assertTrue(outcome.paid());
        assertFalse(outcome.failed());
        assertEquals("ws_CO_123", outcome.checkoutRequestId());
        assertEquals("NLJ7RT61SV", outcome.receipt());
    }

    @Test
    void cancelledCallbackIsAFailure() throws Exception {
        MpesaCallbackOutcome outcome = MpesaCallbackParser.parse(objectMapper.readTree("""
                {"Body":{"stkCallback":{"CheckoutRequestID":"ws_CO_9","ResultCode":1032,"ResultDesc":"Cancelled"}}}
                """));

        assertFalse(outcome.paid());
        assertTrue(outcome.failed());
        assertEquals("Cancelled", outcome.description());
    }

    @Test
    void phoneAcceptsLocalSafaricomFormat() {
        assertEquals("254712345678", MpesaPhone.normalize("0712 345 678"));
        assertEquals("254112345678", MpesaPhone.normalize("+254112345678"));
    }

    @Test
    void phoneRejectsALandline() {
        assertThrows(InvalidBookingException.class, () -> MpesaPhone.normalize("020123456"));
    }
}
