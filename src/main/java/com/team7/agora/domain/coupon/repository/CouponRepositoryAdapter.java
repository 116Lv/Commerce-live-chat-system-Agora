package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import java.time.LocalDateTime;
import java.util.Collection;
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
    public List<Coupon> saveAll(List<Coupon> coupons) {
        return repository.saveAll(coupons);
    }

    @Override
    public Optional<Coupon> findById(Long couponId) {
        return repository.findById(couponId);
    }

    @Override
    public Optional<Coupon> findFirstAvailableSlotForUpdate(Long eventId) {
        return repository.findFirstByCouponEventIdAndUserIsNullAndStatusOrderByIdAsc(eventId, CouponStatus.AVAILABLE);
    }

    @Override
    public List<Coupon> findAllByUserIdAndStatusIn(Long userId, Collection<CouponStatus> statuses) {
        return repository.findAllByUserIdAndStatusIn(userId, statuses);
    }

    @Override
    public List<Coupon> findAllByCouponEventIdAndUserIsNotNull(Long eventId) {
        return repository.findAllByCouponEventIdAndUserIsNotNull(eventId);
    }

    @Override
    public boolean existsByCouponEventIdAndUserId(Long eventId, Long userId) {
        return repository.existsByCouponEventIdAndUserId(eventId, userId);
    }

    @Override
    public void deleteByCouponEventIdAndUserIsNull(Long eventId) {
        repository.deleteByCouponEventIdAndUserIsNull(eventId);
    }

    @Override
    public int expireIssuedCouponsBefore(LocalDateTime now) {
        return repository.expireIssuedCouponsBefore(now);
    }
}
