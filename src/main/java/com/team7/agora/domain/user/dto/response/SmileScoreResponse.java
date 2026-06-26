// 사용자 스마일지수 조회 응답 DTO
package com.team7.agora.domain.user.dto.response;

import com.team7.agora.domain.user.entity.User;

/**
 * Response payload for returning smile score data.
 * @param userId the user id value
 * @param smileScore the smile score value
 */
public record SmileScoreResponse(
    Long userId,
    int smileScore
) {

    /**
     * Creates a response from the given domain object.
     * @param user the user value
     * @return the from result
     */
    public static SmileScoreResponse from(User user) {
        return new SmileScoreResponse(user.getId(), user.getSmileScore());
    }
}
