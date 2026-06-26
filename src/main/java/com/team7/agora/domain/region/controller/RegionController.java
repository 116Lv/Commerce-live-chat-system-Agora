package com.team7.agora.domain.region.controller;

import com.team7.agora.domain.region.dto.response.RegionResponse;
import com.team7.agora.domain.region.service.RegionService;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
public class RegionController {

    private final RegionService regionService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param regionService 입력 값
     */
    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * 데이터를 반환한다.
     * @param keyword 입력 값
     * @return 처리 결과
     */
    @GetMapping("/api/regions")
    public ApiResponse<List<RegionResponse>> findRegions(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success("지역을 조회했습니다.", regionService.findRegions(keyword));
    }
}
