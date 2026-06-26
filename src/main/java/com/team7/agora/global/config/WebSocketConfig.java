package com.team7.agora.global.config;

import com.team7.agora.global.websocket.StompAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정이다.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor stompAuthInterceptor;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param stompAuthInterceptor WebSocket/STOMP 연결 인증을 처리하는 인터셉터
     */
    public WebSocketConfig(StompAuthInterceptor stompAuthInterceptor) {
        this.stompAuthInterceptor = stompAuthInterceptor;
    }

    /**
     * 채팅 메시지를 주고받을 STOMP 발행 경로와 구독 경로를 설정한다.
     * @param registry 등록 설정 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    /**
     * 클라이언트가 WebSocket/STOMP에 연결할 엔드포인트와 SockJS 옵션을 등록한다.
     * @param registry 등록 설정 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*");
    }

    /**
     * WebSocket으로 들어오는 STOMP 요청에 JWT 인증 인터셉터를 적용한다.
     * @param registration 채널 등록 객체
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
}
