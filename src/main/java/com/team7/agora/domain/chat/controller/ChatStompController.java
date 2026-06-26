package com.team7.agora.domain.chat.controller;

import com.team7.agora.domain.chat.dto.request.ChatMessageRequest;
import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.service.ChatService;
import com.team7.agora.global.auth.AuthUser;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@Controller
@Validated
public class ChatStompController {

    private final ChatService chatService;
    private final ChatRedisPublisher chatRedisPublisher;

    public ChatStompController(ChatService chatService, ChatRedisPublisher chatRedisPublisher) {
        this.chatService = chatService;
        this.chatRedisPublisher = chatRedisPublisher;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param chatRoomId 입력 값
     * @param request 입력 값
     * @param principal 입력 값
     */
    @MessageMapping("/chat/{chatRoomId}/messages")
    public void send(
        @DestinationVariable Long chatRoomId,
        @Payload ChatMessageRequest request,
        Principal principal
    ) {
        AuthUser authUser = (AuthUser) ((StompPrincipal) principal).authUser();
        ChatMessageResponse response = chatService.sendMessage(authUser.userId(), chatRoomId, request.content());
        chatRedisPublisher.publish(chatRoomId, response);
    }
}
