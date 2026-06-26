// 내 정보 조회 응답 값을 담는 DTO
package com.team7.agora.domain.user.dto.response;

import com.team7.agora.domain.user.entity.User;

/**
 * Response payload for returning user me data.
 * @param id the id value
 * @param email the email value
 * @param nickname the nickname value
 * @param role the role value
 * @param status the status value
 */
public record UserMeResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String status
) {

    /**
     * Creates a response from the given domain object.
     * @param user the user value
     * @return the from result
     */
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
