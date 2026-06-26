// 채팅방 기준 네고 API 계약을 기존 네고 서비스에 연결하는 컨트롤러입니다.
package com.team7.agora.domain.nego.controller;

import com.team7.agora.domain.nego.dto.request.NegoOfferCreateRequest;
import com.team7.agora.domain.nego.dto.response.NegoOfferResponse;
import com.team7.agora.domain.nego.service.NegoService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅방 가격 제안 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/chat/rooms")
public class NegoChatRoomController {

    private final NegoService negoService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param negoService 가격 제안 비즈니스 로직을 처리하는 서비스
     */
    public NegoChatRoomController(NegoService negoService) {
        this.negoService = negoService;
    }

    /**
     * 채팅방 가격 제안 정보를 생성하거나 준비하는 POST /api/chat/rooms/{chatRoomId}/nego-offers 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param chatRoomId 대상 채팅방 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{chatRoomId}/nego-offers")
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
}
