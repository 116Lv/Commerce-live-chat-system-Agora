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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/nego-offers")
public class NegoController {

    private final NegoService negoService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param negoService 입력 값
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
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param offerId 입력 값
     * @return 처리 결과
     */
    @RequestMapping(path = "/{offerId}/reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectOffer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::rejectOffer, "가격 제안을 거절했습니다.");
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param offerId 입력 값
     * @return 처리 결과
     */
    @RequestMapping(path = "/{offerId}/extension-request", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> requestExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::requestExtension, "가격 제안 연장을 요청했습니다.");
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param offerId 입력 값
     * @return 처리 결과
     */
    @RequestMapping(path = "/{offerId}/extension-approve", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> approveExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::approveExtension, "가격 제안 연장을 승인했습니다.");
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param offerId 입력 값
     * @return 처리 결과
     */
    @RequestMapping(path = "/{offerId}/extension-reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::rejectExtension, "가격 제안 연장을 거절했습니다.");
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param offerId 입력 값
     * @return 처리 결과
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
