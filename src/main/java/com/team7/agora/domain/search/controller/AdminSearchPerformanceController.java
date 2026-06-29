package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.service.SearchPerformanceQueryService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 검색 성능 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
public class AdminSearchPerformanceController {

    private final SearchPerformanceQueryService searchPerformanceQueryService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param searchPerformanceQueryService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminSearchPerformanceController(SearchPerformanceQueryService searchPerformanceQueryService) {
        this.searchPerformanceQueryService = searchPerformanceQueryService;
    }

    /**
     * 관리자 검색 성능 정보를 조회하는 GET /api/admin/search/performance 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/admin/search/performance")
    public ApiResponse<SearchPerformanceResponse> getPerformanceComparison(
        @AuthenticationPrincipal AdminPrincipal userDetails
    ) {
        return ApiResponse.success("검색 성능 비교 결과입니다.", searchPerformanceQueryService.getComparison(userDetails));
    }
}
