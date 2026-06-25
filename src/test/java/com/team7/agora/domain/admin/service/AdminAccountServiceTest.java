// 관리자 계정 권한 변경 서비스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
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
class AdminAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    private AdminAccountService createService() {
        return new AdminAccountService(userRepository);
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(99L, "root@admin.com", "encoded", role, UserStatus.ACTIVE, "최고관리자");
    }

    @Test
    void changeRole_updatesUserRoleWhenRootAdminRequestsAdminRole() {
        // given
        AdminAccountService service = createService();
        User user = User.create("target@test.com", "encoded", "대상관리자");
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        AdminUserResponse response = service.changeRole(principal(UserRole.ROOT_ADMIN), 1L, UserRole.PRODUCT_ADMIN);

        // then
        assertThat(response.role()).isEqualTo("PRODUCT_ADMIN");
        assertThat(user.getRole()).isEqualTo(UserRole.PRODUCT_ADMIN);
    }

    @Test
    void changeRole_throwsForbiddenWhenRequesterIsNotRootAdmin() {
        // given
        AdminAccountService service = createService();

        // when & then
        assertThatThrownBy(() -> service.changeRole(principal(UserRole.USER_ADMIN), 1L, UserRole.PRODUCT_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void changeRole_throwsInvalidRequestWhenTargetRoleIsUser() {
        // given
        AdminAccountService service = createService();

        // when & then
        assertThatThrownBy(() -> service.changeRole(principal(UserRole.ROOT_ADMIN), 1L, UserRole.ROLE_USER))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }
}
