// 사용자 스마일지수 조회 API 컨트롤러
package com.team7.agora.domain.user.controller;

import com.team7.agora.domain.user.dto.response.SmileScoreResponse;
import com.team7.agora.domain.user.service.UserService;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmileScoreController {

    private final UserService userService;

    public SmileScoreController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/{userId}/smile-score")
    public ApiResponse<SmileScoreResponse> getSmileScore(@PathVariable Long userId) {
        SmileScoreResponse response = userService.getSmileScore(userId);
        return ApiResponse.success("스마일지수 조회가 완료되었습니다.", response);
    }
}
