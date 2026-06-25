package com.team7.agora.domain.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.service.ChatService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatRedisPublisher chatRedisPublisher;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatRoomController(chatService, chatRedisPublisher))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMessagesRejectsSizeLessThanOne() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/chat/rooms/{chatRoomId}/messages", 100L)
                .param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));

        verify(chatService, never()).getMessages(anyLong(), anyLong(), any(), anyInt());
    }

    @Test
    void getMessagesRejectsSizeGreaterThanFiveHundred() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/chat/rooms/{chatRoomId}/messages", 100L)
                .param("size", "501"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));

        verify(chatService, never()).getMessages(anyLong(), anyLong(), any(), anyInt());
    }

    private void authenticate() {
        CustomUserDetails principal = new CustomUserDetails(
            2L,
            "buyer@test.com",
            "encoded",
            UserRole.ROLE_USER,
            UserStatus.ACTIVE,
            "구매자"
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
