package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.service.SearchPerformanceQueryService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes admin search performance endpoints.
 */
@RestController
public class AdminSearchPerformanceController {

    private final SearchPerformanceQueryService searchPerformanceQueryService;

    /**
     * Creates a admin search performance controller instance.
     * @param searchPerformanceQueryService the search performance query service value
     */
    public AdminSearchPerformanceController(SearchPerformanceQueryService searchPerformanceQueryService) {
        this.searchPerformanceQueryService = searchPerformanceQueryService;
    }

    /**
     * Returns performance comparison data.
     * @param authUser the auth user value
     * @return the get performance comparison result
     */
    @GetMapping("/api/admin/search/performance")
    public ApiResponse<SearchPerformanceResponse> getPerformanceComparison(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success("검색 성능 비교 결과입니다.", searchPerformanceQueryService.getComparison(userDetails.toAuthUser()));
    }
}
