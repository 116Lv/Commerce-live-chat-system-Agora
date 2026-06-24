// 역할별 관리자 대시보드 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

import java.util.List;

public record AdminDashboardResponse(
        String role,
        List<String> accessibleMenus
) {
}
