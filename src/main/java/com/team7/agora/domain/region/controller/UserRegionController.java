// 로그인 사용자의 관심 지역 수정 API를 제공하는 컨트롤러
package com.team7.agora.domain.region.controller;

import com.team7.agora.domain.region.dto.request.PreferredRegionUpdateRequest;
import com.team7.agora.domain.region.service.RegionService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 선호 지역 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
public class UserRegionController {

    private final RegionService regionService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param regionService 지역 비즈니스 로직을 처리하는 서비스
     */
    public UserRegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * 사용자 선호 지역 상태를 변경하는 PUT /api/users/me/regions 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PutMapping("/api/users/me/regions")
    public ApiResponse<Void> updatePreferredRegions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PreferredRegionUpdateRequest request
    ) {
        regionService.updatePreferredRegions(userDetails.getUserId(), request);
        return ApiResponse.success("관심 지역이 변경되었습니다.", null);
    }
}
