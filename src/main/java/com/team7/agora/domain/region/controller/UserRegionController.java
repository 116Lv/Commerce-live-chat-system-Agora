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

@RestController
public class UserRegionController {

    private final RegionService regionService;

    public UserRegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PutMapping("/api/users/me/regions")
    public ApiResponse<Void> updatePreferredRegions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PreferredRegionUpdateRequest request
    ) {
        regionService.updatePreferredRegions(userDetails.getUserId(), request);
        return ApiResponse.success("관심 지역이 변경되었습니다.", null);
    }
}
