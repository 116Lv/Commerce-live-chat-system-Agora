package com.team11.agora.global.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final String secretKey;
    private final long accessTokenValidTime;

    public JwtProvider(
        @Value("${jwt.secret-key}") String secretKey,
        @Value("${jwt.access-token-valid-time}") long accessTokenValidTime
    ) {
        this.secretKey = secretKey;
        this.accessTokenValidTime = accessTokenValidTime;
    }

    public String createToken(Long userId, String email, String role, String nickname) {
        long now = Instant.now().toEpochMilli();
        long expiresAt = now + accessTokenValidTime;

        String header = encodeJson(Map.of(
            "alg", "HS256",
            "typ", "JWT"
        ));
        String payload = encodeJson(new LinkedHashMap<>() {{
            put("sub", String.valueOf(userId));
            put("email", email);
            put("role", role);
            put("nickname", nickname);
            put("iat", now);
            put("exp", expiresAt);
        }});

        String unsignedToken = header + "." + payload;
        return BEARER_PREFIX + unsignedToken + "." + sign(unsignedToken);
    }

    public String substringBearer(String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Bearer 토큰 형식이 아닙니다.");
        }
        return token.substring(BEARER_PREFIX.length());
    }

    public JwtClaims parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "JWT 토큰 형식이 올바르지 않습니다.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "JWT 서명이 올바르지 않습니다.");
        }

        Map<String, String> claims = parseFlatJson(new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8));
        long expiresAt = Long.parseLong(claims.get("exp"));
        if (expiresAt < Instant.now().toEpochMilli()) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        return new JwtClaims(
            Long.parseLong(claims.get("sub")),
            claims.get("email"),
            claims.get("role"),
            claims.get("nickname")
        );
    }

    private String encodeJson(Map<String, ?> values) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (!first) {
                builder.append(",");
            }
            first = false;
            builder.append("\"").append(escape(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof Number) {
                builder.append(value);
            } else {
                builder.append("\"").append(escape(String.valueOf(value))).append("\"");
            }
        }
        builder.append("}");
        return URL_ENCODER.encodeToString(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, String> parseFlatJson(String json) {
        Map<String, String> values = new LinkedHashMap<>();
        String body = json.substring(1, json.length() - 1);
        for (String pair : body.split(",")) {
            int separatorIndex = pair.indexOf(':');
            String key = unquote(pair.substring(0, separatorIndex));
            String value = pair.substring(separatorIndex + 1);
            values.put(key, unquote(value));
        }
        return values;
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("JWT 서명 생성에 실패했습니다.", e);
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        }
        return trimmed;
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected.length() != actual.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }
}
