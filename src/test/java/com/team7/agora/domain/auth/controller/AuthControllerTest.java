// AuthController 인증 엔드포인트의 HTTP 계약을 검증하는 테스트
package com.team7.agora.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.auth.dto.request.LoginRequest;
import com.team7.agora.domain.auth.dto.request.SignupRequest;
import com.team7.agora.domain.auth.dto.response.LoginResponse;
import com.team7.agora.domain.auth.dto.response.ReissueResponse;
import com.team7.agora.domain.auth.dto.response.SignupResponse;
import com.team7.agora.domain.auth.service.AuthService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    private String signupJson(String email, String password, String nickname) {
        return signupJson(email, password, nickname, "01012345678");
    }

    private String signupJson(String email, String password, String nickname, String phone) {
        return """
                {"email":"%s","password":"%s","nickname":"%s","phone":"%s"}
                """.formatted(email, password, nickname, phone);
    }

    private String loginJson(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    private String reissueJson(String refreshToken) {
        return """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void signup_returns201WithSuccessBody() throws Exception {
        // given
        when(authService.signup(any(SignupRequest.class)))
                .thenReturn(new SignupResponse("user@test.com", "동네유저"));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("user@test.com", "password123!", "동네유저")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("동네유저"));
    }

    @Test
    void signup_returns400WhenEmailFormatInvalid() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("not-an-email", "password123!", "동네유저")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void signup_returns400WhenPhoneMissing() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("user@test.com", "password123!", "동네유저", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("휴대폰 번호는 필수입니다."));
    }

    @Test
    void signup_returns500WhenUnexpectedErrorOccurs() throws Exception {
        // given
        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("user@test.com", "password123!", "동네유저")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void signup_returns409WhenEmailDuplicated() throws Exception {
        // given
        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("user@test.com", "password123!", "동네유저")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    void login_returns200WithTokens() throws Exception {
        // given
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("Bearer access-token", "refresh-token"));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("user@test.com", "password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("Bearer access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void login_returns401WhenCredentialsInvalid() throws Exception {
        // given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("user@test.com", "wrongpassword!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void reissue_returns200WithNewTokens() throws Exception {
        // given
        when(authService.reissue(anyString()))
                .thenReturn(new ReissueResponse("Bearer new-access-token", "new-refresh-token"));

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reissueJson("old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("Bearer new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    void reissue_returns401WhenRefreshTokenInvalid() throws Exception {
        // given
        when(authService.reissue(anyString()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_TOKEN));

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reissueJson("unknown-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void logout_returns200AndDelegatesWithAuthenticatedUserId() throws Exception {
        // given
        CustomUserDetails principal = new CustomUserDetails(
                1L, "user@test.com", "encoded", UserRole.ROLE_USER, UserStatus.ACTIVE, "동네유저");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        // when & then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        verify(authService).logout(1L);
    }
}
