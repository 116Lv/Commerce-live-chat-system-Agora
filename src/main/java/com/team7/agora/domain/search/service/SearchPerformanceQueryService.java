package com.team7.agora.domain.search.service;

import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class SearchPerformanceQueryService {

    private final SearchPerformanceRecorder searchPerformanceRecorder;

    public SearchPerformanceQueryService(SearchPerformanceRecorder searchPerformanceRecorder) {
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    public SearchPerformanceResponse getComparison(AdminPrincipal admin) {
        if (admin == null || admin.getRole() != AdminRole.ROOT_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only ROOT_ADMIN can view search performance comparisons.");
        }
        return SearchPerformanceResponse.of(
            searchPerformanceRecorder.getStats("v1"),
            searchPerformanceRecorder.getStats("v2")
        );
    }
}
