package com.team7.agora.global.auth;

public record JwtClaims(
    Long subjectId,
    String email,
    String role,
    String nickname,
    AccountType accountType
) {

    public Long userId() {
        return subjectId;
    }
}
