// 로그인 성공 시 발급된 토큰을 담는 응답 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param accessToken 입력 값
 * @param refreshToken 입력 값
 */
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
