package com.team7.agora.domain.review.dto.response;

import com.team7.agora.domain.review.entity.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
    Long reviewId,
    Long tradeId,
    Long reviewerId,
    Long targetUserId,
    int rating,
    String content,
    LocalDateTime createdAt
) {

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
