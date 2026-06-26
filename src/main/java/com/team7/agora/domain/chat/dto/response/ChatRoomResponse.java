package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatRoom;

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
    String status
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param chatRoom 채팅방 엔티티
     * @return 클라이언트에 반환할 API 응답
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
