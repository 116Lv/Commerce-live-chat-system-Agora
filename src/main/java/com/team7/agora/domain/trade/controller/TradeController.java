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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param tradeService 입력 값
     */
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @param tradeId 입력 값
     * @return 처리 결과
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
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param productId 입력 값
     * @return 처리 결과
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
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param tradeId 입력 값
     * @return 처리 결과
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
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param tradeId 입력 값
     * @return 처리 결과
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
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param tradeId 입력 값
     * @return 처리 결과
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
