package com.team7.agora.domain.search.service;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class SearchPerformanceQueryService {

    private final SearchPerformanceRecorder searchPerformanceRecorder;

    public SearchPerformanceQueryService(SearchPerformanceRecorder searchPerformanceRecorder) {
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

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
