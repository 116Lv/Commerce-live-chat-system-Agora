package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param messageId 입력 값
 * @param chatRoomId 입력 값
 * @param senderId 입력 값
 * @param senderNickname 입력 값
 * @param content 입력 값
 * @param messageType 입력 값
 * @param createdAt 입력 값
 */
public record ChatMessageResponse(
    Long messageId,
    Long chatRoomId,
    Long senderId,
    String senderNickname,
    String content,
    String messageType,
    LocalDateTime createdAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param message 입력 값
     * @return 처리 결과
     */
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getChatRoom().getId(),
            message.getSender().getId(),
            message.getSender().getNickname(),
            message.getContent(),
            message.getMessageType().name(),
            message.getCreatedAt()
        );
    }
}
