// 사용자별 채팅 알림 STOMP 페이로드를 표현하는 DTO
package com.team7.agora.domain.chat.dto.response;

import java.time.OffsetDateTime;

/**
 * 사용자별 채팅 알림 응답 본문을 표현하는 DTO이다.
 * @param recipientId 알림 수신자 ID
 * @param messageId 채팅 메시지 ID
 * @param chatRoomId 채팅방 ID
 * @param senderId 메시지를 보낸 회원 ID
 * @param senderNickname 메시지를 보낸 회원 닉네임
 * @param content 메시지 내용
 * @param messageType 메시지 유형
 * @param productTitle 채팅방 상품명
 * @param createdAt 메시지 생성 시각
 */
public record ChatNotificationResponse(
    Long recipientId,
    Long messageId,
    Long chatRoomId,
    Long senderId,
    String senderNickname,
    String content,
    String messageType,
    String productTitle,
    OffsetDateTime createdAt
) {

    public static ChatNotificationResponse from(
        Long recipientId,
        ChatMessageResponse message,
        ChatRoomResponse room
    ) {
        return new ChatNotificationResponse(
            recipientId,
            message.messageId(),
            message.chatRoomId(),
            message.senderId(),
            message.senderNickname(),
            message.content(),
            message.messageType(),
            room.productTitle(),
            message.createdAt()
        );
    }
}
