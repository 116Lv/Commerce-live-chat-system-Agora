// 사용자 스마일지수 조회 응답 DTO
package com.team7.agora.domain.user.dto.response;

import com.team7.agora.domain.user.entity.User;

public record SmileScoreResponse(
    Long userId,
    int smileScore
) {

    public static SmileScoreResponse from(User user) {
        return new SmileScoreResponse(user.getId(), user.getSmileScore());
    }
}
