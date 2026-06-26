// 회원가입 결과를 반환하는 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * Signup 응답 본문을 표현하는 DTO이다.
 * @param email 이메일
 * @param nickname 닉네임
 */
public record SignupResponse(
        String email,
        String nickname
) {
}
