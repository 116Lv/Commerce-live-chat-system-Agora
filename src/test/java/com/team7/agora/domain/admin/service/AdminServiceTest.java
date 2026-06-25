// 관리자 정보와 대시보드 서비스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    private AdminService createService() {
        return new AdminService(userRepository);
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(1L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "관리자");
    }

    @Test
    void getMe_returnsAdminAccountInfo() {
        // given
        AdminService service = createService();
        User admin = User.create("admin@test.com", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", 1L);
        admin.changeRole(UserRole.ROOT_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        // when
        AdminMeResponse response = service.getMe(principal(UserRole.ROOT_ADMIN));

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("admin@test.com");
        assertThat(response.role()).isEqualTo("ROOT_ADMIN");
    }

    @Test
    void getDashboard_returnsMenusScopedToUserAdmin() {
        // given
        AdminService service = createService();

        // when
        AdminDashboardResponse response = service.getDashboard(principal(UserRole.USER_ADMIN));

        // then
        assertThat(response.role()).isEqualTo("USER_ADMIN");
        assertThat(response.accessibleMenus()).containsExactly("USERS", "USER_REPORTS");
    }

    @Test
    void getDashboard_throwsForbiddenForRegularUser() {
        // given
        AdminService service = createService();

        // when & then
        assertThatThrownBy(() -> service.getDashboard(principal(UserRole.ROLE_USER)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
