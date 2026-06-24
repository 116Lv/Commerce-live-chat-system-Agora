package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.service.PopularKeywordService;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final ProductSearchService productSearchService;
    private final PopularKeywordService popularKeywordService;

    public SearchController(ProductSearchService productSearchService, PopularKeywordService popularKeywordService) {
        this.productSearchService = productSearchService;
        this.popularKeywordService = popularKeywordService;
    }

    @GetMapping("/v1/products/search")
    public ResponseEntity<ApiResponse<List<ProductSearchResponse>>> searchV1(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        popularKeywordService.recordSearchKeyword(keyword);
        List<ProductSearchResponse> responses = productSearchService.searchV1(
            new ProductSearchCondition(keyword, regionId, category, PageRequest.of(page, size))
        );
        return ResponseEntity.ok(ApiResponse.success("상품 검색 결과입니다.", responses));
    }

    @GetMapping("/v2/products/search")
    public ResponseEntity<ApiResponse<List<ProductSearchResponse>>> searchV2(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        popularKeywordService.recordSearchKeyword(keyword);
        List<ProductSearchResponse> responses = productSearchService.searchV2(
            new ProductSearchCondition(keyword, regionId, category, PageRequest.of(page, size))
        );
        return ResponseEntity.ok(ApiResponse.success("캐시 적용 상품 검색 결과입니다.", responses));
    }

    @GetMapping({"/v1/search/popular", "/search/keywords/realtime"})
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> popularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("인기 검색어 목록입니다.", popularKeywordService.getTopKeywords(limit)));
    }

    @GetMapping("/search/keywords/daily")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> dailyPopularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("일간 인기 검색어입니다.", popularKeywordService.getTopDailyKeywords(limit)));
    }

    @GetMapping("/search/keywords/weekly")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> weeklyPopularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("주간 인기 검색어입니다.", popularKeywordService.getTopWeeklyKeywords(limit)));
    }
}
