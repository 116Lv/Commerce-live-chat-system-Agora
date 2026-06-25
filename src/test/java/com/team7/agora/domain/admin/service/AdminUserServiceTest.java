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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(user)));

        List<AdminUserResponse> responses = service.getUsers(principal(UserRole.USER_ADMIN), PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).email()).isEqualTo("user@test.com");
        assertThat(responses.get(0).status()).isEqualTo("ACTIVE");
    }

    @Test
    void getUsers_rejectsNonUserAdmin() {
        AdminUserService service = new AdminUserService(userRepository);

        assertThatThrownBy(() -> service.getUsers(principal(UserRole.PRODUCT_ADMIN), PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(99L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "관리자");
    }
}
