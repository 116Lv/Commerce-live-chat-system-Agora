package com.team7.agora.global.auth;

public record AuthUser(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
