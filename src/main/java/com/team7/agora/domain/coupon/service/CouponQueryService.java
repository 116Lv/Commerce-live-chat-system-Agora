package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventResponse;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponQueryService {

    private final CouponEventRepository couponEventRepository;

    public CouponQueryService(CouponEventRepository couponEventRepository) {
        this.couponEventRepository = couponEventRepository;
    }

    public List<CouponEventResponse> listActiveEvents() {
        return couponEventRepository.findAllByStatus(CouponEventStatus.ACTIVE).stream()
            .map(CouponEventResponse::from)
            .toList();
    }
}
