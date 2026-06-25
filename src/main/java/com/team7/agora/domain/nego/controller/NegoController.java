package com.team7.agora.domain.nego.controller;

import com.team7.agora.domain.nego.dto.request.NegoOfferCreateRequest;
import com.team7.agora.domain.nego.dto.response.NegoOfferResponse;
import com.team7.agora.domain.nego.service.NegoService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.function.BiFunction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nego-offers")
public class NegoController {

    private final NegoService negoService;

    public NegoController(NegoService negoService) {
        this.negoService = negoService;
    }

    @PostMapping("/chat-rooms/{chatRoomId}")
    public ApiResponse<NegoOfferResponse> createOffer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long chatRoomId,
        @Valid @RequestBody NegoOfferCreateRequest request
    ) {
        NegoOfferResponse response = negoService.createOffer(
            userDetails.getUserId(),
            chatRoomId,
            request.offerPrice()
        );
        return ApiResponse.success("가격 제안이 등록되었습니다.", response);
    }

    @RequestMapping(path = "/{offerId}/accept", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> acceptOffer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::acceptOffer, "가격 제안을 수락했습니다.");
    }

    @RequestMapping(path = "/{offerId}/reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectOffer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::rejectOffer, "가격 제안을 거절했습니다.");
    }

    @RequestMapping(path = "/{offerId}/extension-request", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> requestExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::requestExtension, "가격 제안 연장을 요청했습니다.");
    }

    @RequestMapping(path = "/{offerId}/extension-approve", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> approveExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::approveExtension, "가격 제안 연장을 승인했습니다.");
    }

    @RequestMapping(path = "/{offerId}/extension-reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectExtension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(userDetails, offerId, negoService::rejectExtension, "가격 제안 연장을 거절했습니다.");
    }

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
