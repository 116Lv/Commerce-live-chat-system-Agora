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
 * 실시간 채팅 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
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
     * 시스템 알림 메시지를 채팅방 메시지로 저장해 대화 흐름에 남긴다.
     * @param chatRoomId 채팅방 ID
     * @param request 요청 본문
     * @param principal 인증 주체
     */
    @MessageMapping("/chat/{chatRoomId}/messages")
    public void send(
        @DestinationVariable Long chatRoomId,
        @Payload ChatMessageRequest request,
        Principal principal
    ) {
        AuthUser authUser = (AuthUser) ((StompPrincipal) principal).authUser();
        ChatMessageResponse response = chatService.sendMessage(authUser.userId(), chatRoomId, request.content());
        chatRedisPublisher.publish(chatRoomId, response, chatService.getRoom(authUser.userId(), chatRoomId));
    }
}
