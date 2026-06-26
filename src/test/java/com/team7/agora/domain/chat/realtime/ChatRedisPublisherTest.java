package com.team7.agora.domain.chat.realtime;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class ChatRedisPublisherTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void publish_sendsJsonMessageToRoomChannel() throws Exception {
        ChatRedisPublisher publisher = new ChatRedisPublisher(redisTemplate, objectMapper);
        ChatMessageResponse message = new ChatMessageResponse(
            1L, 100L, 2L, "구매자", "안녕하세요", "TEXT", LocalDateTime.now()
        );

        publisher.publish(100L, message);

        verify(redisTemplate).convertAndSend("chat-room:100", objectMapper.writeValueAsString(message));
    }
}
