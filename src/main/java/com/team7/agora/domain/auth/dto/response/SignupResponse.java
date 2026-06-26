// 회원가입 결과를 반환하는 DTO
package com.team7.agora.domain.auth.dto.response;

/**
 * Response payload for returning signup data.
 * @param email the email value
 * @param nickname the nickname value
 */
public record SignupResponse(
        String email,
        String nickname
) {
}
