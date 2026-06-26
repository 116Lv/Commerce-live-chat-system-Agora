// 채팅 메시지를 Redis 채널(chat-room:{roomId})에 발행해, 서버가 여러 대여도 모두 전달받게 한다
package com.team7.agora.domain.chat.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatRedisPublisher {

    static final String TOPIC_PREFIX = "chat-room:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatRedisPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(Long roomId, ChatMessageResponse message) {
        try {
            redisTemplate.convertAndSend(TOPIC_PREFIX + roomId, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("채팅 메시지 직렬화에 실패했습니다.", e);
        }
    }
}
