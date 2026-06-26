package com.team7.agora.domain.nego.controller;

import com.team7.agora.domain.nego.dto.response.NegoOfferResponse;
import com.team7.agora.domain.nego.service.NegoService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.function.BiFunction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 가격 제안 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/nego-offers")
public class NegoController {

    private final NegoService negoService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param negoService 가격 제안 비즈니스 로직을 처리하는 서비스
     */
    public NegoController(NegoService negoService) {
        this.negoService = negoService;
    }

    @RequestMapping(path = "/{offerId}/accept", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> acceptOffer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::acceptOffer, "가격 제안을 수락했습니다.");
    }

    /**
     * 판매자가 구매자의 가격 제안을 거절 상태로 변경한다.
     * @param userDetails 인증된 사용자 정보
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @RequestMapping(path = "/{offerId}/reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectOffer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::rejectOffer, "가격 제안을 거절했습니다.");
    }

    /**
     * 구매자가 가격 제안의 응답 기한 연장을 요청한다.
     * @param userDetails 인증된 사용자 정보
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @RequestMapping(path = "/{offerId}/extension-request", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> requestExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::requestExtension, "가격 제안 연장을 요청했습니다.");
    }

    /**
     * 판매자가 가격 제안 연장 요청을 승인하고 만료 시간을 늘린다.
     * @param userDetails 인증된 사용자 정보
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @RequestMapping(path = "/{offerId}/extension-approve", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> approveExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::approveExtension, "가격 제안 연장을 승인했습니다.");
    }

    /**
     * 판매자가 가격 제안 연장 요청을 거절한다.
     * @param userDetails 인증된 사용자 정보
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @RequestMapping(path = "/{offerId}/extension-reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::rejectExtension, "가격 제안 연장을 거절했습니다.");
    }

    /**
     * 가격 제안 기능을 처리하는 POST /api/nego-offers/{offerId}/expire 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param offerId 대상 가격 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{offerId}/expire")
    public ApiResponse<NegoOfferResponse> expireOffer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        NegoOfferResponse response = negoService.expireOffer(userDetails.toAuthUser(), offerId);
        return ApiResponse.success("가격 제안이 만료 처리되었습니다.", response);
    }

    private ApiResponse<NegoOfferResponse> handleOfferAction(
        CustomUserDetails userDetails,
        Long offerId,
        BiFunction<Long, Long, NegoOfferResponse> action,
        String message
    ) {
        NegoOfferResponse response = action.apply(userDetails.getUserId(), offerId);
        return ApiResponse.success(message, response);
    }
}
