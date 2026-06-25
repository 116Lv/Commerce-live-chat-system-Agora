package com.team7.agora.domain.trade.controller;

import com.team7.agora.domain.trade.dto.response.TradeDetailResponse;
import com.team7.agora.domain.trade.dto.response.TradeResponse;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/{tradeId}")
    public ApiResponse<TradeDetailResponse> getDetail(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        TradeDetailResponse response = tradeService.getTradeDetail(authUser.userId(), tradeId);
        return ApiResponse.success("거래 상세 조회가 완료되었습니다.", response);
    }

    @PostMapping("/products/{productId}")
    public ApiResponse<TradeResponse> start(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long productId
    ) {
        TradeResponse response = tradeService.startTrade(authUser.userId(), productId);
        return ApiResponse.success("구매 신청이 완료되었습니다.", response);
    }

    @PostMapping("/{tradeId}/complete")
    public ApiResponse<TradeResponse> complete(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        TradeResponse response = tradeService.completeTrade(authUser.userId(), tradeId);
        return ApiResponse.success("구매 확정이 완료되었습니다. 판매자 정산이 시작됩니다.", response);
    }

    @PostMapping("/{tradeId}/expire-reservation")
    public ApiResponse<TradeResponse> expireReservation(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        TradeResponse response = tradeService.expireReservation(authUser, tradeId);
        return ApiResponse.success("예약이 만료되었습니다.", response);
    }

    @PostMapping("/{tradeId}/rating-request-message")
    public ApiResponse<Void> sendRatingRequestMessage(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        tradeService.sendRatingRequestMessage(authUser, tradeId);
        return ApiResponse.success("평가 요청 메시지를 발송했습니다.", null);
    }
}
