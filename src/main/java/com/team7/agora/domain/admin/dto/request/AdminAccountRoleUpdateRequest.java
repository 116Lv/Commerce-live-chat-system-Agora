package com.team7.agora.domain.admin.dto.request;

import com.team7.agora.domain.admin.enums.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminAccountRoleUpdateRequest(
        @NotNull(message = "Role is required.")
        AdminRole role
) {
}
