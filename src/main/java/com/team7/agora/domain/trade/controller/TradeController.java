package com.team7.agora.domain.trade.controller;

import com.team7.agora.domain.trade.dto.response.TradeDetailResponse;
import com.team7.agora.domain.trade.dto.response.TradeResponse;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 거래 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param tradeService 거래 비즈니스 로직을 처리하는 서비스
     */
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * 거래 정보를 조회하는 GET /api/trades/{tradeId} 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param tradeId 대상 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/{tradeId}")
    public ApiResponse<TradeDetailResponse> getDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long tradeId
    ) {
        TradeDetailResponse response = tradeService.getTradeDetail(userDetails.getUserId(), tradeId);
        return ApiResponse.success("거래 상세 조회가 완료되었습니다.", response);
    }

    /**
     * 거래을 확정하거나 진행하는 POST /api/trades/products/{productId} 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param productId 대상 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/products/{productId}")
    public ApiResponse<TradeResponse> start(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId
    ) {
        TradeResponse response = tradeService.startTrade(userDetails.getUserId(), productId);
        return ApiResponse.success("구매 신청이 완료되었습니다.", response);
    }

    /**
     * 거래을 확정하거나 진행하는 POST /api/trades/{tradeId}/complete 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param tradeId 대상 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{tradeId}/complete")
    public ApiResponse<TradeResponse> complete(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long tradeId
    ) {
        TradeResponse response = tradeService.completeTrade(userDetails.getUserId(), tradeId);
        return ApiResponse.success("구매 확정이 완료되었습니다. 판매자 정산이 시작됩니다.", response);
    }

    /**
     * 거래 기능을 처리하는 POST /api/trades/{tradeId}/expire-reservation 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param tradeId 대상 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{tradeId}/expire-reservation")
    public ApiResponse<TradeResponse> expireReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long tradeId
    ) {
        TradeResponse response = tradeService.expireReservation(userDetails.toAuthUser(), tradeId);
        return ApiResponse.success("예약이 만료되었습니다.", response);
    }

    /**
     * 거래 메시지를 전송하는 POST /api/trades/{tradeId}/rating-request-message 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param tradeId 대상 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{tradeId}/rating-request-message")
    public ApiResponse<Void> sendRatingRequestMessage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long tradeId
    ) {
        tradeService.sendRatingRequestMessage(userDetails.toAuthUser(), tradeId);
        return ApiResponse.success("평가 요청 메시지를 발송했습니다.", null);
    }
}
