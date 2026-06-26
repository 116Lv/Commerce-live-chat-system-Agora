package com.team7.agora.domain.auth.controller;

import com.team7.agora.domain.auth.dto.request.LoginRequest;
import com.team7.agora.domain.auth.dto.request.ReissueRequest;
import com.team7.agora.domain.auth.dto.request.SignupRequest;
import com.team7.agora.domain.auth.dto.response.LoginResponse;
import com.team7.agora.domain.auth.dto.response.ReissueResponse;
import com.team7.agora.domain.auth.dto.response.SignupResponse;
import com.team7.agora.domain.auth.service.AuthService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param authService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 인증 정보를 생성하거나 준비하는 POST /api/auth/signup 요청을 처리한다.
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 인증 로그인을 처리하는 POST /api/auth/login 요청을 처리한다.
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    /**
     * 인증 로그아웃을 처리하는 POST /api/auth/logout 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

    /**
     * 인증 토큰을 재발급하는 POST /api/auth/reissue 요청을 처리한다.
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        ReissueResponse response = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", response));
    }
}
