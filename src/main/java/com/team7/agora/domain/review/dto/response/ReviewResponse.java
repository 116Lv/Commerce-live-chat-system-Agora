package com.team7.agora.domain.review.dto.response;

import com.team7.agora.domain.review.entity.Review;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param reviewId 입력 값
 * @param tradeId 입력 값
 * @param reviewerId 입력 값
 * @param targetUserId 입력 값
 * @param rating 입력 값
 * @param content 입력 값
 * @param createdAt 입력 값
 */
public record ReviewResponse(
    Long reviewId,
    Long tradeId,
    Long reviewerId,
    Long targetUserId,
    int rating,
    String content,
    LocalDateTime createdAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param review 입력 값
     * @return 처리 결과
     */
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getTrade().getId(),
            review.getReviewer().getId(),
            review.getTargetUser().getId(),
            review.getRating(),
            review.getContent(),
            review.getCreatedAt()
        );
    }
}
