// 채팅방 생성 API 요청 DTO입니다.
package com.team7.agora.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatRoomOpenRequest(
    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId
) {
}
