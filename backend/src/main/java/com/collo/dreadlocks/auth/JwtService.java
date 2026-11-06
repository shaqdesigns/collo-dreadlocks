package com.collo.dreadlocks.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public String createToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(authProperties.getJwt().getExpirationMs());

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getEmail());
        payload.put("uid", user.getId());
        payload.put("role", user.getRole().name());
        payload.put("salonistId", user.getSalonistId());
        payload.put("customerId", user.getCustomerId());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiry.getEpochSecond());

        String encodedHeader = encode(writeJson(header));
        String encodedPayload = encode(writeJson(payload));
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public Map<String, Object> parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }
        String expected = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expected, parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        Map<String, Object> claims = readJson(decode(parts[1]));
        Object exp = claims.get("exp");
        if (exp instanceof Number number && Instant.ofEpochSecond(number.longValue()).isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expired");
        }
        return claims;
    }

    public String extractEmail(String token) {
        Object subject = parse(token).get("sub");
        return subject == null ? null : subject.toString();
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes(), "HmacSHA256"));
            return encode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign token", exception);
        }
    }

    private byte[] secretBytes() {
        byte[] bytes = authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = java.util.Arrays.copyOf(bytes, 32);
        }
        return bytes;
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private byte[] writeJson(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to encode token", exception);
        }
    }

    private Map<String, Object> readJson(byte[] value) {
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token payload", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }
}
