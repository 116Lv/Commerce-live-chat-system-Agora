// 프로필 수정 요청 값을 담는 DTO
package com.team7.agora.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Profile Update 요청 본문을 표현하는 DTO이다.
 * @param nickname 닉네임
 */
public record ProfileUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
