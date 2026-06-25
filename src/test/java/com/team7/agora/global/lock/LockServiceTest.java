package com.team7.agora.global.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.global.exception.BusinessException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    private RedisLockRepository redisLockRepository;

    @Test
    void localLockExecutesSupplier() {
        LockService lockService = LockService.local();

        String result = lockService.withLock("coupon:1", () -> "issued");

        assertThat(result).isEqualTo("issued");
    }

    @Test
    void redisLockUsesThreeSecondTtlAndUnlocksWithSameOwner() {
        LockService lockService = new LockService(redisLockRepository, Duration.ofSeconds(3));
        when(redisLockRepository.tryLock(eq("coupon:1"), any(String.class), eq(Duration.ofSeconds(3))))
            .thenReturn(true);

        String result = lockService.withLock("coupon:1", () -> "issued");

        assertThat(result).isEqualTo("issued");

        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisLockRepository).tryLock(eq("coupon:1"), ownerCaptor.capture(), eq(Duration.ofSeconds(3)));
        verify(redisLockRepository).unlock("coupon:1", ownerCaptor.getValue());
    }

    @Test
    void redisLockThrowsImmediatelyWhenLockCannotBeAcquired() {
        LockService lockService = new LockService(redisLockRepository, Duration.ofSeconds(3));
        when(redisLockRepository.tryLock(eq("coupon:1"), any(String.class), eq(Duration.ofSeconds(3))))
            .thenReturn(false);

        assertThatThrownBy(() -> lockService.withLock("coupon:1", () -> "issued"))
            .isInstanceOf(BusinessException.class);

        verify(redisLockRepository, never()).unlock(eq("coupon:1"), any(String.class));
    }
}
