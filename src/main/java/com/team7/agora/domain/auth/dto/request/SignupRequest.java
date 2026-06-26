// 회원가입 요청 본문을 담는 DTO
package com.team7.agora.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param email 입력 값
 * @param password 입력 값
 * @param nickname 입력 값
 */
public record SignupRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
