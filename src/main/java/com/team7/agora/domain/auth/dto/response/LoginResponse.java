// 로그인 성공 시 발급된 토큰을 담는 응답 DTO
package com.team7.agora.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
