// 사용자 상태에 따른 CustomUserDetails 플래그 매핑을 검증하는 단위 테스트
package com.team7.agora.global.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import org.junit.jupiter.api.Test;

class CustomUserDetailsTest {

    private CustomUserDetails detailsWithStatus(UserStatus status) {
        return new CustomUserDetails(1L, "user@test.com", "encoded", UserRole.ROLE_USER, status, "닉네임");
    }

    @Test
    void activeUser_isEnabledAndUnlocked() {
        // when
        CustomUserDetails details = detailsWithStatus(UserStatus.ACTIVE);

        // then
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_USER");
    }

    @Test
    void suspendedUser_isDisabledAndLocked() {
        // when
        CustomUserDetails details = detailsWithStatus(UserStatus.SUSPENDED);

        // then
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    void deletedUser_isDisabledAndExpired() {
        // when
        CustomUserDetails details = detailsWithStatus(UserStatus.DELETED);

        // then
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonExpired()).isFalse();
    }
}
