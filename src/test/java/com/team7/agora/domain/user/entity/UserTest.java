package com.team7.agora.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void changeStatusRejectsSameStatusTransition() {
        User user = User.signup("user@test.com", "encoded", "nickname", "01011112222");

        assertThatThrownBy(() -> user.changeStatus(UserStatus.ACTIVE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void changeStatusUpdatesToDifferentStatus() {
        User user = User.signup("user@test.com", "encoded", "nickname", "01011112222");

        user.changeStatus(UserStatus.BLOCKED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void changeStatusToDeletedAnonymizesPiiAndUsesDeletedDisplayName() {
        User user = User.signup("user@test.com", "encoded", "nickname", "01011112222");

        user.changeStatus(UserStatus.DELETED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getEmail()).startsWith("deleted-user-");
        assertThat(user.getEmail()).endsWith("@agora.local");
        assertThat(user.getEmail()).doesNotContain("user@test.com", "test.com");
        assertThat(user.getPhone()).isNull();
        assertThat(user.getNickname()).isEqualTo("탈퇴한 사용자");
    }

    @Test
    void changeStatusFromDeletedIsRejected() {
        User user = User.signup("user@test.com", "encoded", "nickname", "01011112222");
        user.changeStatus(UserStatus.DELETED);

        assertThatThrownBy(() -> user.changeStatus(UserStatus.ACTIVE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
