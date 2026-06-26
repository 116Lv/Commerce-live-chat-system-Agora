package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.coupon.enums.CouponType;
import java.util.List;
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
    public Optional<Coupon> findById(Long couponId) {
        return repository.findById(couponId);
    }

    @Override
    public Optional<Coupon> findFirstComeCoupon() {
        return repository.findFirstByTypeAndStatus(CouponType.FIRST_COME, CouponStatus.ACTIVE);
    }

    @Override
    public List<Coupon> findAll() {
        return repository.findAll();
    }
}
