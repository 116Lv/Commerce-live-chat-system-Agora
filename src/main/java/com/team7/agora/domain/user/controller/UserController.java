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

/**
 * 회원 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userService 회원 비즈니스 로직을 처리하는 서비스
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 회원 정보를 조회하는 GET /api/users/me 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping
    public ApiResponse<UserMeResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("내 정보를 조회했습니다.", userService.getMe(userDetails.getUserId()));
    }

    /**
     * 회원 상태를 변경하는 PATCH /api/users/me/profile 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PatchMapping("/profile")
    public ApiResponse<UserMeResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        UserMeResponse response = userService.updateProfile(userDetails.getUserId(), request.nickname());
        return ApiResponse.success("프로필이 수정되었습니다.", response);
    }

    /**
     * 회원 상태를 변경하는 PATCH /api/users/me/password 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(userDetails.getUserId(), request.currentPassword(), request.newPassword());
        return ApiResponse.success("비밀번호가 변경되었습니다.", null);
    }
}
