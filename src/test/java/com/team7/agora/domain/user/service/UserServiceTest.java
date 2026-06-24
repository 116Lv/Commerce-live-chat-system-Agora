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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void getMeReturnsUserInfo() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = User.signup("user@test.com", passwordEncoder.encode("password123!"), "동네유저", "01012345678");
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserMeResponse response = userService.getMe(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.nickname()).isEqualTo("동네유저");
    }

    @Test
    void getMeThrowsNotFoundWhenUserMissing() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void changePasswordUpdatesPasswordWhenCurrentPasswordMatches() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = User.signup("user@test.com", passwordEncoder.encode("oldPassword1!"), "동네유저", "01012345678");
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.changePassword(1L, "oldPassword1!", "newPassword1!");

        assertThat(passwordEncoder.matches("newPassword1!", user.getPassword())).isTrue();
    }

    @Test
    void changePasswordRejectsWhenCurrentPasswordDoesNotMatch() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = User.signup("user@test.com", passwordEncoder.encode("oldPassword1!"), "동네유저", "01012345678");
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "newPassword1!"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void getSmileScoreReturnsUserScore() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = User.signup("user@test.com", passwordEncoder.encode("password123!"), "동네유저", "01012345678");
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userService.getSmileScore(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.smileScore()).isEqualTo(60);
    }

    @Test
    void updateProfileChangesNickname() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = User.signup("user@test.com", passwordEncoder.encode("password123!"), "동네유저", "01012345678");
        assignId(user, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserMeResponse response = userService.updateProfile(1L, "새이름");

        assertThat(response.nickname()).isEqualTo("새이름");
        assertThat(user.getNickname()).isEqualTo("새이름");
    }
}
