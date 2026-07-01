package com.team7.agora.domain.coupon.dto.response;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class CouponResponseDisplayTest {

    @Test
    void eventAvailabilityUsesKoreanOperationTime() {
        LocalDateTime kstNow = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        CouponEvent event = CouponEvent.create(
            CouponEventType.FIRST_COME,
            "KST operation coupon",
            10,
            kstNow.minusMinutes(1),
            kstNow.plusMinutes(10),
            5000,
            10000,
            30
        );
        assignId(event, 1L);

        AdminCouponEventResponse response = AdminCouponEventResponse.from(event);

        assertThat(response.canIssue()).isTrue();
        assertThat(response.ended()).isFalse();
        assertThat(response.statusLabel()).isEqualTo("Active");
    }
}
