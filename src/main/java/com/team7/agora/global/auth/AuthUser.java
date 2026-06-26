package com.team7.agora.global.auth;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 * @param userId 회원 ID
 * @param email 이메일
 * @param role 권한
 * @param nickname 닉네임
 */
public record AuthUser(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
