// 토큰 재발급 성공 시 새 토큰을 담는 응답 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * Reissue 응답 본문을 표현하는 DTO이다.
 * @param accessToken API 인증에 사용하는 액세스 토큰
 * @param refreshToken 토큰 재발급에 사용하는 리프레시 토큰
 */
public record ReissueResponse(
        String accessToken,
        String refreshToken
) {
}
