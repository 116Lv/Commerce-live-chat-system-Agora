// 토큰 재발급 요청 본문을 담는 DTO
package com.team7.agora.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
