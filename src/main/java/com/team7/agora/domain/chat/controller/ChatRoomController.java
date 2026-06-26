package com.team7.agora.domain.chat.controller;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.dto.response.ChatRoomResponse;
import com.team7.agora.domain.chat.dto.request.ChatRoomOpenRequest;
import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.service.ChatService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
 * 채팅방 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@Validated
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatService chatService;
    private final ChatRedisPublisher chatRedisPublisher;

    public ChatRoomController(ChatService chatService, ChatRedisPublisher chatRedisPublisher) {
        this.chatService = chatService;
        this.chatRedisPublisher = chatRedisPublisher;
    }

    /**
     * 채팅방을 열거나 준비하는 POST /api/chat/rooms/products/{productId} 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param productId 대상 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/rooms/products/{productId}")
    public ApiResponse<ChatRoomResponse> openRoom(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId
    ) {
        return openRoomResponse(userDetails, productId);
    }

    /**
     * 채팅방을 열거나 준비하는 POST /api/chat/rooms 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/rooms")
    public ApiResponse<ChatRoomResponse> openRoomByRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ChatRoomOpenRequest request
    ) {
        return openRoomResponse(userDetails, request.productId());
    }

    /**
     * 채팅방 메시지를 전송하는 POST /api/chat/rooms/{chatRoomId}/images 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param chatRoomId 대상 채팅방 ID
     * @param image 업로드할 이미지 파일
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/rooms/{chatRoomId}/images")
    public ApiResponse<ChatMessageResponse> sendImage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long chatRoomId,
        @RequestParam("image") MultipartFile image
    ) {
        ChatMessageResponse response = chatService.sendImageMessage(userDetails.getUserId(), chatRoomId, image);
        chatRedisPublisher.publish(chatRoomId, response);
        return ApiResponse.success("이미지 메시지를 전송했습니다.", response);
    }

    /**
     * 채팅방 정보를 조회하는 GET /api/chat/rooms/{chatRoomId}/messages 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param chatRoomId 대상 채팅방 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long chatRoomId,
        @RequestParam(required = false) Long lastMessageId,
        @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
        @Max(value = 500, message = "조회 크기는 500 이하여야 합니다.")
        @RequestParam(defaultValue = "20") int size
    ) {
        List<ChatMessageResponse> responses = chatService.getMessages(userDetails.getUserId(), chatRoomId, lastMessageId, size);
        return ApiResponse.success("채팅 메시지 목록을 조회했습니다.", responses);
    }

    /**
     * 채팅방 정보를 조회하는 GET /api/chat/rooms 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResponse>> getMyRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatRoomResponse> responses = chatService.getMyRooms(userDetails.getUserId());
        return ApiResponse.success("채팅방 목록을 조회했습니다.", responses);
    }

    /**
     * 채팅방 상태를 변경하는 PATCH /api/chat/rooms/{chatRoomId}/read 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param chatRoomId 대상 채팅방 ID
     * @return 클라이언트에 반환할 API 응답
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
