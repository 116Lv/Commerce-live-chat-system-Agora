// 관리자 로그인 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

/**
 * Response payload for returning admin login data.
 * @param accessToken the access token value
 */
public record AdminLoginResponse(
        String accessToken
) {
}
