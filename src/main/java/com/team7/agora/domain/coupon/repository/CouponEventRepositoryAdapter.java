package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;
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
    public List<CouponEvent> findAll() {
        return repository.findAll();
    }

    @Override
    public List<CouponEvent> findPublicIssueableEvents(LocalDateTime now) {
        return repository.findPublicIssueableEvents(now);
    }

    @Override
    public int endActiveEventsBefore(LocalDateTime now) {
        return repository.endActiveEventsBefore(now);
    }
}
