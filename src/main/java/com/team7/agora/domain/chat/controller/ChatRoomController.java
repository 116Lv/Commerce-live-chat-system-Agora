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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
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
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param productId 입력 값
     * @return 처리 결과
     */
    @PostMapping("/rooms/products/{productId}")
    public ApiResponse<ChatRoomResponse> openRoom(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId
    ) {
        return openRoomResponse(userDetails, productId);
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param request 입력 값
     * @return 처리 결과
     */
    @PostMapping("/rooms")
    public ApiResponse<ChatRoomResponse> openRoomByRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ChatRoomOpenRequest request
    ) {
        return openRoomResponse(userDetails, request.productId());
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param chatRoomId 입력 값
     * @param image 입력 값
     * @return 처리 결과
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
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @param chatRoomId 입력 값
     * @return 처리 결과
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
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @return 처리 결과
     */
    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResponse>> getMyRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatRoomResponse> responses = chatService.getMyRooms(userDetails.getUserId());
        return ApiResponse.success("채팅방 목록을 조회했습니다.", responses);
    }

    /**
     * 상태를 변경한다.
     * @param userDetails 입력 값
     * @param chatRoomId 입력 값
     * @return 처리 결과
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
