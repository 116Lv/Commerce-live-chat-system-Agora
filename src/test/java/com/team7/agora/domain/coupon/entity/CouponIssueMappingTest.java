package com.team7.agora.domain.coupon.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Table;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CouponIssueMappingTest {

    @Test
    void couponIssuePreventsDuplicateCouponPolicyPerUser() {
        Table table = CouponIssue.class.getAnnotation(Table.class);

        boolean hasCouponUserUniqueConstraint = Arrays.stream(table.uniqueConstraints())
            .anyMatch(constraint -> Arrays.equals(constraint.columnNames(), new String[]{"coupon_id", "user_id"}));

        assertThat(hasCouponUserUniqueConstraint).isTrue();
    }
}
