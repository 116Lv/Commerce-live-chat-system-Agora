package com.team7.agora.domain.coupon.repository;

import static com.team7.agora.support.TestEntityIds.assignId;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCouponRepository implements CouponRepository {

    private final AtomicLong sequence = new AtomicLong();
    private Coupon firstComeCoupon;

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) {
            assignId(coupon, sequence.incrementAndGet());
        }
        this.firstComeCoupon = coupon;
        return coupon;
    }

    @Override
    public Optional<Coupon> findFirstComeCoupon() {
        return Optional.ofNullable(firstComeCoupon)
            .filter(coupon -> coupon.getType() == CouponType.FIRST_COME);
    }
}
