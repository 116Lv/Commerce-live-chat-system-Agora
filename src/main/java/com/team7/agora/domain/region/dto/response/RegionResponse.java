package com.team7.agora.domain.region.dto.response;

import com.team7.agora.domain.region.entity.Region;

public record RegionResponse(
        Long regionId,
        String name,
        String code,
        String sido,
        String sigungu,
        String eupmyeondong
) {

    public static RegionResponse from(Region region) {
        return new RegionResponse(
                region.getId(),
                region.getName(),
                region.getCode(),
                region.getSido(),
                region.getSigungu(),
                region.getEupmyeondong()
        );
    }
}
