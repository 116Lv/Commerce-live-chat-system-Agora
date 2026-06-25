// 관심 지역 수정 요청 값을 담는 DTO
package com.team7.agora.domain.region.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PreferredRegionUpdateRequest(
        @NotEmpty(message = "관심 지역은 필수입니다.")
        List<Long> regionIds,
        Long primaryRegionId
) {
}
