package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void getUsers_returnsUserList() {
        AdminUserService service = new AdminUserService(userRepository);
        User user = User.signup("user@test.com", "encoded", "동네유저", "01011112222");
        assignId(user, 1L);
        when(userRepository.findAllByDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 20), 42));

        Page<AdminUserResponse> responses = service.getUsers(principal(UserRole.USER_ADMIN), PageRequest.of(0, 20));

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).id()).isEqualTo(1L);
        assertThat(responses.getContent().get(0).email()).isEqualTo("user@test.com");
        assertThat(responses.getContent().get(0).status()).isEqualTo("ACTIVE");
        assertThat(responses.getTotalElements()).isEqualTo(42);
        assertThat(responses.getTotalPages()).isEqualTo(3);
    }

    @Test
    void getUsers_rejectsNonUserAdmin() {
        AdminUserService service = new AdminUserService(userRepository);

        assertThatThrownBy(() -> service.getUsers(principal(UserRole.PRODUCT_ADMIN), PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void changeStatus_updatesUserStatus() {
        AdminUserService service = new AdminUserService(userRepository);
        User user = User.signup("user@test.com", "encoded", "동네유저", "01011112222");
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserResponse response = service.changeStatus(principal(UserRole.USER_ADMIN), 1L, UserStatus.BLOCKED);

        assertThat(response.status()).isEqualTo("BLOCKED");
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void changeStatus_rejectsUserAdminWhenTargetIsAdminAccount() {
        AdminUserService service = new AdminUserService(userRepository);
        User user = User.signup("admin@test.com", "encoded", "관리자", "01011112222");
        user.changeRole(UserRole.ROOT_ADMIN);
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changeStatus(principal(UserRole.USER_ADMIN), 1L, UserStatus.BLOCKED))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void changeStatus_allowsRootAdminWhenTargetIsAdminAccount() {
        AdminUserService service = new AdminUserService(userRepository);
        User user = User.signup("admin@test.com", "encoded", "관리자", "01011112222");
        user.changeRole(UserRole.USER_ADMIN);
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserResponse response = service.changeStatus(principal(UserRole.ROOT_ADMIN), 1L, UserStatus.BLOCKED);

        assertThat(response.status()).isEqualTo("BLOCKED");
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void changeStatus_rejectsNonUserAdmin() {
        AdminUserService service = new AdminUserService(userRepository);

        assertThatThrownBy(() -> service.changeStatus(principal(UserRole.PRODUCT_ADMIN), 1L, UserStatus.BLOCKED))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void changeStatus_throwsNotFoundWhenUserMissing() {
        AdminUserService service = new AdminUserService(userRepository);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(principal(UserRole.USER_ADMIN), 1L, UserStatus.BLOCKED))
                .isInstanceOf(BusinessException.class);
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(99L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "관리자");
    }
}
