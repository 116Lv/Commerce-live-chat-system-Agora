package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.user.entity.User;

public record AdminManagedUserResponse(
        Long id,
        String email,
        String nickname,
        String status
) {
    public static AdminManagedUserResponse from(User user) {
        return new AdminManagedUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getStatus().name()
        );
    }
}
