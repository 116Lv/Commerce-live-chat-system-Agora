// JWT 페이로드에서 추출한 인증 클레임을 담는 레코드
package com.team7.agora.global.auth;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 * @param userId 회원 ID
 * @param email 이메일
 * @param role 권한
 * @param nickname 닉네임
 */
public record JwtClaims(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
