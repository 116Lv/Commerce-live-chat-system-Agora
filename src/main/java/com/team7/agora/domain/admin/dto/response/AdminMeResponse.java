// 내 관리자 정보 조회 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.user.entity.User;

public record AdminMeResponse(
        Long id,
        String email,
        String nickname,
        String role
) {

    public static AdminMeResponse from(User user) {
        return new AdminMeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
