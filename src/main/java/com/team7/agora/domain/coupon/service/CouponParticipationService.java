package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponParticipationService {

    private final CouponEventRepository couponEventRepository;
    private final CouponSlotService couponSlotService;

    public CouponParticipationService(CouponEventRepository couponEventRepository, CouponSlotService couponSlotService) {
        this.couponEventRepository = couponEventRepository;
        this.couponSlotService = couponSlotService;
    }

    public CouponEventIssueResponse participate(Long userId, Long eventId) {
        CouponEvent event = couponEventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 이벤트를 찾을 수 없습니다."));
        if (event.getType() == CouponEventType.ADMIN_INDIVIDUAL) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 전용 쿠폰 이벤트입니다.");
        }
        return couponSlotService.assignSlot(eventId, userId);
    }
}
