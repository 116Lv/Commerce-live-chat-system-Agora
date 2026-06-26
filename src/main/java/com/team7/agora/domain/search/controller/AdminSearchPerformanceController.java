package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.service.SearchPerformanceQueryService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
public class AdminSearchPerformanceController {

    private final SearchPerformanceQueryService searchPerformanceQueryService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param searchPerformanceQueryService 입력 값
     */
    public AdminSearchPerformanceController(SearchPerformanceQueryService searchPerformanceQueryService) {
        this.searchPerformanceQueryService = searchPerformanceQueryService;
    }

    /**
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @return 처리 결과
     */
    @GetMapping("/api/admin/search/performance")
    public ApiResponse<SearchPerformanceResponse> getPerformanceComparison(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success("검색 성능 비교 결과입니다.", searchPerformanceQueryService.getComparison(userDetails.toAuthUser()));
    }
}
