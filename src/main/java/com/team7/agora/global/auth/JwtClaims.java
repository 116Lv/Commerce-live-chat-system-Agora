// JWT 페이로드에서 추출한 인증 클레임을 담는 레코드
package com.team7.agora.global.auth;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 * @param userId 입력 값
 * @param email 입력 값
 * @param role 입력 값
 * @param nickname 입력 값
 */
public record JwtClaims(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
