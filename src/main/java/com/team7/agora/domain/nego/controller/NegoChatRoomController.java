// 채팅방 기준 네고 API 계약을 기존 네고 서비스에 연결하는 컨트롤러입니다.
package com.team7.agora.domain.nego.controller;

import com.team7.agora.domain.nego.dto.request.NegoOfferCreateRequest;
import com.team7.agora.domain.nego.dto.response.NegoOfferResponse;
import com.team7.agora.domain.nego.service.NegoService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes nego chat room endpoints.
 */
@RestController
@RequestMapping("/api/chat/rooms")
public class NegoChatRoomController {

    private final NegoService negoService;

    /**
     * Creates a nego chat room controller instance.
     * @param negoService the nego service value
     */
    public NegoChatRoomController(NegoService negoService) {
        this.negoService = negoService;
    }

    /**
     * Creates offer data.
     * @param authUser the auth user value
     * @param chatRoomId the chat room id value
     * @param request the request value
     * @return the create offer result
     */
    @PostMapping("/{chatRoomId}/nego-offers")
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
}
