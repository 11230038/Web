package com.example.end.auth;

import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class JwtUtil {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public JwtUtil(JwtProperties jwtProperties, ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.objectMapper = objectMapper;
    }

    public String generateToken(Long userId, String username, Integer role) {
        long now = Instant.now().getEpochSecond();
        long exp = now + jwtProperties.getExpireSeconds();

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", role);
        payload.put("iat", now);
        payload.put("exp", exp);

        try {
            String headerPart = encode(objectMapper.writeValueAsBytes(header));
            String payloadPart = encode(objectMapper.writeValueAsBytes(payload));
            String content = headerPart + "." + payloadPart;
            String signature = sign(content);
            return content + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("failed to generate jwt token", e);
        }
    }

    public Map<String, Object> parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalStateException("invalid jwt format");
            }

            String content = parts[0] + "." + parts[1];
            String expectedSignature = sign(content);
            if (!expectedSignature.equals(parts[2])) {
                throw new IllegalStateException("invalid jwt signature");
            }

            byte[] payloadBytes = URL_DECODER.decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, Map.class);
            Number exp = (Number) payload.get("exp");
            if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
                throw new IllegalStateException("jwt token expired");
            }
            return payload;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("failed to parse jwt token", e);
        }
    }

    private String encode(byte[] bytes) {
        return URL_ENCODER.encodeToString(bytes);
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec keySpec = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(keySpec);
        return encode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }
}
