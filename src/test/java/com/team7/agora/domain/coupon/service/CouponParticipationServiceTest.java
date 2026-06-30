package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class CouponParticipationServiceTest {

    @Test
    void participateDoesNotStartTransactionBeforeLockDelegation() throws NoSuchMethodException {
        assertThat(CouponParticipationService.class.getAnnotation(Transactional.class)).isNull();
        assertThat(CouponParticipationService.class
            .getMethod("participate", Long.class, Long.class)
            .getAnnotation(Transactional.class))
            .isNull();
    }
}
