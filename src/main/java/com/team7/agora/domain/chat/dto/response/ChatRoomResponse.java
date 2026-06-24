package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatRoom;

public record ChatRoomResponse(
    Long chatRoomId,
    Long productId,
    Long sellerId,
    Long buyerId,
    String status
) {

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
            chatRoom.getId(),
            chatRoom.getProduct().getId(),
            chatRoom.getSeller().getId(),
            chatRoom.getBuyer().getId(),
            chatRoom.getStatus().name()
        );
    }
}
