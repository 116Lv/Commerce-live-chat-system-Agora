package com.team7.agora.domain.chat.controller;

import com.team7.agora.global.auth.AuthUser;
import java.security.Principal;

/**
 * WebSocket/STOMP 연결에서 인증된 사용자를 식별하기 위해 사용하는 Principal 구현체이다.
 * @param authUser 인증 사용자 정보
 */
public record StompPrincipal(AuthUser authUser) implements Principal {
    /**
     * STOMP 세션에서 인증 사용자를 구분할 사용자 ID 문자열을 반환한다.
     * @return STOMP 세션에서 사용할 사용자 ID 문자열
     */
    @Override
    public String getName() {
        return String.valueOf(authUser.userId());
    }
}
