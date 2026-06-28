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
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.storage.ImageStorageClient;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Chat 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ImageStorageClient imageStorageClient;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param chatRoomRepository 데이터를 조회하고 저장하는 리포지토리
     * @param chatMessageRepository 데이터를 조회하고 저장하는 리포지토리
     * @param productRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param imageStorageClient 외부 시스템 또는 저장소와 통신하는 클라이언트
     */
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

    /**
     * 상품 구매자가 판매자와 대화할 채팅방을 찾거나 새로 생성한다.
     * @param userId 회원 ID
     * @param productId 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ChatRoomResponse openRoom(Long userId, Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (product.isSeller(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "내 상품에는 채팅을 시작할 수 없습니다.");
        }

        User buyer = findUser(userId);
        ChatRoom chatRoom = chatRoomRepository.findByProductAndSellerAndBuyer(product, product.getSeller(), buyer)
            .orElseGet(() -> createRoomOrFindExisting(product, buyer));

        return ChatRoomResponse.from(chatRoom);
    }

    private ChatRoom createRoomOrFindExisting(Product product, User buyer) {
        try {
            return chatRoomRepository.save(ChatRoom.open(product, buyer));
        } catch (DataIntegrityViolationException e) {
            return chatRoomRepository.findByProductAndSellerAndBuyer(product, product.getSeller(), buyer)
                .orElseThrow(() -> e);
        }
    }

    /**
     * 채팅방 참여자인지 확인한 뒤 텍스트 채팅 메시지를 저장한다.
     * @param userId 회원 ID
     * @param chatRoomId 채팅방 ID
     * @param content 내용
     * @return 클라이언트에 반환할 API 응답
     */
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

    /**
     * 채팅방 참여자인지 확인한 뒤 이미지를 저장하고 이미지 메시지를 생성한다.
     * @param userId 회원 ID
     * @param chatRoomId 채팅방 ID
     * @param image 업로드할 이미지 파일
     * @return 클라이언트에 반환할 API 응답
     */
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

    public List<ChatMessageResponse> getMessages(Long userId, Long chatRoomId, Long lastMessageId, int size) {
        ChatRoom chatRoom = findActiveRoom(chatRoomId);

        if (!chatRoom.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 메시지를 조회할 수 있습니다.");
        }

        Pageable pageable = PageRequest.of(0, size);
        List<ChatMessage> messages = lastMessageId == null
            ? chatMessageRepository.findLatestByChatRoom(chatRoom, pageable)
            : chatMessageRepository.findByChatRoomBeforeMessageId(chatRoom, lastMessageId, pageable);

        return messages.stream()
            .map(ChatMessageResponse::from)
            .toList();
    }

    /**
     * 사용자가 판매자 또는 구매자로 참여 중인 채팅방 목록을 조회한다.
     * @param userId 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public List<ChatRoomResponse> getMyRooms(Long userId) {
        User user = findUser(userId);
        return chatRoomRepository.findAllBySellerOrBuyer(user, user).stream()
            .map(ChatRoomResponse::from)
            .toList();
    }

    /**
     * 사용자가 채팅방 메시지를 읽은 시각을 갱신해 읽음 상태로 표시한다.
     * @param userId 회원 ID
     * @param chatRoomId 채팅방 ID
     * @return 클라이언트에 반환할 API 응답
     */
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
        return userRepository.findByIdAndStatusAndDeletedAtIsNull(userId, UserStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
