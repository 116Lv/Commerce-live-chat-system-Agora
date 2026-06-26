package com.team7.agora.domain.region.controller;

import com.team7.agora.domain.region.dto.response.RegionResponse;
import com.team7.agora.domain.region.service.RegionService;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes region endpoints.
 */
@RestController
public class RegionController {

    private final RegionService regionService;

    /**
     * Creates a region controller instance.
     * @param regionService the region service value
     */
    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * Returns regions data.
     * @param keyword the keyword value
     * @return the find regions result
     */
    @GetMapping("/api/regions")
    public ApiResponse<List<RegionResponse>> findRegions(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success("지역을 조회했습니다.", regionService.findRegions(keyword));
    }
}
