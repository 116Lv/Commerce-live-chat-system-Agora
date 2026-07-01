package com.team7.agora.domain.coupon.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.test.support.RepositorySliceTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RepositorySliceTest
class JpaCouponDataRepositoryTest {

    @Autowired
    private JpaCouponDataRepository couponDataRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void expireIssuedCouponsBeforeExpiresOnlyCouponsBeforeExclusiveCutoff() {
        LocalDateTime cutoff = LocalDateTime.of(2026, 7, 1, 0, 0);
        CouponEvent event = persistEvent();
        Coupon expiredYesterday = persistIssuedCoupon(event, cutoff.minusDays(2), 1);
        Coupon expiresAtCutoff = persistIssuedCoupon(event, cutoff.minusDays(1), 1);
        Coupon expiresAfterCutoff = persistIssuedCoupon(event, cutoff, 1);
        Coupon usedBeforeCutoff = persistIssuedCoupon(event, cutoff.minusDays(3), 1);
        usedBeforeCutoff.use(cutoff.minusDays(2));
        flushAndClear();

        int expiredCount = couponDataRepository.expireIssuedCouponsBefore(cutoff);
        flushAndClear();

        assertThat(expiredCount).isEqualTo(1);
        assertThat(findCoupon(expiredYesterday).getStatus()).isEqualTo(CouponStatus.EXPIRED);
        assertThat(findCoupon(expiresAtCutoff).getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(findCoupon(expiresAfterCutoff).getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(findCoupon(usedBeforeCutoff).getStatus()).isEqualTo(CouponStatus.USED);
    }

    private CouponEvent persistEvent() {
        CouponEvent event = CouponEvent.create(
            CouponEventType.FIRST_COME,
            "Cleanup boundary coupon",
            10,
            LocalDateTime.of(2026, 6, 1, 0, 0),
            LocalDateTime.of(2026, 7, 31, 23, 59),
            5000,
            10000,
            7
        );
        entityManager.persist(event);
        return event;
    }

    private Coupon persistIssuedCoupon(CouponEvent event, LocalDateTime issuedAt, int validDays) {
        Coupon coupon = Coupon.createAvailableSlot(event);
        coupon.assign(null, issuedAt, validDays);
        entityManager.persist(coupon);
        return coupon;
    }

    private Coupon findCoupon(Coupon coupon) {
        return couponDataRepository.findById(coupon.getId()).orElseThrow();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
