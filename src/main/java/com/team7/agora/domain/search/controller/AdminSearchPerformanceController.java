package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.service.SearchPerformanceQueryService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminSearchPerformanceController {

    private final SearchPerformanceQueryService searchPerformanceQueryService;

    public AdminSearchPerformanceController(SearchPerformanceQueryService searchPerformanceQueryService) {
        this.searchPerformanceQueryService = searchPerformanceQueryService;
    }

    @GetMapping("/api/admin/search/performance")
    public ApiResponse<SearchPerformanceResponse> getPerformanceComparison(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success("검색 성능 비교 결과입니다.", searchPerformanceQueryService.getComparison(userDetails.toAuthUser()));
    }
}
