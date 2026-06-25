package com.team7.agora.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.team7.agora.domain.chat.controller.StompPrincipal;
import com.team7.agora.global.auth.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

class StompAuthInterceptorTest {

    @Test
    void connectWithBearerTokenSetsStompPrincipal() {
        JwtProvider jwtProvider = new JwtProvider("localEnvDummySecretKeyForAgoraProjectTest12345!", 3600000);
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtProvider);
        String token = jwtProvider.createToken(1L, "user@test.com", "ROLE_USER", "동네유저");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", token);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isInstanceOf(StompPrincipal.class);
        assertThat(resultAccessor.getUser().getName()).isEqualTo("1");
    }
}
