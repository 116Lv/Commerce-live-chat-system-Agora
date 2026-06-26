// 관심 지역 수정 요청 값을 담는 DTO
package com.team7.agora.domain.region.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Preferred Region Update 요청 본문을 표현하는 DTO이다.
 * @param regionIds 선호 지역 ID 목록
 * @param primaryRegionId 대표 선호 지역 ID
 */
public record PreferredRegionUpdateRequest(
        @NotEmpty(message = "관심 지역은 필수입니다.")
        List<Long> regionIds,
        Long primaryRegionId
) {
}
