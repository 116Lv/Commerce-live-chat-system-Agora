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

/**
 * STOMP 인증 처리를 담당하는 WebSocket 컴포넌트이다.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtProvider jwtProvider;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param jwtProvider 입력 값
     */
    public StompAuthInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param message 입력 값
     * @param channel 입력 값
     * @return 처리 결과
     */
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
