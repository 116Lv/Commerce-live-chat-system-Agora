package com.team7.agora.domain.chat.controller;

import com.team7.agora.global.auth.AuthUser;
import java.security.Principal;

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 * @param authUser 입력 값
 */
public record StompPrincipal(AuthUser authUser) implements Principal {
    /**
     * 데이터를 반환한다.
     * @return 처리 결과
     */
    @Override
    public String getName() {
        return String.valueOf(authUser.userId());
    }
}
