// 회원가입 결과를 반환하는 DTO
package com.team7.agora.domain.auth.dto.response;

public record SignupResponse(
        String email,
        String nickname
) {
}
