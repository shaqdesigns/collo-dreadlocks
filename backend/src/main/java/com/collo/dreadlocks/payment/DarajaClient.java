package com.collo.dreadlocks.payment;

import com.collo.dreadlocks.booking.InvalidBookingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DarajaClient {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MpesaProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Clock clock;
    private final HttpClient httpClient;

    public DarajaClient(MpesaProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public StkPushResult push(String phone, BigDecimal amount, String accountReference, String description) {
        if (!properties.configured()) {
            throw new InvalidBookingException(
                    "M-Pesa is not configured yet. Add the Daraja consumer key, secret, passkey, and https callback URL."
            );
        }
        String token = accessToken();
        String timestamp = LocalDateTime.now(clock).format(TIMESTAMP);
        String password = Base64.getEncoder().encodeToString(
                (properties.getShortcode() + properties.getPasskey() + timestamp).getBytes(StandardCharsets.UTF_8)
        );
        String shillings = amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
        if (new BigDecimal(shillings).compareTo(BigDecimal.ONE) < 0) {
            throw new InvalidBookingException("M-Pesa needs an amount of at least Ksh 1");
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put("BusinessShortCode", properties.getShortcode());
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", properties.getTransactionType());
        body.put("Amount", shillings);
        body.put("PartyA", phone);
        body.put("PartyB", properties.getShortcode());
        body.put("PhoneNumber", phone);
        body.put("CallBackURL", properties.getCallbackUrl());
        body.put("AccountReference", limit(accountReference, 12));
        body.put("TransactionDesc", limit(description, 13));

        JsonNode response = post("/mpesa/stkpush/v1/processrequest", body, token);
        String code = response.path("ResponseCode").asText("");
        if (!"0".equals(code)) {
            String reason = response.path("ResponseDescription").asText("Safaricom rejected the prompt");
            throw new InvalidBookingException(reason);
        }
        return new StkPushResult(
                response.path("MerchantRequestID").asText(null),
                response.path("CheckoutRequestID").asText(null),
                response.path("CustomerMessage").asText("Check your phone and enter your M-Pesa PIN")
        );
    }

    private String accessToken() {
        String basic = Base64.getEncoder().encodeToString(
                (properties.getConsumerKey() + ":" + properties.getConsumerSecret()).getBytes(StandardCharsets.UTF_8)
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.baseUrl() + "/oauth/v1/generate?grant_type=client_credentials"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + basic)
                .GET()
                .build();
        JsonNode response = send(request);
        String token = response.path("access_token").asText("");
        if (token.isBlank()) {
            throw new InvalidBookingException("Safaricom did not return an access token. Check the Daraja consumer key and secret.");
        }
        return token;
    }

    private JsonNode post(String path, Map<String, String> body, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.baseUrl() + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            return send(request);
        } catch (InvalidBookingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidBookingException("Could not reach Safaricom to send the M-Pesa prompt");
        }
    }

    private JsonNode send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new InvalidBookingException("Safaricom rejected the M-Pesa request (" + response.statusCode() + ")");
            }
            return objectMapper.readTree(response.body());
        } catch (InvalidBookingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidBookingException("Could not reach Safaricom to send the M-Pesa prompt");
        }
    }

    private static String limit(String value, int max) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
