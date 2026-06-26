// 역할별 관리자 대시보드 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

import java.util.List;

/**
 * Admin Dashboard 응답 본문을 표현하는 DTO이다.
 * @param role 권한
 * @param accessibleMenus 관리자가 접근할 수 있는 메뉴 목록
 */
public record AdminDashboardResponse(
        String role,
        List<String> accessibleMenus
) {
}
