package com.team7.agora.global.websocket;

import com.team7.agora.domain.chat.controller.StompPrincipal;
import com.team7.agora.global.auth.AccountType;
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
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param jwtProvider JWT 생성과 검증을 담당하는 컴포넌트
     */
    public StompAuthInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * STOMP CONNECT 요청의 JWT 토큰을 검증하고 인증 사용자를 세션에 저장한다.
     * @param message 메시지
     * @param channel 메시지 채널
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authorization = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        JwtClaims claims = jwtProvider.parse(jwtProvider.substringBearer(authorization));
        if (claims.accountType() != AccountType.USER) {
            return null;
        }
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
