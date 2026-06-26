package com.team7.agora.domain.chat.controller;

import com.team7.agora.domain.chat.dto.request.ChatMessageRequest;
import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.service.ChatService;
import com.team7.agora.global.auth.AuthUser;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * REST controller that exposes chat stomp endpoints.
 */
@Controller
@Validated
public class ChatStompController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a chat stomp controller instance.
     * @param chatService the chat service value
     * @param messagingTemplate the messaging template value
     */
    public ChatStompController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles send behavior.
     * @param chatRoomId the chat room id value
     * @param request the request value
     * @param principal the principal value
     */
    @MessageMapping("/chat/{chatRoomId}/messages")
    public void send(
        @DestinationVariable Long chatRoomId,
        @Payload ChatMessageRequest request,
        Principal principal
    ) {
        AuthUser authUser = (AuthUser) ((StompPrincipal) principal).authUser();
        ChatMessageResponse response = chatService.sendMessage(authUser.userId(), chatRoomId, request.content());
        messagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, response);
    }
}
