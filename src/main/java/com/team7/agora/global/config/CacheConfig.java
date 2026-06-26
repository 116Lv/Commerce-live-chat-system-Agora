// Caffeine 기반 In-memory Cache(@Cacheable)를 활성화하는 설정
package com.team7.agora.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache에서 사용하는 Caffeine 기반 인메모리 캐시를 설정한다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCT_SEARCH_CACHE = "productSearch";

    /**
     * 상품 검색 캐싱에 사용할 캐시 매니저를 생성한다.
     * @return Caffeine 캐시 매니저
     */
    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(PRODUCT_SEARCH_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(60, TimeUnit.SECONDS));
        return cacheManager;
    }
}
