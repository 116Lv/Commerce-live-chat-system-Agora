// Redisson 분산 락 클라이언트를 생성하는 설정
package com.team7.agora.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 분산 락에 사용할 Redisson 클라이언트를 설정한다.
 */
@Configuration
public class RedissonConfig {

    /**
     * Spring Redis 연결 속성을 기반으로 Redisson 클라이언트를 생성한다.
     * @param host Redis 호스트
     * @param port Redis 포트
     * @param password 비밀번호
     * @return Redisson 클라이언트
     */
    @Bean(destroyMethod = "shutdown")
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
