package com.team7.agora.domain.chat.dto.response;

import com.team7.agora.domain.chat.entity.ChatRoom;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param chatRoomId 입력 값
 * @param productId 입력 값
 * @param sellerId 입력 값
 * @param buyerId 입력 값
 * @param status 입력 값
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
     * @param chatRoom 입력 값
     * @return 처리 결과
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
