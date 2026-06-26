// Redisson 분산 락 클라이언트를 생성하는 설정
package com.team7.agora.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 분산 락에 사용할 Redisson 클라이언트를 설정한다.
 */
@Configuration
public class RedissonConfig {

    /**
     * Spring Redis 연결 속성을 기반으로 Redisson 클라이언트를 생성한다.
     * 테스트처럼 Redis가 필요 없는 컨텍스트에서는 agora.redisson.enabled=false로 비활성화할 수 있다.
     * @param host Redis 호스트
     * @param port Redis 포트
     * @param password 비밀번호
     * @return Redisson 클라이언트
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "agora.redisson.enabled", havingValue = "true", matchIfMissing = true)
    public RedissonClient redissonClient(
        @Value("${spring.data.redis.host}") String host,
        @Value("${spring.data.redis.port}") int port,
        @Value("${spring.data.redis.password:}") String password
    ) {
        Config config = new Config();
        var singleServerConfig = config.useSingleServer()
            .setAddress("redis://" + host + ":" + port);

        if (password != null && !password.isBlank()) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
