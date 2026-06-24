// AuthService.signup의 성공/중복 분기를 검증하는 단위 테스트
package com.team7.agora.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.auth.dto.request.SignupRequest;
import com.team7.agora.domain.auth.dto.response.SignupResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService createService() {
        return new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void signup_savesEncodedPasswordWithDefaultRoleAndStatus() {
        // given
        AuthService authService = createService();
        SignupRequest request = new SignupRequest("user@test.com", "password123!", "동네유저");
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
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
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }
}
