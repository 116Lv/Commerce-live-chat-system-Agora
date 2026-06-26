// 로그인 성공 시 발급된 토큰을 담는 응답 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * Response payload for returning login data.
 * @param accessToken the access token value
 * @param refreshToken the refresh token value
 */
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
