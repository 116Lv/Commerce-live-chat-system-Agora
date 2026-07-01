package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponCleanupServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Test
    void cleanupExpiredEndsEventsDeletesAvailableSlotsAndExpiresIssuedCoupons() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        when(couponEventRepository.endActiveEventsBefore(now)).thenReturn(2);
        when(couponRepository.deleteAvailableSlotsForEndedEventsBefore(now)).thenReturn(7);
        when(couponRepository.expireIssuedCouponsBefore(todayStart)).thenReturn(3);

        var result = new CouponCleanupService(couponEventRepository, couponRepository).cleanupExpired(now);

        assertThat(result.endedEventCount()).isEqualTo(2);
        assertThat(result.deletedAvailableSlotCount()).isEqualTo(7);
        assertThat(result.expiredIssuedCouponCount()).isEqualTo(3);
        verify(couponRepository).expireIssuedCouponsBefore(todayStart);
    }

    @Test
    void expireIssuedCouponsUsesTodayStartAsExclusiveCutoff() {
        LocalDateTime justAfterMidnight = LocalDateTime.of(2026, 7, 1, 0, 0, 1);
        LocalDateTime todayStart = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(couponRepository.expireIssuedCouponsBefore(todayStart)).thenReturn(5);

        int expiredCount = new CouponCleanupService(couponEventRepository, couponRepository)
            .expireIssuedCoupons(justAfterMidnight);

        assertThat(expiredCount).isEqualTo(5);
        verify(couponRepository).expireIssuedCouponsBefore(todayStart);
    }
}
