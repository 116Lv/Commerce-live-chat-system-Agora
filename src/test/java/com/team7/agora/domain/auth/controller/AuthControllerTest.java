// AuthController 회원가입 엔드포인트의 HTTP 계약을 검증하는 테스트
package com.team7.agora.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.auth.dto.request.SignupRequest;
import com.team7.agora.domain.auth.dto.response.SignupResponse;
import com.team7.agora.domain.auth.service.AuthService;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    private String signupJson(String email, String password, String nickname) {
        return """
                {"email":"%s","password":"%s","nickname":"%s"}
                """.formatted(email, password, nickname);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
}
