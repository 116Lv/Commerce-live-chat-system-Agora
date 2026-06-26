package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;

/**
 * Chat Message 응답 본문을 표현하는 DTO이다.
 * @param messageId 채팅 메시지 ID
 * @param chatRoomId 채팅방 ID
 * @param senderId 메시지를 보낸 회원 ID
 * @param senderNickname 메시지를 보낸 회원 닉네임
 * @param content 내용
 * @param messageType 채팅 메시지 유형
 * @param createdAt 데이터가 생성된 시각
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
     * @param message 메시지
     * @return 클라이언트에 반환할 API 응답
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
