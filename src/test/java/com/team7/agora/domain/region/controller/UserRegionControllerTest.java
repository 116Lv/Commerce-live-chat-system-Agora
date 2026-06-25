// 관심 지역 수정 HTTP 계약을 검증하는 컨트롤러 테스트
package com.team7.agora.domain.region.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.region.dto.request.PreferredRegionUpdateRequest;
import com.team7.agora.domain.region.service.RegionService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserRegionControllerTest {

    @Mock
    private RegionService regionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserRegionController(regionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate() {
        CustomUserDetails principal = new CustomUserDetails(
                1L, "user@test.com", "encoded", UserRole.ROLE_USER, UserStatus.ACTIVE, "동네유저");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    void updatePreferredRegions_usesAuthenticatedUserId() throws Exception {
        // given
        authenticate();

        // when & then
        mockMvc.perform(put("/api/users/me/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"regionIds":[1,2,3],"primaryRegionId":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        verify(regionService).updatePreferredRegions(
                1L,
                new PreferredRegionUpdateRequest(java.util.List.of(1L, 2L, 3L), 1L)
        );
    }

    @Test
    void updatePreferredRegions_returns400WhenRegionIdsEmpty() throws Exception {
        // given
        authenticate();

        // when & then
        mockMvc.perform(put("/api/users/me/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"regionIds":[],"primaryRegionId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}
