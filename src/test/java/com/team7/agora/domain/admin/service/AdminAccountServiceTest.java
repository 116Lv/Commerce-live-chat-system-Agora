package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceTest {

    @Mock
    private AdminRepository adminRepository;

    private AdminAccountService createService() {
        return new AdminAccountService(adminRepository);
    }

    private AdminPrincipal principal(AdminRole role) {
        return new AdminPrincipal(99L, "root@admin.com", "encoded", role, AdminStatus.ACTIVE, "root-admin");
    }

    @Test
    void changeRole_rejectsDirectRootRoleChangeBecauseApprovalIsRequired() {
        AdminAccountService service = createService();

        assertThatThrownBy(() -> service.changeRole(principal(AdminRole.ROOT_ADMIN), 1L, AdminRole.PRODUCT_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void changeRole_throwsForbiddenWhenRequesterIsNotRootAdmin() {
        AdminAccountService service = createService();

        assertThatThrownBy(() -> service.changeRole(principal(AdminRole.USER_ADMIN), 1L, AdminRole.PRODUCT_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void changeRole_throwsConflictWhenTargetAlreadyHasRole() {
        AdminAccountService service = createService();

        assertThatThrownBy(() -> service.changeRole(principal(AdminRole.ROOT_ADMIN), 1L, AdminRole.PRODUCT_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void getAccounts_returnsAdminAccountsForRootAdminOnly() {
        AdminAccountService service = createService();
        Admin admin = Admin.create("target@test.com", "encoded", "account-admin", AdminRole.USER_ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(adminRepository.findAll()).thenReturn(List.of(admin));

        List<AdminUserResponse> responses = service.getAccounts(principal(AdminRole.ROOT_ADMIN));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).email()).isEqualTo("target@test.com");
        assertThat(responses.get(0).role()).isEqualTo("USER_ADMIN");
    }

    @Test
    void getAccounts_throwsForbiddenWhenRequesterIsNotRootAdmin() {
        AdminAccountService service = createService();

        assertThatThrownBy(() -> service.getAccounts(principal(AdminRole.USER_ADMIN)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void changeRole_throwsConflictWhenRootAdminDemotesSelf() {
        AdminAccountService service = createService();

        assertThatThrownBy(() -> service.changeRole(principal(AdminRole.ROOT_ADMIN), 99L, AdminRole.USER_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void changeRole_throwsConflictWhenDemotingLastRootAdmin() {
        AdminAccountService service = createService();

        assertThatThrownBy(() -> service.changeRole(principal(AdminRole.ROOT_ADMIN), 1L, AdminRole.PRODUCT_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
