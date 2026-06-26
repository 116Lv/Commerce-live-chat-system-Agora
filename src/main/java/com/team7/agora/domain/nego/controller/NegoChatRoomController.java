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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/chat/rooms")
public class NegoChatRoomController {

    private final NegoService negoService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param negoService 입력 값
     */
    public NegoChatRoomController(NegoService negoService) {
        this.negoService = negoService;
    }

    /**
     * 도메인 객체를 생성한다.
     * @param userDetails 입력 값
     * @param chatRoomId 입력 값
     * @param request 입력 값
     * @return 처리 결과
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
