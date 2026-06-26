// 관리자 권한 변경 요청 값을 담는 DTO
package com.team7.agora.domain.admin.dto.request;

import com.team7.agora.domain.user.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param role 입력 값
 */
public record AdminAccountRoleUpdateRequest(
        @NotNull(message = "변경할 권한은 필수입니다.")
        UserRole role
) {
}
