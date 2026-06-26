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
 * 인증 처리를 담당하는 컴포넌트이다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * 인증되지 않은 사용자가 보호된 API에 접근했을 때 401 응답을 반환한다.
     * @param request 요청 본문
     * @param response HTTP 응답
     * @param authException 인증 예외
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
