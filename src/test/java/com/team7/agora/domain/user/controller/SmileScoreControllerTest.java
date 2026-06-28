// 사용자 스마일지수 HTTP 계약을 검증하는 컨트롤러 테스트
package com.team7.agora.domain.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.user.dto.response.SmileScoreResponse;
import com.team7.agora.domain.user.service.UserService;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SmileScoreControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SmileScoreController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSmileScore_keepsExistingResponseFormatForAllowedUser() throws Exception {
        // given
        when(userService.getSmileScore(2L))
                .thenReturn(new SmileScoreResponse(2L, 72));

        // when & then
        mockMvc.perform(get("/api/users/2/smile-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("스마일지수 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(2L))
                .andExpect(jsonPath("$.data.smileScore").value(72));
    }
}
