// JWT 페이로드에서 추출한 인증 클레임을 담는 레코드
package com.team7.agora.global.auth;

public record JwtClaims(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
