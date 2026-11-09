package com.collo.dreadlocks.payment;

import com.fasterxml.jackson.databind.JsonNode;

public final class MpesaCallbackParser {

    private MpesaCallbackParser() {
    }

    public static MpesaCallbackOutcome parse(JsonNode root) {
        if (root == null || root.isNull()) {
            return MpesaCallbackOutcome.ignored();
        }
        JsonNode callback = root.path("Body").path("stkCallback");
        if (callback.isMissingNode() || callback.isNull()) {
            return MpesaCallbackOutcome.ignored();
        }
        String checkout = text(callback, "CheckoutRequestID");
        if (checkout == null || checkout.isBlank()) {
            return MpesaCallbackOutcome.ignored();
        }
        JsonNode codeNode = callback.get("ResultCode");
        if (codeNode == null || codeNode.isNull()) {
            return MpesaCallbackOutcome.ignored();
        }
        int code = codeNode.isNumber() ? codeNode.asInt() : parseCode(codeNode.asText("1"));
        String description = text(callback, "ResultDesc");
        String receipt = null;
        for (JsonNode item : callback.path("CallbackMetadata").path("Item")) {
            if ("MpesaReceiptNumber".equals(item.path("Name").asText())) {
                receipt = item.path("Value").asText(null);
            }
        }
        boolean paid = code == 0;
        return new MpesaCallbackOutcome(checkout, paid, !paid, description, receipt, null);
    }

    private static int parseCode(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }
}
