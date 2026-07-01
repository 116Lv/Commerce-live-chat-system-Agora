package com.team7.agora.domain.coupon.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminCouponEventIssueRequest(
    @Size(max = 100, message = "관리자 개별 발급은 한 번에 100명까지만 가능합니다.")
    List<String> targets,

    @Size(max = 100, message = "관리자 개별 발급은 한 번에 100명까지만 가능합니다.")
    List<Long> userIds
) {
    public List<String> issueTargets() {
        if (targets != null && !targets.isEmpty()) {
            return targets;
        }
        if (userIds == null) {
            return List.of();
        }
        return userIds.stream()
            .map(String::valueOf)
            .toList();
    }
}
