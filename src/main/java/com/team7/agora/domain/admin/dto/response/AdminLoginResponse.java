// 관리자 로그인 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

/**
 * Admin Login 응답 본문을 표현하는 DTO이다.
 * @param accessToken API 인증에 사용하는 액세스 토큰
 */
public record AdminLoginResponse(
        String accessToken
) {
}
