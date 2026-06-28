// 사용자 스마일지수 조회 API 컨트롤러
package com.team7.agora.domain.user.controller;

import com.team7.agora.domain.user.dto.response.SmileScoreResponse;
import com.team7.agora.domain.user.service.UserService;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 스마일 점수 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
public class SmileScoreController {

    private final UserService userService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userService 회원 비즈니스 로직을 처리하는 서비스
     */
    public SmileScoreController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 스마일 점수 정보를 조회하는 GET /api/users/{userId}/smile-score 요청을 처리한다.
     * @param userId 대상 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/users/{userId}/smile-score")
    public ApiResponse<SmileScoreResponse> getSmileScore(@PathVariable Long userId) {
        SmileScoreResponse response = userService.getSmileScore(userId);
        return ApiResponse.success("스마일지수 조회가 완료되었습니다.", response);
    }
}
