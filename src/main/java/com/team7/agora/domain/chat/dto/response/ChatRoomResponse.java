package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatRoom;

/**
 * Response payload for returning chat room data.
 * @param chatRoomId the chat room id value
 * @param productId the product id value
 * @param sellerId the seller id value
 * @param buyerId the buyer id value
 * @param status the status value
 */
public record ChatRoomResponse(
    Long chatRoomId,
    Long productId,
    Long sellerId,
    Long buyerId,
    String status
) {

    /**
     * Creates a response from the given domain object.
     * @param chatRoom the chat room value
     * @return the from result
     */
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
