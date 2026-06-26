package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.service.PopularKeywordService;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api")
public class SearchController {

    private final ProductSearchService productSearchService;
    private final PopularKeywordService popularKeywordService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param productSearchService 입력 값
     * @param popularKeywordService 입력 값
     */
    public SearchController(ProductSearchService productSearchService, PopularKeywordService popularKeywordService) {
        this.productSearchService = productSearchService;
        this.popularKeywordService = popularKeywordService;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param keyword 입력 값
     * @param regionId 입력 값
     * @param category 입력 값
     * @param page 입력 값
     * @param size 입력 값
     * @return 처리 결과
     */
    @GetMapping("/v1/products/search")
    public ResponseEntity<ApiResponse<List<ProductSearchResponse>>> searchV1(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        popularKeywordService.recordSearchKeyword(viewerId(userDetails), keyword);
        List<ProductSearchResponse> responses = productSearchService.searchV1(
            new ProductSearchCondition(keyword, regionId, category, PageRequest.of(page, size))
        );
        return ResponseEntity.ok(ApiResponse.success("상품 검색 결과입니다.", responses));
    }

    /**
     * 요청한 동작을 처리한다.
     * @param keyword 입력 값
     * @param regionId 입력 값
     * @param category 입력 값
     * @param page 입력 값
     * @param size 입력 값
     * @return 처리 결과
     */
    @GetMapping("/v2/products/search")
    public ResponseEntity<ApiResponse<List<ProductSearchResponse>>> searchV2(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        popularKeywordService.recordSearchKeyword(viewerId(userDetails), keyword);
        List<ProductSearchResponse> responses = productSearchService.searchV2(
            new ProductSearchCondition(keyword, regionId, category, PageRequest.of(page, size))
        );
        return ResponseEntity.ok(ApiResponse.success("캐시 적용 상품 검색 결과입니다.", responses));
    }

    private Long viewerId(CustomUserDetails userDetails) {
        return userDetails == null ? null : userDetails.getUserId();
    }

    @GetMapping({"/v1/search/popular", "/search/keywords/realtime"})
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> popularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("인기 검색어 목록입니다.", popularKeywordService.getTopKeywords(limit)));
    }

    /**
     * 요청한 동작을 처리한다.
     * @param limit 입력 값
     * @return 처리 결과
     */
    @GetMapping("/search/keywords/daily")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> dailyPopularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("일간 인기 검색어입니다.", popularKeywordService.getTopDailyKeywords(limit)));
    }

    /**
     * 요청한 동작을 처리한다.
     * @param limit 입력 값
     * @return 처리 결과
     */
    @GetMapping("/search/keywords/weekly")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> weeklyPopularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("주간 인기 검색어입니다.", popularKeywordService.getTopWeeklyKeywords(limit)));
    }
}
