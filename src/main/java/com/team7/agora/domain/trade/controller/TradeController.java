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

/**
 * REST controller that exposes trade endpoints.
 */
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    /**
     * Creates a trade controller instance.
     * @param tradeService the trade service value
     */
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * Returns detail data.
     * @param authUser the auth user value
     * @param tradeId the trade id value
     * @return the get detail result
     */
    @GetMapping("/{tradeId}")
    public ApiResponse<TradeDetailResponse> getDetail(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        TradeDetailResponse response = tradeService.getTradeDetail(authUser.userId(), tradeId);
        return ApiResponse.success("거래 상세 조회가 완료되었습니다.", response);
    }

    /**
     * Handles start behavior.
     * @param authUser the auth user value
     * @param productId the product id value
     * @return the start result
     */
    @PostMapping("/products/{productId}")
    public ApiResponse<TradeResponse> start(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long productId
    ) {
        TradeResponse response = tradeService.startTrade(authUser.userId(), productId);
        return ApiResponse.success("구매 신청이 완료되었습니다.", response);
    }

    /**
     * Handles complete behavior.
     * @param authUser the auth user value
     * @param tradeId the trade id value
     * @return the complete result
     */
    @PostMapping("/{tradeId}/complete")
    public ApiResponse<TradeResponse> complete(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        TradeResponse response = tradeService.completeTrade(authUser.userId(), tradeId);
        return ApiResponse.success("구매 확정이 완료되었습니다. 판매자 정산이 시작됩니다.", response);
    }

    /**
     * Handles expire reservation behavior.
     * @param authUser the auth user value
     * @param tradeId the trade id value
     * @return the expire reservation result
     */
    @PostMapping("/{tradeId}/expire-reservation")
    public ApiResponse<TradeResponse> expireReservation(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        TradeResponse response = tradeService.expireReservation(authUser, tradeId);
        return ApiResponse.success("예약이 만료되었습니다.", response);
    }

    /**
     * Handles send rating request message behavior.
     * @param authUser the auth user value
     * @param tradeId the trade id value
     * @return the send rating request message result
     */
    @PostMapping("/{tradeId}/rating-request-message")
    public ApiResponse<Void> sendRatingRequestMessage(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        tradeService.sendRatingRequestMessage(authUser, tradeId);
        return ApiResponse.success("평가 요청 메시지를 발송했습니다.", null);
    }
}
