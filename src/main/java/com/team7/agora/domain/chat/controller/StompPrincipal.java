package com.team7.agora.domain.chat.controller;

import com.team7.agora.global.auth.AuthUser;
import java.security.Principal;

public record StompPrincipal(AuthUser authUser) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(authUser.userId());
    }
}
