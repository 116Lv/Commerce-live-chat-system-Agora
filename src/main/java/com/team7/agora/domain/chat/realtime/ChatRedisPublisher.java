// 채팅 메시지를 Redis 채널(chat-room:{roomId})에 발행해, 서버가 여러 대여도 모두 전달받게 한다
package com.team7.agora.domain.chat.realtime;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatRedisPublisher {

    static final String TOPIC_PREFIX = "chat-room:";

    private final RedisTemplate<String, Object> redisTemplate;

    public ChatRedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(Long roomId, ChatMessageResponse message) {
        redisTemplate.convertAndSend(TOPIC_PREFIX + roomId, message);
    }
}
