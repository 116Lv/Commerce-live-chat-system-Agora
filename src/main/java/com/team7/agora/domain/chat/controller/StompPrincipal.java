package com.team7.agora.domain.chat.controller;

import com.team7.agora.global.auth.AuthUser;
import java.security.Principal;

/**
 * REST controller that exposes stomp endpoints.
 * @param authUser the auth user value
 */
public record StompPrincipal(AuthUser authUser) implements Principal {
    /**
     * Returns name data.
     * @return the get name result
     */
    @Override
    public String getName() {
        return String.valueOf(authUser.userId());
    }
}
