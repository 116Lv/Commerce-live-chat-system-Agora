package com.team7.agora.domain.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.annotation.Validated;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

    @Test
    void getMessagesSizeUsesBeanValidationOnly() throws NoSuchMethodException {
        assertThat(ChatRoomController.class.getAnnotation(Validated.class)).isNotNull();

        Method method = ChatRoomController.class.getMethod(
            "getMessages",
            com.team7.agora.global.auth.CustomUserDetails.class,
            Long.class,
            Long.class,
            int.class
        );

        Annotation[] sizeAnnotations = method.getParameterAnnotations()[3];
        assertThat(sizeAnnotations).anyMatch(annotation -> annotation instanceof Min min && min.value() == 1);
        assertThat(sizeAnnotations).anyMatch(annotation -> annotation instanceof Max max && max.value() == 500);
    }
}
