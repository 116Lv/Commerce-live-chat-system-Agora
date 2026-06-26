package com.team7.agora.domain.search.service;

import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<ProductSearchResponse> searchV1(ProductSearchCondition condition) {
        long start = System.nanoTime();
        List<ProductSearchResponse> responses = productRepository.search(condition);
        searchPerformanceRecorder.record("v1", System.nanoTime() - start, true);
        return responses;
    }

    public List<ProductSearchResponse> searchV2(ProductSearchCondition condition) {
        long start = System.nanoTime();
        List<ProductSearchResponse> responses = productSearchCacheLoader.load(condition);
        searchPerformanceRecorder.recordCall("v2", System.nanoTime() - start);
        return responses;
    }

    public void evictSearchCache() {
        productSearchCacheLoader.evictAll();
    }
}
