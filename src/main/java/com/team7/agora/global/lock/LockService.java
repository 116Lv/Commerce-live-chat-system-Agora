package com.team7.agora.global.lock;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Redisson 분산 락을 통해 작업을 실행하고, 테스트에서는 로컬 락 대체 구현을 제공한다.
 */
@Component
public class LockService {

    private static final long WAIT_TIME_SECONDS = 2;

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final RedissonClient redissonClient;
    private final long waitTimeSeconds;

    /**
     * Redisson 분산 락을 사용하는 락 서비스를 생성한다.
     * RedissonClient 빈이 없는 테스트/로컬 컨텍스트에서는 인메모리 로컬 락으로 대체된다.
     * @param redissonClientProvider Redisson 클라이언트 빈을 선택적으로 제공하는 객체
     */
    @Autowired
    public LockService(ObjectProvider<RedissonClient> redissonClientProvider) {
        this(redissonClientProvider.getIfAvailable(), WAIT_TIME_SECONDS);
    }

    LockService(RedissonClient redissonClient, long waitTimeSeconds) {
        this.redissonClient = redissonClient;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    /**
     * 인메모리 로컬 락을 사용하는 락 서비스를 생성한다.
     * @return 로컬 락 서비스
     */
    public static LockService local() {
        return new LockService(null, WAIT_TIME_SECONDS);
    }

    /**
     * 지정한 키의 락을 획득한 상태에서 작업을 실행한다.
     * @param key 락 키
     * @param supplier 락으로 보호할 작업
     * @return 작업 실행 결과
     */
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
