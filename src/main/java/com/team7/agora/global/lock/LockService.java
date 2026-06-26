package com.team7.agora.global.lock;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Distributed locking component for lock behavior.
 */
@Component
public class LockService {

    private static final Duration REDIS_LOCK_TTL = Duration.ofSeconds(3);

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final RedisLockRepository redisLockRepository;
    private final Duration redisLockTtl;

    /**
     * Creates a lock service instance.
     * @param redisLockRepository the redis lock repository value
     */
    @Autowired
    public LockService(RedisLockRepository redisLockRepository) {
        this(redisLockRepository, REDIS_LOCK_TTL);
    }

    LockService(RedisLockRepository redisLockRepository, Duration redisLockTtl) {
        this.redisLockRepository = redisLockRepository;
        this.redisLockTtl = redisLockTtl;
    }

    /**
     * Handles local behavior.
     * @return the local result
     */
    public static LockService local() {
        return new LockService(null, REDIS_LOCK_TTL);
    }

    /**
     * Handles with lock behavior.
     * @param key the key value
     * @param supplier the supplier value
     * @return the with lock result
     */
    public <T> T withLock(String key, Supplier<T> supplier) {
        if (redisLockRepository != null) {
            return withRedisLock(key, supplier);
        }
        return withLocalLock(key, supplier);
    }

    private <T> T withLocalLock(String key, Supplier<T> supplier) {
        ReentrantLock lock = locks.computeIfAbsent(key, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    private <T> T withRedisLock(String key, Supplier<T> supplier) {
        String lockOwner = UUID.randomUUID().toString();
        if (!redisLockRepository.tryLock(key, lockOwner, redisLockTtl)) {
            throw new BusinessException(ErrorCode.CONFLICT, "잠시 후 다시 시도해 주세요.");
        }

        try {
            return supplier.get();
        } finally {
            redisLockRepository.unlock(key, lockOwner);
        }
    }
}
