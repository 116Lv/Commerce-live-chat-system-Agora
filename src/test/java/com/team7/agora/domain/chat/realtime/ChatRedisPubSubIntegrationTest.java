package com.team7.agora.domain.chat.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@EnabledIfSystemProperty(named = "agora.redis.integration.enabled", matches = "true")
@TestPropertySource(properties = "agora.chat.redis-listener.enabled=true")
class ChatRedisPubSubIntegrationTest {

    @Autowired
    private ChatRedisPublisher chatRedisPublisher;

    @MockitoSpyBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void publish_isDeliveredToLocalStompSubscribersThroughRedisChannel() {
        Long roomId = 999_001L;
        ChatMessageResponse message = new ChatMessageResponse(
            1L, roomId, 2L, "구매자", "안녕하세요", "TEXT", LocalDateTime.now()
        );

        chatRedisPublisher.publish(roomId, message);

        ArgumentCaptor<ChatMessageResponse> captor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate, timeout(3000).atLeast(1))
            .convertAndSend(eq("/sub/chat/" + roomId), captor.capture());

        ChatMessageResponse delivered = captor.getValue();
        assertThat(delivered.messageId()).isEqualTo(1L);
        assertThat(delivered.chatRoomId()).isEqualTo(roomId);
        assertThat(delivered.content()).isEqualTo("안녕하세요");
    }
}
