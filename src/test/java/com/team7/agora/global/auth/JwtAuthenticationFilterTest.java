// JwtAuthenticationFilter의 인증/실패 분기를 검증하는 단위 테스트
package com.team7.agora.global.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "localEnvDummySecretKeyForAgoraProjectTest12345!";

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 3_600_000L);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private JwtAuthenticationFilter filterWith(UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtProvider, userDetailsService);
    }

    private UserDetailsService userServiceWithStatus(UserStatus status) {
        return email -> new CustomUserDetails(1L, email, "encoded", UserRole.ROLE_USER, status, "닉네임");
    }

    private boolean[] chainCalled() {
        return new boolean[1];
    }

    private FilterChain recordingChain(boolean[] called) {
        return (req, res) -> called[0] = true;
    }

    @Test
    void validToken_setsAuthenticationAndContinues() throws Exception {
        // given
        JwtAuthenticationFilter filter = filterWith(userServiceWithStatus(UserStatus.ACTIVE));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION,
            jwtProvider.createToken(1L, "user@test.com", "ROLE_USER", "닉네임"));
        boolean[] called = chainCalled();

        // when
        filter.doFilter(request, response, recordingChain(called));

        // then
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(principal).isInstanceOf(CustomUserDetails.class);
        assertThat(((CustomUserDetails) principal).getEmail()).isEqualTo("user@test.com");
        assertThat(called[0]).isTrue();
    }

    @Test
    void noAuthorizationHeader_passesThroughWithoutAuthentication() throws Exception {
        // given
        JwtAuthenticationFilter filter = filterWith(userServiceWithStatus(UserStatus.ACTIVE));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] called = chainCalled();

        // when
        filter.doFilter(request, response, recordingChain(called));

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(called[0]).isTrue();
    }

    @Test
    void malformedHeader_returns401() throws Exception {
        // given
        JwtAuthenticationFilter filter = filterWith(userServiceWithStatus(UserStatus.ACTIVE));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, "wrong-token");
        boolean[] called = chainCalled();

        // when
        filter.doFilter(request, response, recordingChain(called));

        // then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(called[0]).isFalse();
    }

    @Test
    void expiredToken_returns401() throws Exception {
        // given
        JwtProvider expiredProvider = new JwtProvider(SECRET, -1_000L);
        JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(expiredProvider, userServiceWithStatus(UserStatus.ACTIVE));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION,
            expiredProvider.createToken(1L, "user@test.com", "ROLE_USER", "닉네임"));
        boolean[] called = chainCalled();

        // when
        filter.doFilter(request, response, recordingChain(called));

        // then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(called[0]).isFalse();
    }

    @Test
    void suspendedUser_returns403() throws Exception {
        // given
        JwtAuthenticationFilter filter = filterWith(userServiceWithStatus(UserStatus.SUSPENDED));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION,
            jwtProvider.createToken(1L, "user@test.com", "ROLE_USER", "닉네임"));
        boolean[] called = chainCalled();

        // when
        filter.doFilter(request, response, recordingChain(called));

        // then
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(called[0]).isFalse();
    }

    @Test
    void unknownUser_returns401() throws Exception {
        // given
        UserDetailsService throwing = email -> {
            throw new UsernameNotFoundException(email);
        };
        JwtAuthenticationFilter filter = filterWith(throwing);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION,
            jwtProvider.createToken(1L, "ghost@test.com", "ROLE_USER", "닉네임"));
        boolean[] called = chainCalled();

        // when
        filter.doFilter(request, response, recordingChain(called));

        // then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(called[0]).isFalse();
    }
}
