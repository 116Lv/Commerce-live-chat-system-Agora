// 내 관리자 정보 조회 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.user.entity.User;

/**
 * Admin Me 응답 본문을 표현하는 DTO이다.
 * @param id 식별자
 * @param email 이메일
 * @param nickname 닉네임
 * @param role 권한
 */
public record AdminMeResponse(
        Long id,
        String email,
        String nickname,
        String role
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param user 회원 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static AdminMeResponse from(User user) {
        return new AdminMeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
