package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import com.team7.agora.global.lock.LockService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CouponSlotService {

    private static final String COUPON_EVENT_LOCK_PREFIX = "lock:coupon-event:";

    private final LockService lockService;
    private final CouponSlotTransactionExecutor executor;

    public CouponSlotService(LockService lockService, CouponSlotTransactionExecutor executor) {
        this.lockService = lockService;
        this.executor = executor;
    }

    public CouponEventIssueResponse assignSlot(Long eventId, Long userId) {
        return lockService.withLock(
            COUPON_EVENT_LOCK_PREFIX + eventId,
            () -> executor.assignSlotInTransaction(eventId, userId)
        );
    }

    public CouponEventIssueResponse assignSlots(Long eventId, List<Long> userIds) {
        return lockService.withLock(
            COUPON_EVENT_LOCK_PREFIX + eventId,
            () -> executor.assignSlotsInTransaction(eventId, userIds)
        );
    }
}
