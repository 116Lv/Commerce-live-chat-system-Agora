// 채팅 메시지를 Redis 채널(chat-room:{roomId})에 발행해, 서버가 여러 대여도 모두 전달받게 한다
package com.team7.agora.domain.chat.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 채팅 메시지를 Redis Pub/Sub 채널로 발행해 모든 애플리케이션 인스턴스가 전달받도록 한다.
 */
@Component
public class ChatRedisPublisher {

    static final String TOPIC_PREFIX = "chat-room:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatRedisPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 채팅 메시지를 직렬화해 채팅방별 Redis 채널에 발행한다.
     * @param roomId 채팅방 ID
     * @param message 메시지
     */
    public void publish(Long roomId, ChatMessageResponse message) {
        try {
            redisTemplate.convertAndSend(TOPIC_PREFIX + roomId, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("채팅 메시지 직렬화에 실패했습니다.", e);
        }
    }
}
