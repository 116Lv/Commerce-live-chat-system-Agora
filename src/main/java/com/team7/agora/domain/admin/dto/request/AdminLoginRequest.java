// 관리자 로그인 요청 값을 담는 DTO
package com.team7.agora.domain.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Admin Login 요청 본문을 표현하는 DTO이다.
 * @param email 이메일
 * @param password 비밀번호
 */
public record AdminLoginRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
