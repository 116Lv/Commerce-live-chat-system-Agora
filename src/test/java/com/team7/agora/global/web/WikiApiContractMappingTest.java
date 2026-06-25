package com.team7.agora.global.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.chat.controller.ChatRoomController;
import com.team7.agora.domain.chat.dto.request.ChatRoomOpenRequest;
import com.team7.agora.global.auth.AuthUser;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

class WikiApiContractMappingTest {

    @Test
    void chatRoomCreate_supportsBodyProductIdPath() throws NoSuchMethodException {
        Method method = ChatRoomController.class.getDeclaredMethod(
            "openRoomByRequest",
            AuthUser.class,
            ChatRoomOpenRequest.class
        );

        assertThat(method.getAnnotation(PostMapping.class).value())
            .contains("/rooms");
    }
}
