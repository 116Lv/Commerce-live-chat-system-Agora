// 역할별 관리자 대시보드 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

import java.util.List;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param role 입력 값
 * @param accessibleMenus 입력 값
 */
public record AdminDashboardResponse(
        String role,
        List<String> accessibleMenus
) {
}
