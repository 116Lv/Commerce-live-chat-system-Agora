// 토큰 재발급 요청 본문을 담는 DTO
package com.team7.agora.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param refreshToken 입력 값
 */
public record ReissueRequest(
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
