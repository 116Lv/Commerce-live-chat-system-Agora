// 사용자 스마일지수 조회 응답 DTO
package com.team7.agora.domain.user.dto.response;

import com.team7.agora.domain.user.entity.User;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param userId 입력 값
 * @param smileScore 입력 값
 */
public record SmileScoreResponse(
    Long userId,
    int smileScore
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param user 입력 값
     * @return 처리 결과
     */
    public static SmileScoreResponse from(User user) {
        return new SmileScoreResponse(user.getId(), user.getSmileScore());
    }
}
