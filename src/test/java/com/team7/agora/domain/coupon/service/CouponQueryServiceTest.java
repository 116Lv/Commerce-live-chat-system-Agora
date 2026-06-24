package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponQueryServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Test
    void listActiveEventsReturnsActiveCouponEvents() {
        CouponEvent event = CouponEvent.create(
            "오픈 기념 쿠폰",
            100,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(7)
        );
        when(couponEventRepository.findAllByStatus(CouponEventStatus.ACTIVE)).thenReturn(List.of(event));

        List<?> responses = new CouponQueryService(couponEventRepository).listActiveEvents();

        assertThat(responses).hasSize(1);
    }
}
