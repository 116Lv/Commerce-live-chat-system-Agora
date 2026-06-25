package com.team7.agora.domain.chat.realtime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ChatRedisSubscriberTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private Message redisMessage;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void onMessage_forwardsDeserializedMessageToRoomDestination() {
        ChatRedisSubscriber subscriber = new ChatRedisSubscriber(messagingTemplate, objectMapper);
        ChatMessageResponse payload = new ChatMessageResponse(
            1L, 100L, 2L, "구매자", "안녕하세요", "TEXT", LocalDateTime.now()
        );
        when(redisMessage.getBody()).thenReturn(objectMapper.writeValueAsBytes(payload));

        subscriber.onMessage(redisMessage, "chat-room:100".getBytes());

        verify(messagingTemplate).convertAndSend(eq("/sub/chat/100"), eq(payload));
    }
}
