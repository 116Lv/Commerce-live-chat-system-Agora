package com.team7.agora.domain.chat.service;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.dto.response.ChatRoomResponse;
import com.team7.agora.domain.chat.entity.ChatMessage;
import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.chat.repository.ChatMessageRepository;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.storage.ImageStorageClient;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ImageStorageClient imageStorageClient;

    public ChatService(
        ChatRoomRepository chatRoomRepository,
        ChatMessageRepository chatMessageRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        ImageStorageClient imageStorageClient
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.imageStorageClient = imageStorageClient;
    }

    @Transactional
    public ChatRoomResponse openRoom(Long userId, Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (product.isSeller(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "내 상품에는 채팅을 시작할 수 없습니다.");
        }

        User buyer = findUser(userId);
        ChatRoom chatRoom = chatRoomRepository.findByProductAndSellerAndBuyer(product, product.getSeller(), buyer)
            .orElseGet(() -> chatRoomRepository.save(ChatRoom.open(product, buyer)));

        return ChatRoomResponse.from(chatRoom);
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long chatRoomId, String content) {
        ChatRoom chatRoom = findActiveRoom(chatRoomId);

        if (!chatRoom.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 메시지를 보낼 수 있습니다.");
        }

        User sender = findUser(userId);
        ChatMessage message = chatMessageRepository.save(ChatMessage.send(chatRoom, sender, content));
        return ChatMessageResponse.from(message);
    }

    @Transactional
    public ChatMessageResponse sendImageMessage(Long userId, Long chatRoomId, MultipartFile image) {
        ChatRoom chatRoom = findActiveRoom(chatRoomId);

        if (!chatRoom.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 이미지를 보낼 수 있습니다.");
        }

        User sender = findUser(userId);
        String imageUrl = imageStorageClient.store("chat", image);
        ChatMessage message = chatMessageRepository.save(ChatMessage.sendImage(chatRoom, sender, imageUrl));
        return ChatMessageResponse.from(message);
    }

    public List<ChatMessageResponse> getMessages(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = findActiveRoom(chatRoomId);

        if (!chatRoom.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 메시지를 조회할 수 있습니다.");
        }

        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom).stream()
            .map(ChatMessageResponse::from)
            .toList();
    }

    public List<ChatRoomResponse> getMyRooms(Long userId) {
        User user = findUser(userId);
        return chatRoomRepository.findAllBySellerOrBuyer(user, user).stream()
            .map(ChatRoomResponse::from)
            .toList();
    }

    @Transactional
    public ChatRoomResponse markRead(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = findActiveRoom(chatRoomId);

        if (!chatRoom.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 읽음 처리를 할 수 있습니다.");
        }

        chatRoom.markRead(userId);
        return ChatRoomResponse.from(chatRoom);
    }

    private ChatRoom findActiveRoom(Long chatRoomId) {
        return chatRoomRepository.findByIdAndStatus(chatRoomId, ChatRoomStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
