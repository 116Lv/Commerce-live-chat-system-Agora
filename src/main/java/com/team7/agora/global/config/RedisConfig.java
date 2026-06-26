// 채팅 메시지를 JSON으로 직렬화해 Redis Pub/Sub에 사용하는 RedisTemplate 설정
package com.team7.agora.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 기반 기능에서 사용할 RedisTemplate 직렬화 방식을 설정한다.
 */
@Configuration
public class RedisConfig {

    /**
     * 문자열 키와 JSON 값을 사용하는 RedisTemplate을 생성한다.
     * @param connectionFactory Redis 연결 팩토리
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}
