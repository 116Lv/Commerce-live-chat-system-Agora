package com.team7.agora.global.auth;

/**
 * Authentication component for auth user behavior.
 * @param userId the user id value
 * @param email the email value
 * @param role the role value
 * @param nickname the nickname value
 */
public record AuthUser(
    Long userId,
    String email,
    String role,
    String nickname
) {
}
