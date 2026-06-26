// 사용자 스마일지수 조회 응답 DTO
package com.team7.agora.domain.user.dto.response;

import com.team7.agora.domain.user.entity.User;

/**
 * 스마일 점수 응답 본문을 표현하는 DTO이다.
 * @param userId 회원 ID
 * @param smileScore 회원의 매너 점수
 */
public record SmileScoreResponse(
    Long userId,
    int smileScore
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param user 회원 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static SmileScoreResponse from(User user) {
        return new SmileScoreResponse(user.getId(), user.getSmileScore());
    }
}
