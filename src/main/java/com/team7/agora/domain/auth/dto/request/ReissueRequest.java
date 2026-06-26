// 토큰 재발급 요청 본문을 담는 DTO
package com.team7.agora.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Reissue 요청 본문을 표현하는 DTO이다.
 * @param refreshToken 토큰 재발급에 사용하는 리프레시 토큰
 */
public record ReissueRequest(
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
