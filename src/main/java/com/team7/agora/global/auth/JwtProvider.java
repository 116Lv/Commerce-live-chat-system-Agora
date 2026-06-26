// JWT(HS256) 토큰을 생성하고 서명·만료를 검증하는 컴포넌트
package com.team7.agora.global.auth;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 */
@Component
public class JwtProvider {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final String secretKey;
    private final long accessTokenValidTime;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param secretKey JWT 서명에 사용하는 비밀 키
     * @param accessTokenValidTime 액세스 토큰 유효 시간
     */
    public JwtProvider(
        @Value("${jwt.secret-key}") String secretKey,
        @Value("${jwt.access-token-valid-time}") long accessTokenValidTime
    ) {
        this.secretKey = secretKey;
        this.accessTokenValidTime = accessTokenValidTime;
    }

    /**
     * 인증된 사용자 정보를 담은 JWT 액세스 토큰을 생성한다.
     * @param userId 회원 ID
     * @param email 이메일
     * @param role 권한
     * @param nickname 닉네임
     * @return 클라이언트에 반환할 API 응답
     */
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

    /**
     * 'substringBearer' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param token JWT 토큰
     * @return 클라이언트에 반환할 API 응답
     */
    public String substringBearer(String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Bearer 토큰 형식이 아닙니다.");
        }
        return token.substring(BEARER_PREFIX.length());
    }

    /**
     * 'parse' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param token JWT 토큰
     * @return 클라이언트에 반환할 API 응답
     */
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

    // 따옴표 안의 쉼표/콜론까지 안전하게 처리하는 평면 JSON 객체 파서
    private Map<String, String> parseFlatJson(String json) {
        Map<String, String> values = new LinkedHashMap<>();
        int[] cursor = {1}; // 여는 중괄호 다음부터 시작
        while (cursor[0] < json.length() && json.charAt(cursor[0]) != '}') {
            skipWhitespace(json, cursor);
            String key = readString(json, cursor);
            skipWhitespace(json, cursor);
            cursor[0]++; // ':' 건너뛰기
            skipWhitespace(json, cursor);
            String value = json.charAt(cursor[0]) == '"'
                ? readString(json, cursor)
                : readPrimitive(json, cursor);
            values.put(key, value);
            skipWhitespace(json, cursor);
            if (cursor[0] < json.length() && json.charAt(cursor[0]) == ',') {
                cursor[0]++;
            }
        }
        return values;
    }

    // cursor가 여는 따옴표를 가리킨다고 가정하고, 이스케이프를 해제한 문자열을 읽는다
    private String readString(String json, int[] cursor) {
        StringBuilder builder = new StringBuilder();
        cursor[0]++; // 여는 따옴표 건너뛰기
        while (cursor[0] < json.length()) {
            char current = json.charAt(cursor[0]);
            if (current == '\\' && cursor[0] + 1 < json.length()) {
                builder.append(json.charAt(cursor[0] + 1));
                cursor[0] += 2;
            } else if (current == '"') {
                cursor[0]++; // 닫는 따옴표 건너뛰기
                break;
            } else {
                builder.append(current);
                cursor[0]++;
            }
        }
        return builder.toString();
    }

    // 따옴표가 없는 값(숫자 등)을 ',' 또는 '}' 전까지 읽는다
    private String readPrimitive(String json, int[] cursor) {
        int start = cursor[0];
        while (cursor[0] < json.length()
            && json.charAt(cursor[0]) != ','
            && json.charAt(cursor[0]) != '}') {
            cursor[0]++;
        }
        return json.substring(start, cursor[0]).trim();
    }

    private void skipWhitespace(String json, int[] cursor) {
        while (cursor[0] < json.length() && Character.isWhitespace(json.charAt(cursor[0]))) {
            cursor[0]++;
        }
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
