package com.team7.agora.domain.region.dto.response;

import com.team7.agora.domain.region.entity.Region;

/**
 * 지역 응답 본문을 표현하는 DTO이다.
 * @param regionId 지역 ID
 * @param name 이름 또는 제목
 * @param code 지역 코드
 * @param sido 시도 이름
 * @param sigungu 시군구 이름
 * @param eupmyeondong 읍면동 이름
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
     * @param region 거래 지역 엔티티
     * @return 클라이언트에 반환할 API 응답
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
