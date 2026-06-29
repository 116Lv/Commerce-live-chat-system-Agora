package com.team7.agora.domain.review.repository;

import com.team7.agora.domain.review.dto.request.MyReviewType;
import com.team7.agora.domain.review.dto.response.MyReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewQueryRepository {

    Page<MyReviewResponse> findMyReviews(Long userId, MyReviewType type, Pageable pageable);
}
