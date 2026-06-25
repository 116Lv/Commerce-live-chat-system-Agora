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

@Component
public class LockService {

    private static final Duration REDIS_LOCK_TTL = Duration.ofSeconds(10);

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final RedisLockRepository redisLockRepository;
    private final Duration redisLockTtl;

    @Autowired
    public LockService(RedisLockRepository redisLockRepository) {
        this(redisLockRepository, REDIS_LOCK_TTL);
    }

    LockService(RedisLockRepository redisLockRepository, Duration redisLockTtl) {
        this.redisLockRepository = redisLockRepository;
        this.redisLockTtl = redisLockTtl;
    }

    public static LockService local() {
        return new LockService(null, REDIS_LOCK_TTL);
    }

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
            throw new BusinessException(ErrorCode.CONFLICT, "요청이 많습니다. 다시 시도해주세요.");
        }

        try {
            return supplier.get();
        } finally {
            redisLockRepository.unlock(key, lockOwner);
        }
    }
}
