package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.admin.entity.Admin;
import java.util.List;

public record AdminMeResponse(
        Long id,
        String email,
        String nickname,
        String role,
        List<String> permissions
) {

    public AdminMeResponse(Long id, String email, String nickname, String role) {
        this(id, email, nickname, role, List.of());
    }

    public static AdminMeResponse from(Admin admin) {
        return new AdminMeResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getNickname(),
                admin.getRole().name(),
                admin.getEffectivePermissions().stream()
                        .map(Enum::name)
                        .toList()
        );
    }
}
