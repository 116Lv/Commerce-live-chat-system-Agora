// AuthService의 회원가입/로그인/로그아웃/토큰 재발급 분기를 검증하는 단위 테스트
package com.team7.agora.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.auth.dto.request.LoginRequest;
import com.team7.agora.domain.auth.dto.request.SignupRequest;
import com.team7.agora.domain.auth.dto.response.LoginResponse;
import com.team7.agora.domain.auth.dto.response.ReissueResponse;
import com.team7.agora.domain.auth.dto.response.SignupResponse;
import com.team7.agora.domain.auth.entity.RefreshToken;
import com.team7.agora.domain.auth.repository.RefreshTokenRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.JwtProvider;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long REFRESH_TOKEN_VALID_TIME = 1_209_600_000L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider =
            new JwtProvider("authServiceTestSecretKeyForAgoraProject12345!", 3_600_000L);

    private AuthService createService() {
        return new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtProvider,
                REFRESH_TOKEN_VALID_TIME
        );
    }

    private User userWithId(long id, String email, String rawPassword) {
        User user = User.create(email, passwordEncoder.encode(rawPassword), "동네유저");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void signup_savesEncodedPasswordWithDefaultRoleAndStatus() {
        // given
        AuthService authService = createService();
        SignupRequest request = new SignupRequest("user@test.com", "password123!", "동네유저");
        when(userRepository.existsByEmailIgnoreCase("user@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SignupResponse response = authService.signup(request);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("동네유저");
        assertThat(savedUser.getPassword()).isNotEqualTo("password123!");
        assertThat(passwordEncoder.matches("password123!", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.nickname()).isEqualTo("동네유저");
    }

    @Test
    void signup_throwsDuplicateEmailWhenEmailAlreadyExists() {
        // given
        AuthService authService = createService();
        SignupRequest request = new SignupRequest("user@test.com", "password123!", "동네유저");
        when(userRepository.existsByEmailIgnoreCase("user@test.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_throwsDuplicateEmailIgnoringCase() {
        // given
        AuthService authService = createService();
        SignupRequest request = new SignupRequest("USER@test.com", "password123!", "동네유저");
        when(userRepository.existsByEmailIgnoreCase("user@test.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_mapsUniqueConstraintViolationToDuplicateEmail() {
        // given
        AuthService authService = createService();
        SignupRequest request = new SignupRequest("user@test.com", "password123!", "동네유저");
        when(userRepository.existsByEmailIgnoreCase("user@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("users.email"));

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    void signup_savesNormalizedLowercaseEmail() {
        // given
        AuthService authService = createService();
        SignupRequest request = new SignupRequest("  USER@test.com  ", "password123!", "동네유저");
        when(userRepository.existsByEmailIgnoreCase("user@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SignupResponse response = authService.signup(request);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("user@test.com");
        assertThat(response.email()).isEqualTo("user@test.com");
    }

    @Test
    void login_issuesAccessAndRefreshTokenWhenCredentialsMatch() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        LoginResponse response = authService.login(new LoginRequest("user@test.com", "password123!"));

        // then
        assertThat(response.accessToken()).startsWith("Bearer ");
        assertThat(response.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_findsUserByNormalizedEmail() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        LoginResponse response = authService.login(new LoginRequest("  USER@test.com  ", "password123!"));

        // then
        assertThat(response.accessToken()).startsWith("Bearer ");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_throwsInvalidCredentialsWhenEmailNotFound() {
        // given
        AuthService authService = createService();
        when(userRepository.findByEmailIgnoreCase("none@test.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest("none@test.com", "password123!")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void login_throwsInvalidCredentialsWhenPasswordMismatch() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "wrongpassword!")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void login_throwsInactiveUserWhenUserSuspended() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        user.changeStatus(UserStatus.SUSPENDED);
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "password123!")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INACTIVE_USER);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void logout_deletesRefreshTokenByUserId() {
        // given
        AuthService authService = createService();

        // when
        authService.logout(1L);

        // then
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    void reissue_rotatesTokenWhenRefreshTokenValid() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        RefreshToken stored = RefreshToken.issue(user, "old-refresh-token", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReissueResponse response = authService.reissue("old-refresh-token");

        // then
        assertThat(response.accessToken()).startsWith("Bearer ");
        assertThat(response.refreshToken()).isNotBlank().isNotEqualTo("old-refresh-token");
        verify(refreshTokenRepository).delete(stored);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_issuesRefreshTokenExpirationByUtcClock() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        authService.login(new LoginRequest("user@test.com", "password123!"));

        // then
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        LocalDateTime expectedEarliest = LocalDateTime.now(ZoneOffset.UTC)
                .plusNanos(REFRESH_TOKEN_VALID_TIME * 1_000_000L)
                .minusSeconds(5);
        LocalDateTime expectedLatest = LocalDateTime.now(ZoneOffset.UTC)
                .plusNanos(REFRESH_TOKEN_VALID_TIME * 1_000_000L)
                .plusSeconds(5);
        assertThat(captor.getValue().getExpiresAt()).isBetween(expectedEarliest, expectedLatest);
    }

    @Test
    void reissue_throwsInvalidTokenWhenRefreshTokenNotFound() {
        // given
        AuthService authService = createService();
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue("unknown-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void reissue_throwsExpiredTokenAndDeletesWhenRefreshTokenExpired() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        RefreshToken expired = RefreshToken.issue(user, "expired-token", LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        // when & then
        assertThatThrownBy(() -> authService.reissue("expired-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPIRED_TOKEN);
        verify(refreshTokenRepository).delete(expired);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void reissue_throwsInactiveUserAndDeletesRefreshTokenWhenUserSuspended() {
        // given
        AuthService authService = createService();
        User user = userWithId(1L, "user@test.com", "password123!");
        user.changeStatus(UserStatus.SUSPENDED);
        RefreshToken stored = RefreshToken.issue(user, "old-refresh-token", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(stored));

        // when & then
        assertThatThrownBy(() -> authService.reissue("old-refresh-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INACTIVE_USER);
        verify(refreshTokenRepository).delete(stored);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}
