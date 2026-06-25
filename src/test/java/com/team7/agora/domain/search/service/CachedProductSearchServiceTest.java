package com.team7.agora.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.cache.ProductSearchCache;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CachedProductSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    void searchV2_reusesCachedResultForSameCondition() {
        ProductSearchService productSearchService = new ProductSearchService(
            productRepository,
            new ProductSearchCache(100, 60_000L),
            new SearchPerformanceRecorder()
        );
        ProductSearchCondition condition = new ProductSearchCondition(" 자전거 ", 1L, "SPORTS", PageRequest.of(0, 20));
        when(productRepository.search(condition)).thenReturn(List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동")
        ));

        List<ProductSearchResponse> first = productSearchService.searchV2(condition);
        List<ProductSearchResponse> second = productSearchService.searchV2(condition);

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        verify(productRepository, times(1)).search(condition);
    }

    @Test
    void evictSearchCache_removesCachedResult() {
        ProductSearchCache cache = new ProductSearchCache(100, 60_000L);
        ProductSearchService productSearchService = new ProductSearchService(
            productRepository, cache, new SearchPerformanceRecorder()
        );
        ProductSearchCondition condition = new ProductSearchCondition("자전거", 1L, "SPORTS", PageRequest.of(0, 20));
        when(productRepository.search(condition)).thenReturn(List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동")
        ));

        productSearchService.searchV2(condition);
        productSearchService.evictSearchCache();
        productSearchService.searchV2(condition);

        verify(productRepository, times(2)).search(condition);
    }
}
