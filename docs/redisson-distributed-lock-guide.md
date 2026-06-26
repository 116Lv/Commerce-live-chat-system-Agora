# Redisson Distributed Lock Guide

Last reviewed: 2026-06-25

## Current Verdict

Redisson is not currently applied.

The project has Redis-backed locking, but it is a custom implementation:

- `build.gradle` includes `spring-boot-starter-data-redis`, not Redisson.
- `RedisLockRepository` uses `StringRedisTemplate.setIfAbsent(...)`.
- Unlock is handled by a custom Lua script.
- `LockService` falls back to local `ReentrantLock` through `LockService.local()` in tests.
- `CouponIssueService` uses `lockService.withLock("lock:coupon-event:" + couponEventId, ...)`.

This means the current behavior is closer to "manual Redis lock" than "Redisson distributed lock."

## Why Redisson Should Replace the Current Lock

Redisson gives us:

- `RLock` with `tryLock(waitTime, leaseTime, TimeUnit)`.
- Safer unlock ownership checks through Redisson internals.
- Optional watchdog-based lease extension when using lock without explicit lease time.
- Cleaner configuration for Redis single-server, sentinel, or cluster modes.
- A well-tested implementation instead of maintaining our own lock protocol.

## Target Design

Keep the rest of the application calling one project-level abstraction:

- Keep `LockService.withLock(key, supplier)` as the public app API.
- Replace `RedisLockRepository` usage with `RedissonClient`.
- Remove custom `SET NX` and Lua unlock code after migration.
- Use Redisson directly only inside `global.lock`.

Recommended lock package after migration:

```text
global.lock
  LockService.java
  LockAcquisitionException.java optional
  RedissonConfig.java optional if starter auto config is not enough
```

## Gradle Dependency

Add Redisson starter:

```gradle
implementation 'org.redisson:redisson-spring-boot-starter:3.27.2'
```

Version note:

- Pin a version compatible with the Spring Boot version used by the project.
- If the project stays on Spring Boot `4.1.0`, verify Redisson starter compatibility before merging.
- If compatibility is uncertain, use Redisson core plus explicit `RedissonClient` bean configuration.

## Configuration

Add explicit Redis/Redisson settings per profile.

Example local:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379

redisson:
  address: redis://localhost:6379
```

Example production parameterized config:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

redisson:
  address: redis://${REDIS_HOST}:${REDIS_PORT:6379}
  password: ${REDIS_PASSWORD:}
```

If the starter does not support these exact custom properties, create a `RedissonConfig` bean:

```java
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    RedissonClient redissonClient(
        @Value("${spring.data.redis.host}") String host,
        @Value("${spring.data.redis.port}") int port
    ) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }
}
```

## LockService Replacement

Target implementation shape:

```java
@Component
public class LockService {

    private static final long WAIT_TIME_SECONDS = 2;
    private static final long LEASE_TIME_SECONDS = 5;

    private final RedissonClient redissonClient;

    public LockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T withLock(String key, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(ErrorCode.CONFLICT, "잠시 후 다시 시도해 주세요.");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.CONFLICT, "잠시 후 다시 시도해 주세요.");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

Design decision:

- Use explicit `leaseTime` for predictable coupon issuance.
- Pick `leaseTime` longer than the worst expected DB transaction time.
- If the critical section can exceed a fixed lease time, use Redisson watchdog mode by calling `lock.lock()` or `tryLock(waitTime, -1, unit)` depending on Redisson API support and team preference.

## Transaction Boundary Fix

Do not keep the current `@Transactional` method as the same method that acquires and releases the distributed lock.

Current pattern:

```java
@Transactional
public void issue(Long userId, Long couponEventId) {
    lockService.withLock(key, () -> {
        // DB work
        return null;
    });
}
```

Problem:

- Spring commits the transaction after the method returns.
- The lock can be released before commit completes.

Recommended pattern:

```java
public void issue(Long userId, Long couponEventId) {
    lockService.withLock("lock:coupon-event:" + couponEventId, () -> {
        issueInTransaction(userId, couponEventId);
        return null;
    });
}

@Transactional
public void issueInTransaction(Long userId, Long couponEventId) {
    // DB read/update/save
}
```

Important Spring caveat:

- `issueInTransaction` must be called through a Spring proxy.
- Put the transactional method in a separate bean, or inject the proxied self carefully.
- Preferred structure:

```text
CouponIssueService
  acquires Redisson lock
  calls CouponIssueTransactionService.issue(...)

CouponIssueTransactionService
  @Transactional
  performs repository work
```

## Coupon-Specific Lock Key

Current key:

```java
"lock:coupon-event:" + couponEventId
```

Keep this shape. It is good because all users competing for the same event share one lock, while different events can issue independently.

Recommended final constant:

```java
private static final String COUPON_EVENT_LOCK_PREFIX = "lock:coupon-event:";
```

## DB Constraints Still Required

Redisson should reduce concurrent access, but it should not be the only correctness guard.

Add DB constraints:

- `coupon_issues`: unique `(coupon_event_id, user_id)`
- `payments`: unique `trade_id`
- `settlements`: unique `payment_id`
- active-trade-per-product strategy, either via DB constraint, row lock, or status model change

Why:

- Locks can expire.
- Redis may be unavailable.
- A future code path may bypass the lock.
- DB constraints are the final source of truth.

## Test Plan

### Unit tests

Replace `RedisLockRepository` mocking tests with Redisson-facing tests:

- Lock acquired: supplier runs and `unlock()` is called.
- Lock not acquired: `BusinessException(CONFLICT)` is thrown and supplier does not run.
- Interrupted while waiting: thread interrupt flag is restored.
- Unlock only when held by current thread.

### Integration tests

Add an integration-style test with a real Redis test container or embedded Redis equivalent:

- 30 concurrent requests to issue 5 coupons.
- Assert `issuedQuantity == 5`.
- Assert issue row count is 5.
- Assert no duplicate `(event, user)` rows.

### Transaction boundary test

Add a test or architecture check that verifies:

- Lock is acquired outside the transactional method.
- Transactional coupon issue work lives in a separate Spring bean.

## Migration Steps

1. Add Redisson dependency.
2. Add Redis/Redisson config properties for local and prod.
3. Replace `LockService` internals with `RedissonClient` and `RLock`.
4. Delete `RedisLockRepository` after no code references it.
5. Split `CouponIssueService.issue(...)` into lock wrapper and transactional worker bean.
6. Add DB unique constraints for coupon issue safety.
7. Update tests from `RedisLockRepository` mocks to Redisson lock behavior.
8. Run:

```powershell
.\gradlew.bat test
.\gradlew.bat javadoc
```

## Acceptance Criteria

- `rg -n "StringRedisTemplate|RedisLockRepository|setIfAbsent|UNLOCK_SCRIPT" src/main/java` returns no lock implementation references.
- `rg -n "RedissonClient|RLock|tryLock" src/main/java` finds the lock implementation.
- Coupon issue concurrency test passes with Redisson-backed locking.
- DB constraints prevent duplicate coupon issue rows even if lock logic is bypassed.
- `gradlew.bat test` passes.
