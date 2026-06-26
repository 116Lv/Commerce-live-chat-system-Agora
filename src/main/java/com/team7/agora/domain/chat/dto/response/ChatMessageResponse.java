package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;

/**
 * Response payload for returning chat message data.
 * @param messageId the message id value
 * @param chatRoomId the chat room id value
 * @param senderId the sender id value
 * @param senderNickname the sender nickname value
 * @param content the content value
 * @param messageType the message type value
 * @param createdAt the created at value
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
     * Creates a response from the given domain object.
     * @param message the message value
     * @return the from result
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
