package com.team7.agora.domain.admin.dto.request;

import com.team7.agora.domain.admin.enums.AdminRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminApprovalRoleChangeRequest(
        @NotNull Long targetAdminId,
        @NotNull AdminRole requestedRole,
        @NotNull @Size(min = 1, max = 500) String reason
) {
}
