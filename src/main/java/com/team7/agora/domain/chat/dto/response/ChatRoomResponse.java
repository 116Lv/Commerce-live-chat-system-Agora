package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.entity.ChatMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 채팅방 응답 본문을 표현하는 DTO이다.
 * @param chatRoomId 채팅방 ID
 * @param productId 상품 ID
 * @param sellerId 상품 판매자 ID
 * @param buyerId 구매자 ID
 * @param status 조회 또는 변경할 상태
 */
public record ChatRoomResponse(
    Long chatRoomId,
    Long productId,
    Long sellerId,
    Long buyerId,
    String status,
    String productTitle,
    BigDecimal productPrice,
    String productStatus,
    String productThumbnailUrl,
    String sellerNickname,
    String buyerNickname,
    Long lastMessageId,
    String lastMessagePreview,
    String lastMessageType,
    Long lastMessageSenderId,
    String lastMessageSenderNickname,
    LocalDateTime lastMessageCreatedAt,
    long unreadCount
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param chatRoom 채팅방 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return from(chatRoom, null, null, 0L);
    }

    public static ChatRoomResponse from(
        ChatRoom chatRoom,
        String productThumbnailUrl,
        ChatMessage lastMessage,
        long unreadCount
    ) {
        return new ChatRoomResponse(
            chatRoom.getId(),
            chatRoom.getProduct().getId(),
            chatRoom.getSeller().getId(),
            chatRoom.getBuyer().getId(),
            chatRoom.getStatus().name(),
            chatRoom.getProduct().getTitle(),
            chatRoom.getProduct().getPrice(),
            chatRoom.getProduct().getStatus().name(),
            productThumbnailUrl,
            chatRoom.getSeller().getNickname(),
            chatRoom.getBuyer().getNickname(),
            lastMessage == null ? null : lastMessage.getId(),
            lastMessage == null ? null : lastMessage.getContent(),
            lastMessage == null ? null : lastMessage.getMessageType().name(),
            lastMessage == null ? null : lastMessage.getSender().getId(),
            lastMessage == null ? null : lastMessage.getSender().getNickname(),
            lastMessage == null ? null : lastMessage.getCreatedAt(),
            unreadCount
        );
    }
}
