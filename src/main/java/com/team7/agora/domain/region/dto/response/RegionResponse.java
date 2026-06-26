package com.team7.agora.domain.region.dto.response;

import com.team7.agora.domain.region.entity.Region;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param regionId 입력 값
 * @param name 입력 값
 * @param code 입력 값
 * @param sido 입력 값
 * @param sigungu 입력 값
 * @param eupmyeondong 입력 값
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
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param region 입력 값
     * @return 처리 결과
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
