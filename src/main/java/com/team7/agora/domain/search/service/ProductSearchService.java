package com.team7.agora.domain.search.service;

import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 검색 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductSearchCacheLoader productSearchCacheLoader;
    private final SearchPerformanceRecorder searchPerformanceRecorder;

    @Autowired
    public ProductSearchService(
        ProductRepository productRepository,
        ProductSearchCacheLoader productSearchCacheLoader,
        SearchPerformanceRecorder searchPerformanceRecorder
    ) {
        this.productRepository = productRepository;
        this.productSearchCacheLoader = productSearchCacheLoader;
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    /**
     * 'searchV1' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param condition 검색 조건
     * @return 클라이언트에 반환할 API 응답
     */
    public List<ProductSearchResponse> searchV1(ProductSearchCondition condition) {
        long start = System.nanoTime();
        List<ProductSearchResponse> responses = productRepository.search(condition);
        searchPerformanceRecorder.record("v1", System.nanoTime() - start, true);
        return responses;
    }

    /**
     * 'searchV2' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param condition 검색 조건
     * @return 클라이언트에 반환할 API 응답
     */
    public List<ProductSearchResponse> searchV2(ProductSearchCondition condition) {
        long start = System.nanoTime();
        List<ProductSearchResponse> responses = productSearchCacheLoader.load(condition);
        searchPerformanceRecorder.recordCall("v2", System.nanoTime() - start);
        return responses;
    }

    /**
     * 'evictSearchCache' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     */
    public void evictSearchCache() {
        productSearchCacheLoader.evictAll();
    }
}
