// 관리자 대상 사용자 정보 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.user.entity.User;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param id 입력 값
 * @param email 입력 값
 * @param nickname 입력 값
 * @param role 입력 값
 * @param status 입력 값
 */
public record AdminUserResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String status
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param user 입력 값
     * @return 처리 결과
     */
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}
