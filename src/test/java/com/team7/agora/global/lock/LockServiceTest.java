package com.team7.agora.global.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.global.exception.BusinessException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @AfterEach
    void tearDown() {
        Thread.interrupted();
    }

    @Test
    void localLockExecutesSupplier() {
        LockService lockService = LockService.local();

        String result = lockService.withLock("coupon:1", () -> "issued");

        assertThat(result).isEqualTo("issued");
    }

    @Test
    void redissonLockUsesWaitTimeWithoutLeaseTimeSoWatchdogStaysActiveAndUnlocksWhenHeldByCurrentThread() throws InterruptedException {
        LockService lockService = new LockService(redissonClient, 2);
        when(redissonClient.getLock("coupon:1")).thenReturn(rLock);
        when(rLock.tryLock(2, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        String result = lockService.withLock("coupon:1", () -> "issued");

        assertThat(result).isEqualTo("issued");
        verify(rLock).tryLock(2, TimeUnit.SECONDS);
        verify(rLock).unlock();
    }

    @Test
    void redissonLockThrowsWhenLockCannotBeAcquired() throws InterruptedException {
        LockService lockService = new LockService(redissonClient, 2);
        when(redissonClient.getLock("coupon:1")).thenReturn(rLock);
        when(rLock.tryLock(2, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() -> lockService.withLock("coupon:1", () -> "issued"))
            .isInstanceOf(BusinessException.class);

        verify(rLock, never()).unlock();
    }

    @Test
    void redissonLockRestoresInterruptFlagWhenInterruptedWhileWaiting() throws InterruptedException {
        LockService lockService = new LockService(redissonClient, 2);
        when(redissonClient.getLock("coupon:1")).thenReturn(rLock);
        when(rLock.tryLock(any(Long.class), eq(TimeUnit.SECONDS)))
            .thenThrow(new InterruptedException());

        assertThatThrownBy(() -> lockService.withLock("coupon:1", () -> "issued"))
            .isInstanceOf(BusinessException.class);

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        verify(rLock, never()).unlock();
    }
}
