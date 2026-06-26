// 인증되지 않은 요청에 공통 401 JSON 응답을 반환하는 엔트리포인트
package com.team7.agora.global.auth;

import com.team7.agora.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Authentication component for jwt authentication entry point behavior.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * Handles commence behavior.
     * @param request the request value
     * @param response the response value
     * @param authException the auth exception value
     */
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        AuthErrorResponseWriter.write(response, ErrorCode.UNAUTHORIZED);
    }
}
