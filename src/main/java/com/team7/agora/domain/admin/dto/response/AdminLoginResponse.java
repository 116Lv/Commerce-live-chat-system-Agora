// 관리자 로그인 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

public record AdminLoginResponse(
        String accessToken
) {
}
