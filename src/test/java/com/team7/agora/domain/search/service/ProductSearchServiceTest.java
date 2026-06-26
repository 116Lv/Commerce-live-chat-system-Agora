package com.team7.agora.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductSearchCacheLoader productSearchCacheLoader;

    @Mock
    private SearchPerformanceRecorder searchPerformanceRecorder;

    @Test
    void searchV1_delegatesToRepositoryEveryCall() {
        ProductSearchService service = new ProductSearchService(
            productRepository, productSearchCacheLoader, searchPerformanceRecorder
        );
        ProductSearchCondition condition = new ProductSearchCondition(" 자전거 ", null, null, PageRequest.of(0, 20));
        when(productRepository.search(condition)).thenReturn(List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동")
        ));

        List<ProductSearchResponse> first = service.searchV1(condition);
        List<ProductSearchResponse> second = service.searchV1(condition);

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        verify(productRepository, times(2)).search(condition);
    }

    @Test
    void searchV2_delegatesToCacheLoaderAndRecordsCallMetric() {
        ProductSearchService service = new ProductSearchService(
            productRepository, productSearchCacheLoader, searchPerformanceRecorder
        );
        ProductSearchCondition condition = new ProductSearchCondition("자전거", 1L, "SPORTS", PageRequest.of(0, 20));
        List<ProductSearchResponse> cached = List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동")
        );
        when(productSearchCacheLoader.load(condition)).thenReturn(cached);

        List<ProductSearchResponse> result = service.searchV2(condition);

        assertThat(result).isEqualTo(cached);
        verify(productSearchCacheLoader, times(1)).load(condition);
        verify(searchPerformanceRecorder, times(1)).recordCall(eq("v2"), anyLong());
    }

    @Test
    void evictSearchCache_delegatesToCacheLoader() {
        ProductSearchService service = new ProductSearchService(
            productRepository, productSearchCacheLoader, searchPerformanceRecorder
        );

        service.evictSearchCache();

        verify(productSearchCacheLoader, times(1)).evictAll();
    }
}
