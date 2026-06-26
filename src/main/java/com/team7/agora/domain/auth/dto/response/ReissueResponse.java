// 토큰 재발급 성공 시 새 토큰을 담는 응답 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * Response payload for returning reissue data.
 * @param accessToken the access token value
 * @param refreshToken the refresh token value
 */
public record ReissueResponse(
        String accessToken,
        String refreshToken
) {
}
