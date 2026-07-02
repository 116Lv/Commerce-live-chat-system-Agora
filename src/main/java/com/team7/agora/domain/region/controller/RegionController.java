package com.team7.agora.domain.region.controller;

import com.team7.agora.domain.region.dto.response.RegionResponse;
import com.team7.agora.domain.region.service.RegionService;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 지역 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
public class RegionController {

    private final RegionService regionService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param regionService 지역 비즈니스 로직을 처리하는 서비스
     */
    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * 지역 정보를 조회하는 GET /api/regions 요청을 처리한다.
     * @param keyword 검색어
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/regions")
    public ApiResponse<List<RegionResponse>> findRegions(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success("지역을 조회했습니다.", regionService.findRegions(keyword));
    }

    /**
     * 전국 시/도 목록을 조회하는 GET /api/regions/sido 요청을 처리한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/regions/sido")
    public ApiResponse<List<String>> findSidoList() {
        return ApiResponse.success("시/도 목록을 조회했습니다.", regionService.findSidoList());
    }

    /**
     * 시/도에 속한 시/군/구 목록을 조회하는 GET /api/regions/sigungu 요청을 처리한다.
     * @param sido 시도 이름
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/regions/sigungu")
    public ApiResponse<List<String>> findSigunguList(@RequestParam String sido) {
        return ApiResponse.success("시/군/구 목록을 조회했습니다.", regionService.findSigunguList(sido));
    }

    /**
     * 시/도, 시/군/구에 속한 읍/면/동 목록을 조회하는 GET /api/regions/dong 요청을 처리한다.
     * @param sido 시도 이름
     * @param sigungu 시군구 이름
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/regions/dong")
    public ApiResponse<List<RegionResponse>> findDongList(
            @RequestParam String sido,
            @RequestParam String sigungu
    ) {
        return ApiResponse.success("읍/면/동 목록을 조회했습니다.", regionService.findDongList(sido, sigungu));
    }
}
