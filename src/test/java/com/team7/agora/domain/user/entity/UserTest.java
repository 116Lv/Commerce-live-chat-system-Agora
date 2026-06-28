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
    void changeStatusToDeletedSetsDeletedAt() {
        User user = User.signup("user@test.com", "encoded", "nickname", "01011112222");

        user.changeStatus(UserStatus.DELETED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void changeStatusFromDeletedClearsDeletedAt() {
        User user = User.signup("user@test.com", "encoded", "nickname", "01011112222");
        user.changeStatus(UserStatus.DELETED);

        user.changeStatus(UserStatus.ACTIVE);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getDeletedAt()).isNull();
    }
}
