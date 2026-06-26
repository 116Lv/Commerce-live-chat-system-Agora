// 인증/회원가입 REST 엔드포인트를 제공하는 컨트롤러
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
 * REST controller that exposes auth endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates a auth controller instance.
     * @param authService the auth service value
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles signup behavior.
     * @param request the request value
     * @return the signup result
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * Handles login behavior.
     * @param request the request value
     * @return the login result
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    /**
     * Handles logout behavior.
     * @param userDetails the user details value
     * @return the logout result
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

    /**
     * Handles reissue behavior.
     * @param request the request value
     * @return the reissue result
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        ReissueResponse response = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", response));
    }
}
