package com.team7.agora.global.lock;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Component
public class LockService {

    private static final long WAIT_TIME_SECONDS = 2;

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final RedissonClient redissonClient;
    private final long waitTimeSeconds;

    @Autowired
    public LockService(RedissonClient redissonClient) {
        this(redissonClient, WAIT_TIME_SECONDS);
    }

    LockService(RedissonClient redissonClient, long waitTimeSeconds) {
        this.redissonClient = redissonClient;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public static LockService local() {
        return new LockService(null, WAIT_TIME_SECONDS);
    }

    public <T> T withLock(String key, Supplier<T> supplier) {
        if (redissonClient != null) {
            return withRedissonLock(key, supplier);
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

    private <T> T withRedissonLock(String key, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTimeSeconds, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(ErrorCode.CONFLICT, "요청이 많습니다. 다시 시도해주세요.");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.CONFLICT, "요청이 많습니다. 다시 시도해주세요.");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
