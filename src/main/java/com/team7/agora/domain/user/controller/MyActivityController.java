package com.team7.agora.domain.user.controller;

import com.team7.agora.domain.trade.dto.request.MyTradeRole;
import com.team7.agora.domain.trade.dto.response.MyTradeResponse;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import com.team7.agora.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class MyActivityController {

    private final TradeService tradeService;

    public MyActivityController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/trades")
    public ApiResponse<PageResponse<MyTradeResponse>> getMyTrades(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String role,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<MyTradeResponse> responses = tradeService.getMyTrades(
            userDetails.getUserId(),
            MyTradeRole.from(role),
            PageRequest.of(page, size)
        );
        return ApiResponse.success("거래 목록을 조회했습니다.", PageResponse.from(responses));
    }
}