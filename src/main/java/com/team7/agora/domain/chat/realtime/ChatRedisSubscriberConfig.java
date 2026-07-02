// 모든 채팅 Redis Pub/Sub 채널을 구독하는 리스너 컨테이너 설정
package com.team7.agora.domain.chat.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 채팅 메시지 팬아웃을 위한 Redis Pub/Sub 구성을 제공한다.
 */
@Configuration
public class ChatRedisSubscriberConfig {

    private static final String CHAT_ROOM_PATTERN = ChatRedisPublisher.TOPIC_PREFIX + "*";
    private static final String CHAT_USER_PATTERN = ChatRedisPublisher.USER_TOPIC_PREFIX + "*";

    /**
     * Java 시간 타입을 지원하는 채팅 Redis 페이로드용 ObjectMapper를 생성한다.
     * @return 채팅 Redis 페이로드 ObjectMapper
     */
    @Bean
    public ObjectMapper chatRedisObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * 모든 채팅방과 사용자별 알림 Redis 채널을 구독하는 리스너 컨테이너를 생성한다.
     * @param connectionFactory Redis 연결 팩토리
     * @param chatRedisSubscriber 채팅 Redis 구독자
     * @return Redis 메시지 리스너 컨테이너
     */
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
        container.addMessageListener(chatRedisSubscriber, new PatternTopic(CHAT_USER_PATTERN));
        return container;
    }
}
