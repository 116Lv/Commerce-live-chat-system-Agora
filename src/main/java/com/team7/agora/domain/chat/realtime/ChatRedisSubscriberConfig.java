// 모든 chat-room:* 채널을 구독하는 Redis 리스너 컨테이너 설정
package com.team7.agora.domain.chat.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class ChatRedisSubscriberConfig {

    private static final String CHAT_ROOM_PATTERN = ChatRedisPublisher.TOPIC_PREFIX + "*";

    @Bean
    public ObjectMapper chatRedisObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    @ConditionalOnProperty(
        name = "agora.chat.redis-listener.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        ChatRedisSubscriber chatRedisSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(chatRedisSubscriber, new PatternTopic(CHAT_ROOM_PATTERN));
        return container;
    }
}
