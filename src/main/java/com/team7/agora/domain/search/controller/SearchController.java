package com.team7.agora.domain.search.controller;

import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
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

    public SearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping("/v1/products/search")
    public ResponseEntity<ApiResponse<List<ProductSearchResponse>>> searchV1(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        List<ProductSearchResponse> responses = productSearchService.searchV1(
            new ProductSearchCondition(keyword, regionId, category, PageRequest.of(page, size))
        );
        return ResponseEntity.ok(ApiResponse.success("상품 검색 결과입니다.", responses));
    }
}
