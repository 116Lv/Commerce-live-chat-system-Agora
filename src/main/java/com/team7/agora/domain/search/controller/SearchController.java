package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.service.PopularKeywordService;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import com.team7.agora.global.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api")
public class SearchController {

    private final ProductSearchService productSearchService;
    private final PopularKeywordService popularKeywordService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productSearchService 해당 기능의 비즈니스 로직을 처리하는 서비스
     * @param popularKeywordService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public SearchController(ProductSearchService productSearchService, PopularKeywordService popularKeywordService) {
        this.productSearchService = productSearchService;
        this.popularKeywordService = popularKeywordService;
    }

    /**
     * 검색 정보를 조회하는 GET /api/v1/products/search 요청을 처리한다.
     * @param keyword 검색어
     * @param regionId 지역 ID
     * @param category 이미지를 저장할 분류
     * @param status 상품 판매 상태
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 항목 개수
     * @param sort 정렬 기준 (recent, likes)
     * @param direction 정렬 방향 (desc, asc)
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/v1/products/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductSearchResponse>>> searchV1(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) ProductStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "recent") String sort,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        popularKeywordService.recordSearchKeyword(viewerId(userDetails), keyword);
        return ResponseEntity.ok(ApiResponse.success("상품 검색 결과입니다.",
            PageResponse.from(productSearchService.searchV1(
                new ProductSearchCondition(keyword, regionId, category, status, PageRequest.of(page, size), sort, direction)
            ))
        ));
    }

    /**
     * 검색 정보를 조회하는 GET /api/v2/products/search 요청을 처리한다.
     * @param keyword 검색어
     * @param regionId 지역 ID
     * @param category 이미지를 저장할 분류
     * @param status 상품 판매 상태
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 항목 개수
     * @param sort 정렬 기준 (recent, likes)
     * @param direction 정렬 방향 (desc, asc)
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/v2/products/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductSearchResponse>>> searchV2(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) ProductStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "recent") String sort,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        popularKeywordService.recordSearchKeyword(viewerId(userDetails), keyword);
        return ResponseEntity.ok(ApiResponse.success("캐시 적용 상품 검색 결과입니다.",
            productSearchService.searchV2(
                new ProductSearchCondition(keyword, regionId, category, status, PageRequest.of(page, size), sort, direction)
            )
        ));
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
     * 검색 기능을 처리하는 GET /api/search/keywords/daily 요청을 처리한다.
     * @param limit 조회할 최대 개수
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/search/keywords/daily")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> dailyPopularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("일간 인기 검색어입니다.", popularKeywordService.getTopDailyKeywords(limit)));
    }

    /**
     * 검색 기능을 처리하는 GET /api/search/keywords/weekly 요청을 처리한다.
     * @param limit 조회할 최대 개수
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/search/keywords/weekly")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> weeklyPopularKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("주간 인기 검색어입니다.", popularKeywordService.getTopWeeklyKeywords(limit)));
    }
}
