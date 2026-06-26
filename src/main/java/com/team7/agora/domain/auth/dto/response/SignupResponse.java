// 회원가입 결과를 반환하는 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param email 입력 값
 * @param nickname 입력 값
 */
public record SignupResponse(
        String email,
        String nickname
) {
}
