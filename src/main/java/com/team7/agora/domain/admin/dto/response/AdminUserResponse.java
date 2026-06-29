package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.user.entity.User;

public record AdminUserResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String status
) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }

    public static AdminUserResponse from(Admin admin) {
        return new AdminUserResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getNickname(),
                admin.getRole().name(),
                admin.getStatus().name()
        );
    }
}
