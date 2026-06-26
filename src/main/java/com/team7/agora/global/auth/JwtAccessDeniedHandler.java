// 인가에 실패한 요청에 공통 403 JSON 응답을 반환하는 핸들러
package com.team7.agora.global.auth;

import com.team7.agora.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Authentication component for jwt access denied behavior.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    /**
     * Handles handle behavior.
     * @param request the request value
     * @param response the response value
     * @param accessDeniedException the access denied exception value
     */
    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        AuthErrorResponseWriter.write(response, ErrorCode.FORBIDDEN);
    }
}
