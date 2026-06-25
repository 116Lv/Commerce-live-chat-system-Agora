// 관리자 인증 서비스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.request.AdminLoginRequest;
import com.team7.agora.domain.admin.dto.response.AdminLoginResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.auth.JwtProvider;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AdminAuthService createService() {
        return new AdminAuthService(userRepository, passwordEncoder, jwtProvider);
    }

    private User userWithRole(UserRole role) {
        User user = User.create("admin@test.com", passwordEncoder.encode("password123!"), "관리자");
        ReflectionTestUtils.setField(user, "id", 1L);
        user.changeRole(role);
        return user;
    }

    @Test
    void login_returnsAccessTokenForAdminAccount() {
        // given
        AdminAuthService service = createService();
        User admin = userWithRole(UserRole.ROOT_ADMIN);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(jwtProvider.createToken(1L, "admin@test.com", "ROOT_ADMIN", "관리자")).thenReturn("Bearer admin-token");

        // when
        AdminLoginResponse response = service.login(new AdminLoginRequest("admin@test.com", "password123!"));

        // then
        assertThat(response.accessToken()).isEqualTo("Bearer admin-token");
    }

    @Test
    void login_findsAdminByNormalizedEmail() {
        // given
        AdminAuthService service = createService();
        User admin = userWithRole(UserRole.ROOT_ADMIN);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(jwtProvider.createToken(1L, "admin@test.com", "ROOT_ADMIN", "관리자")).thenReturn("Bearer admin-token");

        // when
        AdminLoginResponse response = service.login(new AdminLoginRequest("  ADMIN@test.com  ", "password123!"));

        // then
        assertThat(response.accessToken()).isEqualTo("Bearer admin-token");
    }

    @Test
    void login_throwsUnauthorizedWhenPasswordMismatches() {
        // given
        AdminAuthService service = createService();
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(userWithRole(UserRole.ROOT_ADMIN)));

        // when & then
        assertThatThrownBy(() -> service.login(new AdminLoginRequest("admin@test.com", "wrongPassword")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(jwtProvider, never()).createToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void login_throwsForbiddenWhenAccountIsNotAdmin() {
        // given
        AdminAuthService service = createService();
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(userWithRole(UserRole.ROLE_USER)));

        // when & then
        assertThatThrownBy(() -> service.login(new AdminLoginRequest("user@test.com", "password123!")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void login_throwsInactiveUserWhenAdminSuspended() {
        // given
        AdminAuthService service = createService();
        User admin = userWithRole(UserRole.ROOT_ADMIN);
        admin.changeStatus(UserStatus.SUSPENDED);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));

        // when & then
        assertThatThrownBy(() -> service.login(new AdminLoginRequest("admin@test.com", "password123!")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INACTIVE_USER);
    }

    @Test
    void logout_throwsUnauthorizedWhenPrincipalMissing() {
        // given
        AdminAuthService service = createService();

        // when & then
        assertThatThrownBy(() -> service.logout(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void logout_throwsForbiddenWhenPrincipalIsNotAdmin() {
        // given
        AdminAuthService service = createService();
        CustomUserDetails user = new CustomUserDetails(
                1L, "user@test.com", "encoded", UserRole.ROLE_USER, UserStatus.ACTIVE, "일반사용자");

        // when & then
        assertThatThrownBy(() -> service.logout(user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
