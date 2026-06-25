// UserService 단위 테스트
package com.team7.agora.domain.user.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.user.dto.response.UserMeResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService createService() {
        return new UserService(userRepository, passwordEncoder);
    }

    private User userWithId(long id) {
        User user = User.create("user@test.com", passwordEncoder.encode("oldPassword1!"), "동네유저");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void getMe_returnsAuthenticatedUserInfo() {
        // given
        UserService userService = createService();
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));

        // when
        UserMeResponse response = userService.getMe(1L);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.nickname()).isEqualTo("동네유저");
        assertThat(response.role()).isEqualTo("ROLE_USER");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void getMe_throwsNotFoundWhenUserMissing() {
        // given
        UserService userService = createService();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMe(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void updateProfile_changesNickname() {
        // given
        UserService userService = createService();
        User user = userWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserMeResponse response = userService.updateProfile(1L, "새닉네임");

        // then
        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    void changePassword_updatesEncodedPasswordWhenCurrentPasswordMatches() {
        // given
        UserService userService = createService();
        User user = userWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        userService.changePassword(1L, "oldPassword1!", "newPassword1!");

        // then
        assertThat(passwordEncoder.matches("newPassword1!", user.getPassword())).isTrue();
    }

    @Test
    void changePassword_throwsInvalidRequestWhenCurrentPasswordMismatches() {
        // given
        UserService userService = createService();
        User user = userWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "newPassword1!"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }
}
