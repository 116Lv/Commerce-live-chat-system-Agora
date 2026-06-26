// Spring @Cacheable(AOP) 기반으로 상품 검색 결과를 캐싱하는 컴포넌트
package com.team7.agora.domain.search.service;

import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Spring Cache AOP를 통해 상품 검색 결과를 조회하고 캐싱하는 경계 컴포넌트이다.
 */
@Component
public class ProductSearchCacheLoader {

    private final ProductRepository productRepository;
    private final SearchPerformanceRecorder searchPerformanceRecorder;

    public ProductSearchCacheLoader(
        ProductRepository productRepository,
        SearchPerformanceRecorder searchPerformanceRecorder
    ) {
        this.productRepository = productRepository;
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    /**
     * 정규화된 검색 조건을 기준으로 상품 검색 결과를 조회하고 캐싱한다.
     * @param condition 검색 조건
     * @return 상품 검색 결과 목록
     */
    @Cacheable(
        cacheNames = CacheConfig.PRODUCT_SEARCH_CACHE,
        key = "'search:' + #condition.normalizedKeyword() + ':' + #condition.regionId() + ':' + #condition.normalizedCategory()"
            + " + ':' + #condition.pageable().pageNumber + ':' + #condition.pageable().pageSize"
    )
    public Page<ProductSearchResponse> load(ProductSearchCondition condition) {
        searchPerformanceRecorder.recordDbHit("v2");
        return productRepository.search(condition);
    }

    /**
     * 캐싱된 모든 상품 검색 결과를 제거한다.
     */
    @CacheEvict(cacheNames = CacheConfig.PRODUCT_SEARCH_CACHE, allEntries = true)
    public void evictAll() {
    }
}
