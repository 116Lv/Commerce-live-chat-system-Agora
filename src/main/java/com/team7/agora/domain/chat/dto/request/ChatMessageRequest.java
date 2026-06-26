package com.team7.agora.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param content 입력 값
 */
public record ChatMessageRequest(
    @NotBlank(message = "메시지를 입력해 주세요.")
    @Size(max = 1000, message = "메시지는 1000자 이하로 입력해 주세요.")
    String content
) {
}
