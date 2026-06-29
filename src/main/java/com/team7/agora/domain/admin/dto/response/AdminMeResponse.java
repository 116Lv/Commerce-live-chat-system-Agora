package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.admin.entity.Admin;

public record AdminMeResponse(
        Long id,
        String email,
        String nickname,
        String role
) {

    public static AdminMeResponse from(Admin admin) {
        return new AdminMeResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getNickname(),
                admin.getRole().name()
        );
    }
}
