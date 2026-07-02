// Redis 채널에서 받은 채팅 메시지를, 이 서버에 붙어있는 STOMP 구독자에게 다시 전달한다
package com.team7.agora.domain.chat.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.dto.response.ChatNotificationResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub에서 수신한 채팅 메시지를 현재 서버의 STOMP 구독자에게 전달한다.
 */
@Slf4j
@Component
public class ChatRedisSubscriber implements MessageListener {

    static final String DESTINATION_PREFIX = "/sub/chat/";
    static final String USER_DESTINATION_PREFIX = "/sub/users/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ChatRedisSubscriber(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Redis 메시지를 역직렬화해 채팅방 STOMP 목적지로 전송한다.
     * @param message 메시지
     * @param pattern 구독 중인 Redis 패턴
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            if (channel.startsWith(ChatRedisPublisher.USER_TOPIC_PREFIX)) {
                ChatNotificationResponse response =
                    objectMapper.readValue(message.getBody(), ChatNotificationResponse.class);
                messagingTemplate.convertAndSend(USER_DESTINATION_PREFIX + response.recipientId() + "/chat", response);
                return;
            }

            ChatMessageResponse response = objectMapper.readValue(message.getBody(), ChatMessageResponse.class);
            messagingTemplate.convertAndSend(DESTINATION_PREFIX + response.chatRoomId(), response);
        } catch (IOException e) {
            log.warn("Redis 채팅 메시지 역직렬화에 실패했습니다.", e);
        }
    }
}
