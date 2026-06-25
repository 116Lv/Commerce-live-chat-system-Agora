// 비밀번호 변경 요청 값을 담는 DTO
package com.team7.agora.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        String newPassword
) {
}
