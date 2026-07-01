package com.team7.agora.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.response.PageResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {
    SearchPerformanceRecorder.class,
    ProductSearchCacheLoader.class,
    ProductSearchCacheLoaderTest.TestConfig.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductSearchCacheLoaderTest {

    @Autowired
    private ProductSearchCacheLoader productSearchCacheLoader;

    @Autowired
    private ProductRepository productRepository;

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("productSearch");
        }

        @Bean
        ProductRepository productRepository() {
            return mock(ProductRepository.class);
        }
    }

    @Test
    void load_reusesCachedResultForSameCondition() {
        ProductSearchCondition condition = new ProductSearchCondition(" 자전거 ", 1L, "SPORTS", PageRequest.of(0, 20), null, null);
        when(productRepository.search(condition)).thenReturn(new PageImpl<>(List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동")
        )));

        PageResponse<ProductSearchResponse> first = productSearchCacheLoader.load(condition);
        PageResponse<ProductSearchResponse> second = productSearchCacheLoader.load(condition);

        assertThat(first.content()).hasSize(1);
        assertThat(second.content()).hasSize(1);
        verify(productRepository, times(1)).search(condition);
    }

    @Test
    void evictAll_removesCachedResult() {
        ProductSearchCondition condition = new ProductSearchCondition("자전거", 1L, "SPORTS", PageRequest.of(0, 20), null, null);
        when(productRepository.search(condition)).thenReturn(new PageImpl<>(List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동")
        )));

        productSearchCacheLoader.load(condition);
        productSearchCacheLoader.evictAll();
        productSearchCacheLoader.load(condition);

        verify(productRepository, times(2)).search(condition);
    }
}
