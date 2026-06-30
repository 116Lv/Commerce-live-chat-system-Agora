package com.team7.agora.domain.chat.repository;

public record ChatRoomUnreadCount(
    Long chatRoomId,
    Long unreadCount
) {
}
