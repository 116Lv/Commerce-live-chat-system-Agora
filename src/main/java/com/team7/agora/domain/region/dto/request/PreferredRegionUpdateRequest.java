// 관심 지역 수정 요청 값을 담는 DTO
package com.team7.agora.domain.region.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param regionIds 입력 값
 * @param primaryRegionId 입력 값
 */
public record PreferredRegionUpdateRequest(
        @NotEmpty(message = "관심 지역은 필수입니다.")
        List<Long> regionIds,
        Long primaryRegionId
) {
}
