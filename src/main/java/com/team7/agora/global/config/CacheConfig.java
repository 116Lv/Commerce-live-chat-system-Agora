// Redis 기반 Remote Cache(@Cacheable)를 활성화하는 설정
package com.team7.agora.global.config;

import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Spring Cache에서 사용하는 Redis 기반 원격 캐시를 설정한다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCT_SEARCH_CACHE = "productSearch";

    /**
     * 상품 검색 캐싱에 사용할 Redis 캐시 매니저를 생성한다.
     * @param connectionFactory Redis 연결 팩토리
     * @return Redis 캐시 매니저
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(60))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json())
            );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
