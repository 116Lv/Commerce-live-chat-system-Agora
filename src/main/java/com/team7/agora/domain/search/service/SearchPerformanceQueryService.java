package com.team7.agora.domain.search.service;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Application service that coordinates search performance query use cases.
 */
@Service
public class SearchPerformanceQueryService {

    private final SearchPerformanceRecorder searchPerformanceRecorder;

    /**
     * Creates a search performance query service instance.
     * @param searchPerformanceRecorder the search performance recorder value
     */
    public SearchPerformanceQueryService(SearchPerformanceRecorder searchPerformanceRecorder) {
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    /**
     * Returns comparison data.
     * @param authUser the auth user value
     * @return the get comparison result
     */
    public SearchPerformanceResponse getComparison(AuthUser authUser) {
        if (authUser == null || !"ROOT_ADMIN".equals(authUser.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "검색 성능 비교 조회는 ROOT_ADMIN만 수행할 수 있습니다.");
        }
        return SearchPerformanceResponse.of(
            searchPerformanceRecorder.getStats("v1"),
            searchPerformanceRecorder.getStats("v2")
        );
    }
}
