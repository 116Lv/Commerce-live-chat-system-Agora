package com.team7.agora.domain.region.dto.response;

import com.team7.agora.domain.region.entity.Region;

/**
 * Response payload for returning region data.
 * @param regionId the region id value
 * @param name the name value
 * @param code the code value
 * @param sido the sido value
 * @param sigungu the sigungu value
 * @param eupmyeondong the eupmyeondong value
 */
public record RegionResponse(
        Long regionId,
        String name,
        String code,
        String sido,
        String sigungu,
        String eupmyeondong
) {

    /**
     * Creates a response from the given domain object.
     * @param region the region value
     * @return the from result
     */
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
