package com.team7.agora.domain.admin.dto.request;

import jakarta.validation.constraints.Size;

public record AdminApprovalDecisionRequest(
        @Size(max = 500) String memo
) {
}
