package com.team7.agora.global.lock;

import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
public class RedisLockRepository {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('get', KEYS[1]) == ARGV[1] then "
            + "return redis.call('del', KEYS[1]) "
            + "else return 0 end",
        Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public RedisLockRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, String owner, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, owner, ttl);
        return Boolean.TRUE.equals(acquired);
    }

    public void unlock(String key, String owner) {
        redisTemplate.execute(UNLOCK_SCRIPT, List.of(key), owner);
    }
}
