package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class CouponEventRepositoryAdapter implements CouponEventRepository {

    private final JpaCouponEventDataRepository repository;

    CouponEventRepositoryAdapter(JpaCouponEventDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public CouponEvent save(CouponEvent couponEvent) {
        return repository.save(couponEvent);
    }

    @Override
    public Optional<CouponEvent> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<CouponEvent> findAllByStatus(CouponEventStatus status) {
        return repository.findAllByStatus(status);
    }
}
