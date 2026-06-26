// 네고/거래 등 다른 도메인이 채팅방에 시스템 메시지를 남기고 실시간으로 전달하기 위한 컴포넌트
package com.team7.agora.domain.chat.service;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.entity.ChatMessage;
import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.repository.ChatMessageRepository;
import com.team7.agora.domain.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * 다른 도메인에서 발생한 시스템 메시지를 채팅방에 저장하고 발행하는 컴포넌트이다.
 */
@Component
public class ChatSystemMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRedisPublisher chatRedisPublisher;

    public ChatSystemMessageService(
        ChatMessageRepository chatMessageRepository,
        ChatRedisPublisher chatRedisPublisher
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRedisPublisher = chatRedisPublisher;
    }

    /**
     * 시스템 메시지를 저장한 뒤 채팅방 Redis 채널로 발행한다.
     * @param chatRoom 메시지를 남길 채팅방
     * @param actor 메시지를 발생시킨 사용자
     * @param content 시스템 메시지 내용
     */
    public void send(ChatRoom chatRoom, User actor, String content) {
        ChatMessage message = chatMessageRepository.save(ChatMessage.system(chatRoom, actor, content));
        chatRedisPublisher.publish(chatRoom.getId(), ChatMessageResponse.from(message));
    }
}
