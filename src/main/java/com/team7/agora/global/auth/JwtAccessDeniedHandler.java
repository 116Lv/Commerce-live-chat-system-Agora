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
 * 인증 처리를 담당하는 컴포넌트이다.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    /**
     * 접근 권한이 없는 사용자가 보호된 API에 접근했을 때 403 응답을 반환한다.
     * @param request 요청 본문
     * @param response HTTP 응답
     * @param accessDeniedException 접근 거부 예외
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
