package com.team7.agora.global.websocket;

import com.team7.agora.domain.chat.controller.StompPrincipal;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.auth.JwtClaims;
import com.team7.agora.global.auth.JwtProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtProvider jwtProvider;

    public StompAuthInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authorization = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        JwtClaims claims = jwtProvider.parse(jwtProvider.substringBearer(authorization));
        AuthUser authUser = new AuthUser(
            claims.userId(),
            claims.email(),
            claims.role(),
            claims.nickname()
        );
        accessor.setUser(new StompPrincipal(authUser));
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }
}
