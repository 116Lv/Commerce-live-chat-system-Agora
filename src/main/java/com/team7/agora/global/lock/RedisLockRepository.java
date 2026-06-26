package com.team7.agora.global.lock;

import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

/**
 * Distributed locking component for Redis lock behavior.
 */
@Repository
public class RedisLockRepository {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('get', KEYS[1]) == ARGV[1] then "
            + "return redis.call('del', KEYS[1]) "
            + "else return 0 end",
        Long.class
    );

    private final StringRedisTemplate redisTemplate;

    /**
     * Creates a redis lock repository instance.
     * @param redisTemplate the redis template value
     */
    public RedisLockRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Handles try lock behavior.
     * @param key the key value
     * @param owner the owner value
     * @param ttl the ttl value
     * @return the try lock result
     */
    public boolean tryLock(String key, String owner, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, owner, ttl);
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * Handles unlock behavior.
     * @param key the key value
     * @param owner the owner value
     */
    public void unlock(String key, String owner) {
        redisTemplate.execute(UNLOCK_SCRIPT, List.of(key), owner);
    }
}
