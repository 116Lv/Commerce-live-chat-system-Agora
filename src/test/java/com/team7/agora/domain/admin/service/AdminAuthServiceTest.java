// 관리자 인증 서비스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.request.AdminLoginRequest;
import com.team7.agora.domain.admin.dto.response.AdminLoginResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AccountType;
import com.team7.agora.global.auth.AdminPrincipal;
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
    private AdminRepository adminRepository;

    @Mock
    private JwtProvider jwtProvider;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AdminAuthService createService() {
        return new AdminAuthService(adminRepository, passwordEncoder, jwtProvider);
    }

    private Admin adminWithRole(AdminRole role) {
        Admin admin = Admin.create("admin@test.com", passwordEncoder.encode("password123!"), "관리자", role);
        ReflectionTestUtils.setField(admin, "id", 1L);
        return admin;
    }

    @Test
    void login_returnsAccessTokenForAdminAccount() {
        // given
        AdminAuthService service = createService();
        Admin admin = adminWithRole(AdminRole.ROOT_ADMIN);
        when(adminRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(jwtProvider.createToken(1L, "admin@test.com", "ROOT_ADMIN", "관리자", AccountType.ADMIN))
                .thenReturn("Bearer admin-token");

        // when
        AdminLoginResponse response = service.login(new AdminLoginRequest("admin@test.com", "password123!"));

        // then
        assertThat(response.accessToken()).isEqualTo("Bearer admin-token");
    }

    @Test
    void login_findsAdminByNormalizedEmail() {
        // given
        AdminAuthService service = createService();
        Admin admin = adminWithRole(AdminRole.ROOT_ADMIN);
        when(adminRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(jwtProvider.createToken(1L, "admin@test.com", "ROOT_ADMIN", "관리자", AccountType.ADMIN))
                .thenReturn("Bearer admin-token");

        // when
        AdminLoginResponse response = service.login(new AdminLoginRequest("  ADMIN@test.com  ", "password123!"));

        // then
        assertThat(response.accessToken()).isEqualTo("Bearer admin-token");
    }

    @Test
    void login_throwsUnauthorizedWhenPasswordMismatches() {
        // given
        AdminAuthService service = createService();
        when(adminRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(adminWithRole(AdminRole.ROOT_ADMIN)));

        // when & then
        assertThatThrownBy(() -> service.login(new AdminLoginRequest("admin@test.com", "wrongPassword")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(jwtProvider, never()).createToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void login_throwsUnauthorizedWhenAdminAccountMissing() {
        // given
        AdminAuthService service = createService();
        when(adminRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.login(new AdminLoginRequest("user@test.com", "password123!")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void login_throwsInactiveUserWhenAdminSuspended() {
        // given
        AdminAuthService service = createService();
        Admin admin = adminWithRole(AdminRole.ROOT_ADMIN);
        ReflectionTestUtils.setField(admin, "status", AdminStatus.SUSPENDED);
        when(adminRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));

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
    void logout_acceptsAdminPrincipal() {
        // given
        AdminAuthService service = createService();
        AdminPrincipal admin = new AdminPrincipal(
                1L, "admin@test.com", "encoded", AdminRole.USER_ADMIN, AdminStatus.ACTIVE, "관리자");

        // when & then
        assertThatCode(() -> service.logout(admin)).doesNotThrowAnyException();
    }
}
