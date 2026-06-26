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
     * 요청한 동작을 처리한다.
     * @param request 입력 값
     * @param response 입력 값
     * @param accessDeniedException 입력 값
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
