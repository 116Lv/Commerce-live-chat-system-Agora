// 토큰 재발급 성공 시 새 토큰을 담는 응답 DTO
package com.team7.agora.domain.auth.dto.response;

public record ReissueResponse(
        String accessToken,
        String refreshToken
) {
}
