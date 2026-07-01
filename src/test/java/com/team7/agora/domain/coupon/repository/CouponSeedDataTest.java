package com.team7.agora.domain.coupon.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest
class CouponSeedDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void localProfileCouponSlotsMatchEventRemainingQuantities() {
        List<Map<String, Object>> mismatches = jdbcTemplate.queryForList("""
            select
                ce.id,
                ce.total_quantity,
                ce.issued_quantity,
                coalesce(slots.available_count, 0) as available_count
            from coupon_events ce
            left join (
                select coupon_event_id, count(*) as available_count
                from coupons
                where status = 'AVAILABLE' and user_id is null
                group by coupon_event_id
            ) slots on slots.coupon_event_id = ce.id
            where ce.id in (1, 2, 3)
              and coalesce(slots.available_count, 0) <> (ce.total_quantity - ce.issued_quantity)
            """);

        assertThat(mismatches).isEmpty();
    }
}
