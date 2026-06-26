package com.team7.agora.domain.chat.controller;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.dto.response.ChatRoomResponse;
import com.team7.agora.domain.chat.dto.request.ChatRoomOpenRequest;
import com.team7.agora.domain.chat.service.ChatService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller that exposes chat room endpoints.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * Creates a chat room controller instance.
     * @param chatService the chat service value
     */
    public ChatRoomController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Handles open room behavior.
     * @param authUser the auth user value
     * @param productId the product id value
     * @return the open room result
     */
    @PostMapping("/rooms/products/{productId}")
    public ApiResponse<ChatRoomResponse> openRoom(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId
    ) {
        return openRoomResponse(userDetails, productId);
    }

    /**
     * Handles open room by request behavior.
     * @param authUser the auth user value
     * @param request the request value
     * @return the open room by request result
     */
    @PostMapping("/rooms")
    public ApiResponse<ChatRoomResponse> openRoomByRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ChatRoomOpenRequest request
    ) {
        return openRoomResponse(userDetails, request.productId());
    }

    /**
     * Handles send image behavior.
     * @param authUser the auth user value
     * @param chatRoomId the chat room id value
     * @param image the image value
     * @return the send image result
     */
    @PostMapping("/rooms/{chatRoomId}/images")
    public ApiResponse<ChatMessageResponse> sendImage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long chatRoomId,
        @RequestParam("image") MultipartFile image
    ) {
        ChatMessageResponse response = chatService.sendImageMessage(userDetails.getUserId(), chatRoomId, image);
        return ApiResponse.success("이미지 메시지를 전송했습니다.", response);
    }

    /**
     * Returns messages data.
     * @param authUser the auth user value
     * @param chatRoomId the chat room id value
     * @return the get messages result
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long chatRoomId
    ) {
        List<ChatMessageResponse> responses = chatService.getMessages(userDetails.getUserId(), chatRoomId);
        return ApiResponse.success("채팅 메시지 목록을 조회했습니다.", responses);
    }

    /**
     * Returns my rooms data.
     * @param authUser the auth user value
     * @return the get my rooms result
     */
    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResponse>> getMyRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatRoomResponse> responses = chatService.getMyRooms(userDetails.getUserId());
        return ApiResponse.success("채팅방 목록을 조회했습니다.", responses);
    }

    /**
     * Marks read state.
     * @param authUser the auth user value
     * @param chatRoomId the chat room id value
     * @return the mark read result
     */
    @PatchMapping("/rooms/{chatRoomId}/read")
    public ApiResponse<ChatRoomResponse> markRead(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long chatRoomId
    ) {
        ChatRoomResponse response = chatService.markRead(userDetails.getUserId(), chatRoomId);
        return ApiResponse.success("채팅방을 읽음 처리했습니다.", response);
    }

    private ApiResponse<ChatRoomResponse> openRoomResponse(CustomUserDetails userDetails, Long productId) {
        ChatRoomResponse response = chatService.openRoom(userDetails.getUserId(), productId);
        return ApiResponse.success("채팅방이 준비되었습니다.", response);
    }
}
