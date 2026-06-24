// 내 정보 조회와 회원 셀프서비스 API를 제공하는 컨트롤러
package com.team7.agora.domain.user.controller;

import com.team7.agora.domain.user.dto.request.PasswordChangeRequest;
import com.team7.agora.domain.user.dto.request.ProfileUpdateRequest;
import com.team7.agora.domain.user.dto.response.UserMeResponse;
import com.team7.agora.domain.user.service.UserService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<UserMeResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("내 정보를 조회했습니다.", userService.getMe(userDetails.getUserId()));
    }

    @PatchMapping("/profile")
    public ApiResponse<UserMeResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        UserMeResponse response = userService.updateProfile(userDetails.getUserId(), request.nickname());
        return ApiResponse.success("프로필이 수정되었습니다.", response);
    }

    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(userDetails.getUserId(), request.currentPassword(), request.newPassword());
        return ApiResponse.success("비밀번호가 변경되었습니다.", null);
    }
}
