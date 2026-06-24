// 인증/인가 실패를 공통 ApiResponse 형식의 JSON으로 직렬화해 응답에 기록하는 유틸
package com.team7.agora.global.auth;

import com.team7.agora.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;

final class AuthErrorResponseWriter {

    private AuthErrorResponseWriter() {
    }

    static void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(toJson(errorCode.getMessage()));
    }

    // ApiResponse.error(message)와 동일한 형식: {"status":"ERROR","message":"...","data":null}
    private static String toJson(String message) {
        return "{\"status\":\"ERROR\",\"message\":\"" + escape(message) + "\",\"data\":null}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
