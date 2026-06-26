package com.team7.agora.domain.search.service;

import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.cache.ProductSearchCache;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates product search use cases.
 */
@Service
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductSearchCache productSearchCache;
    private final SearchPerformanceRecorder searchPerformanceRecorder;

    /**
     * Creates a product search service instance.
     * @param productRepository the product repository value
     */
    public ProductSearchService(ProductRepository productRepository) {
        this(productRepository, new ProductSearchCache(1_000, 60_000L), new SearchPerformanceRecorder());
    }

    /**
     * Creates a product search service instance.
     * @param productRepository the product repository value
     * @param productSearchCache the product search cache value
     * @param searchPerformanceRecorder the search performance recorder value
     */
    @Autowired
    public ProductSearchService(
        ProductRepository productRepository,
        ProductSearchCache productSearchCache,
        SearchPerformanceRecorder searchPerformanceRecorder
    ) {
        this.productRepository = productRepository;
        this.productSearchCache = productSearchCache;
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    /**
     * Handles search v1 behavior.
     * @param condition the condition value
     * @return the search v1 result
     */
    public List<ProductSearchResponse> searchV1(ProductSearchCondition condition) {
        long start = System.nanoTime();
        List<ProductSearchResponse> responses = productRepository.search(condition);
        searchPerformanceRecorder.record("v1", System.nanoTime() - start, true);
        return responses;
    }

    /**
     * Handles search v2 behavior.
     * @param condition the condition value
     * @return the search v2 result
     */
    public List<ProductSearchResponse> searchV2(ProductSearchCondition condition) {
        long start = System.nanoTime();
        AtomicBoolean dbQueried = new AtomicBoolean(false);
        List<ProductSearchResponse> responses = productSearchCache.get(condition)
            .orElseGet(() -> {
                dbQueried.set(true);
                List<ProductSearchResponse> dbResponses = productRepository.search(condition);
                productSearchCache.put(condition, dbResponses);
                return dbResponses;
            });
        searchPerformanceRecorder.record("v2", System.nanoTime() - start, dbQueried.get());
        return responses;
    }

    /**
     * Handles evict search cache behavior.
     */
    public void evictSearchCache() {
        productSearchCache.clear();
    }
}
