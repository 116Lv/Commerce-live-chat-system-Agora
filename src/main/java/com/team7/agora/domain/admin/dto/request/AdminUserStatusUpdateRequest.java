package com.team7.agora.domain.admin.dto.request;

import com.team7.agora.domain.user.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record AdminUserStatusUpdateRequest(
        @NotNull(message = "변경할 상태는 필수입니다.")
        UserStatus status
) {
}
