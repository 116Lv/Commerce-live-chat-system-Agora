package com.team7.agora.global.auth;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 * @param userId 입력 값
 * @param email 입력 값
 * @param role 입력 값
 * @param nickname 입력 값
 */
public record AuthUser(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
