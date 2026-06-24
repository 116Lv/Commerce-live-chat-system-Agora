package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.coupon.enums.CouponType;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class CouponRepositoryAdapter implements CouponRepository {

    private final JpaCouponDataRepository repository;

    CouponRepositoryAdapter(JpaCouponDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        return repository.save(coupon);
    }

    @Override
    public Optional<Coupon> findFirstComeCoupon() {
        return repository.findFirstByTypeAndStatus(CouponType.FIRST_COME, CouponStatus.ACTIVE);
    }
}
