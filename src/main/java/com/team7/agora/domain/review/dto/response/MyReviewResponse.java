package com.team7.agora.domain.review.dto.response;

import java.time.LocalDateTime;

public record MyReviewResponse(
    Long reviewId,
    Long tradeId,
    Long productId,
    String productTitle,
    String reviewerNickname,
    String targetNickname,
    int rating,
    String content,
    LocalDateTime createdAt
) {
}
