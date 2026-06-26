package com.team7.agora.domain.review.dto.response;

import com.team7.agora.domain.review.entity.Review;
import java.time.LocalDateTime;

/**
 * Response payload for returning review data.
 * @param reviewId the review id value
 * @param tradeId the trade id value
 * @param reviewerId the reviewer id value
 * @param targetUserId the target user id value
 * @param rating the rating value
 * @param content the content value
 * @param createdAt the created at value
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
     * Creates a response from the given domain object.
     * @param review the review value
     * @return the from result
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
