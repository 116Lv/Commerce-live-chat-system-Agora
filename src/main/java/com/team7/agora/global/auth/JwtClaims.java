// JWT 페이로드에서 추출한 인증 클레임을 담는 레코드
package com.team7.agora.global.auth;

/**
 * Authentication component for jwt claims behavior.
 * @param userId the user id value
 * @param email the email value
 * @param role the role value
 * @param nickname the nickname value
 */
public record JwtClaims(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
