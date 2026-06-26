package com.team7.agora.domain.review.dto.response;

import com.team7.agora.domain.review.entity.Review;
import java.time.LocalDateTime;

/**
 * 리뷰 응답 본문을 표현하는 DTO이다.
 * @param reviewId 후기 ID
 * @param tradeId 거래 ID
 * @param reviewerId 후기를 작성한 회원 ID
 * @param targetUserId 후기를 받는 회원 ID
 * @param rating 후기 평점
 * @param content 내용
 * @param createdAt 데이터가 생성된 시각
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
     * @param review 후기 엔티티
     * @return 클라이언트에 반환할 API 응답
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
