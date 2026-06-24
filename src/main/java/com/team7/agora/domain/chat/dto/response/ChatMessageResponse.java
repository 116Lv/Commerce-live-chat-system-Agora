package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long messageId,
    Long chatRoomId,
    Long senderId,
    String senderNickname,
    String content,
    String messageType,
    LocalDateTime createdAt
) {

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
