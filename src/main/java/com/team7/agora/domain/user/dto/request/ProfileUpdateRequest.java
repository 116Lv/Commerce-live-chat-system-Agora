package com.team7.agora.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @Size(max = 20, message = "휴대폰 번호는 20자 이하여야 합니다.")
        String phone
) {
}
