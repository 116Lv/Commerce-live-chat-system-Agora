// 토큰 재발급 성공 시 새 토큰을 담는 응답 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param accessToken 입력 값
 * @param refreshToken 입력 값
 */
public record ReissueResponse(
        String accessToken,
        String refreshToken
) {
}
