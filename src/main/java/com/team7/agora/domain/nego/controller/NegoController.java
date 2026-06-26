package com.team7.agora.domain.nego.controller;

import com.team7.agora.domain.nego.dto.request.NegoOfferCreateRequest;
import com.team7.agora.domain.nego.dto.response.NegoOfferResponse;
import com.team7.agora.domain.nego.service.NegoService;
import com.team7.agora.global.auth.AuthUser;
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

/**
 * REST controller that exposes nego endpoints.
 */
@RestController
@RequestMapping("/api/nego-offers")
public class NegoController {

    private final NegoService negoService;

    /**
     * Creates a nego controller instance.
     * @param negoService the nego service value
     */
    public NegoController(NegoService negoService) {
        this.negoService = negoService;
    }

    /**
     * Creates offer data.
     * @param authUser the auth user value
     * @param chatRoomId the chat room id value
     * @param request the request value
     * @return the create offer result
     */
    @PostMapping("/chat-rooms/{chatRoomId}")
    public ApiResponse<NegoOfferResponse> createOffer(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long chatRoomId,
        @Valid @RequestBody NegoOfferCreateRequest request
    ) {
        NegoOfferResponse response = negoService.createOffer(
            authUser.userId(),
            chatRoomId,
            request.offerPrice()
        );
        return ApiResponse.success("가격 제안이 등록되었습니다.", response);
    }

    /**
     * Handles accept offer behavior.
     * @param authUser the auth user value
     * @param offerId the offer id value
     * @return the accept offer result
     */
    @RequestMapping(path = "/{offerId}/accept", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> acceptOffer(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(authUser, offerId, negoService::acceptOffer, "가격 제안을 수락했습니다.");
    }

    /**
     * Handles reject offer behavior.
     * @param authUser the auth user value
     * @param offerId the offer id value
     * @return the reject offer result
     */
    @RequestMapping(path = "/{offerId}/reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectOffer(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(authUser, offerId, negoService::rejectOffer, "가격 제안을 거절했습니다.");
    }

    /**
     * Handles request extension behavior.
     * @param authUser the auth user value
     * @param offerId the offer id value
     * @return the request extension result
     */
    @RequestMapping(path = "/{offerId}/extension-request", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> requestExtension(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(authUser, offerId, negoService::requestExtension, "가격 제안 연장을 요청했습니다.");
    }

    /**
     * Handles approve extension behavior.
     * @param authUser the auth user value
     * @param offerId the offer id value
     * @return the approve extension result
     */
    @RequestMapping(path = "/{offerId}/extension-approve", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> approveExtension(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(authUser, offerId, negoService::approveExtension, "가격 제안 연장을 승인했습니다.");
    }

    /**
     * Handles reject extension behavior.
     * @param authUser the auth user value
     * @param offerId the offer id value
     * @return the reject extension result
     */
    @RequestMapping(path = "/{offerId}/extension-reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ApiResponse<NegoOfferResponse> rejectExtension(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long offerId
    ) {
        return handleOfferAction(authUser, offerId, negoService::rejectExtension, "가격 제안 연장을 거절했습니다.");
    }

    /**
     * Handles expire offer behavior.
     * @param authUser the auth user value
     * @param offerId the offer id value
     * @return the expire offer result
     */
    @PostMapping("/{offerId}/expire")
    public ApiResponse<NegoOfferResponse> expireOffer(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long offerId
    ) {
        NegoOfferResponse response = negoService.expireOffer(authUser, offerId);
        return ApiResponse.success("가격 제안이 만료 처리되었습니다.", response);
    }

    private ApiResponse<NegoOfferResponse> handleOfferAction(
        AuthUser authUser,
        Long offerId,
        BiFunction<Long, Long, NegoOfferResponse> action,
        String message
    ) {
        NegoOfferResponse response = action.apply(authUser.userId(), offerId);
        return ApiResponse.success(message, response);
    }
}
