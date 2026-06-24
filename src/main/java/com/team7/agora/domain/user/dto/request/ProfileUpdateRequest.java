// 프로필 수정 요청 값을 담는 DTO
package com.team7.agora.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
