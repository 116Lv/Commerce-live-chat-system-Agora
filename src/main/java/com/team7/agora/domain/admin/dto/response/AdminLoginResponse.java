// 관리자 로그인 응답 값을 담는 DTO
package com.team7.agora.domain.admin.dto.response;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param accessToken 입력 값
 */
public record AdminLoginResponse(
        String accessToken
) {
}
