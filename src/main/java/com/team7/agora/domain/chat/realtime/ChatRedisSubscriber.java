// Redis 채널에서 받은 채팅 메시지를, 이 서버에 붙어있는 STOMP 구독자에게 다시 전달한다
package com.team7.agora.domain.chat.realtime;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatRedisSubscriber implements MessageListener {

    static final String DESTINATION_PREFIX = "/sub/chat/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ChatRedisSubscriber(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        ChatMessageResponse response = objectMapper.readValue(message.getBody(), ChatMessageResponse.class);
        messagingTemplate.convertAndSend(DESTINATION_PREFIX + response.chatRoomId(), response);
    }
}
