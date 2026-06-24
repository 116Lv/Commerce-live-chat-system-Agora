package com.team7.agora.domain.coupon.repository;

import static com.team7.agora.support.TestEntityIds.assignId;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCouponEventRepository implements CouponEventRepository {

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, CouponEvent> store = new ConcurrentHashMap<>();

    @Override
    public CouponEvent save(CouponEvent couponEvent) {
        if (couponEvent.getId() == null) {
            assignId(couponEvent, sequence.incrementAndGet());
        }
        store.put(couponEvent.getId(), couponEvent);
        return couponEvent;
    }

    @Override
    public Optional<CouponEvent> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<CouponEvent> findAllByStatus(CouponEventStatus status) {
        return store.values().stream()
            .filter(event -> event.getStatus() == status)
            .toList();
    }
}
