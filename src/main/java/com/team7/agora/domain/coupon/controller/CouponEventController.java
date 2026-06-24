package com.team7.agora.domain.coupon.controller;

import com.team7.agora.domain.coupon.dto.response.CouponEventResponse;
import com.team7.agora.domain.coupon.service.CouponQueryService;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupon-events")
public class CouponEventController {

    private final CouponQueryService couponQueryService;

    public CouponEventController(CouponQueryService couponQueryService) {
        this.couponQueryService = couponQueryService;
    }

    @GetMapping
    public ApiResponse<List<CouponEventResponse>> list() {
        return ApiResponse.success("진행중인 쿠폰 이벤트 목록입니다.", couponQueryService.listActiveEvents());
    }
}
