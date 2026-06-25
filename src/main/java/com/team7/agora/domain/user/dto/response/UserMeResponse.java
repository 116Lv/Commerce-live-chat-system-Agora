// 내 정보 조회 응답 값을 담는 DTO
package com.team7.agora.domain.user.dto.response;

import com.team7.agora.domain.user.entity.User;

public record UserMeResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String status
) {

    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}
