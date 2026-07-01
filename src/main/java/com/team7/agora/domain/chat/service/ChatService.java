package com.team7.agora.domain.chat.service;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.dto.response.ChatRoomResponse;
import com.team7.agora.domain.chat.entity.ChatMessage;
import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.chat.repository.ChatMessageRepository;
import com.team7.agora.domain.chat.repository.ChatRoomUnreadCount;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.storage.ImageStorageClient;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ProductImageRepository productImageRepository;
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
        ProductImageRepository productImageRepository,
        UserRepository userRepository,
        ImageStorageClient imageStorageClient
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
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
        Product product = productRepository.findByIdAndDeletedAtIsNullAndApprovalStatus(productId, ProductApprovalStatus.APPROVED)
            .or(() -> rejectSellerSelfChat(userId, productId))
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (product.isSeller(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "내 상품에는 채팅을 시작할 수 없습니다.");
        }

        if (product.getApprovalStatus() != ProductApprovalStatus.APPROVED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only approved products can open chat rooms.");
        }

        User buyer = findActiveUser(userId);
        ChatRoom chatRoom = chatRoomRepository.findByProductAndSellerAndBuyer(product, product.getSeller(), buyer)
            .orElseGet(() -> createRoomOrFindExisting(product, buyer));

        return toRoomResponse(chatRoom, userId);
    }

    private Optional<Product> rejectSellerSelfChat(Long userId, Long productId) {
        if (productRepository.existsByIdAndSellerIdAndDeletedAtIsNull(productId, userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "내 상품에는 채팅을 시작할 수 없습니다.");
        }
        return Optional.empty();
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

        User sender = findActiveUser(userId);
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

        User sender = findActiveUser(userId);
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
        findUser(userId);
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByParticipantIdAndStatus(userId, ChatRoomStatus.ACTIVE)
            .stream()
            .filter(chatRoom -> chatRoom.isParticipant(userId))
            .toList();
        Map<Long, String> primaryImageUrls = findPrimaryImageUrls(chatRooms);
        Map<Long, ChatMessage> lastMessages = findLastMessages(chatRooms);
        Map<Long, Long> unreadCounts = countUnreadMessages(chatRooms, userId);

        return chatRooms.stream()
            .map(chatRoom -> toRoomResponse(
                chatRoom,
                primaryImageUrls.get(chatRoom.getProduct().getId()),
                lastMessages.get(chatRoom.getId()),
                unreadCounts.getOrDefault(chatRoom.getId(), 0L)
            ))
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
        return toRoomResponse(chatRoom, userId);
    }

    private ChatRoomResponse toRoomResponse(ChatRoom chatRoom, Long viewerId) {
        String primaryImageUrl = findPrimaryImageUrls(List.of(chatRoom)).get(chatRoom.getProduct().getId());
        return toRoomResponse(chatRoom, viewerId, primaryImageUrl);
    }

    private ChatRoomResponse toRoomResponse(ChatRoom chatRoom, Long viewerId, String productThumbnailUrl) {
        Optional<ChatMessage> lastMessage = findLastMessage(chatRoom);
        long unreadCount = countUnreadMessages(chatRoom, viewerId);

        return toRoomResponse(chatRoom, productThumbnailUrl, lastMessage.orElse(null), unreadCount);
    }

    private ChatRoomResponse toRoomResponse(
        ChatRoom chatRoom,
        String productThumbnailUrl,
        ChatMessage lastMessage,
        long unreadCount
    ) {
        return ChatRoomResponse.from(chatRoom, productThumbnailUrl, lastMessage, unreadCount);
    }

    private Optional<ChatMessage> findLastMessage(ChatRoom chatRoom) {
        Optional<ChatMessage> lastMessage = chatMessageRepository.findFirstByChatRoomOrderByIdDesc(chatRoom);
        return lastMessage == null ? Optional.empty() : lastMessage;
    }

    private long countUnreadMessages(ChatRoom chatRoom, Long viewerId) {
        LocalDateTime lastReadAt = null;

        if (chatRoom.getSeller().getId().equals(viewerId)) {
            lastReadAt = chatRoom.getSellerLastReadAt();
        } else if (chatRoom.getBuyer().getId().equals(viewerId)) {
            lastReadAt = chatRoom.getBuyerLastReadAt();
        }

        return chatMessageRepository.countUnreadMessagesForUser(chatRoom, viewerId, lastReadAt);
    }

    private Map<Long, ChatMessage> findLastMessages(List<ChatRoom> chatRooms) {
        List<Long> chatRoomIds = chatRooms.stream()
            .map(ChatRoom::getId)
            .distinct()
            .toList();
        Map<Long, ChatMessage> lastMessages = new LinkedHashMap<>();

        if (chatRoomIds.isEmpty()) {
            return lastMessages;
        }

        List<ChatMessage> messages = chatMessageRepository.findLatestMessagesByChatRoomIds(chatRoomIds);
        if (messages == null) {
            return lastMessages;
        }

        for (ChatMessage message : messages) {
            lastMessages.put(message.getChatRoom().getId(), message);
        }

        return lastMessages;
    }

    private Map<Long, Long> countUnreadMessages(List<ChatRoom> chatRooms, Long viewerId) {
        List<Long> chatRoomIds = chatRooms.stream()
            .map(ChatRoom::getId)
            .distinct()
            .toList();
        Map<Long, Long> unreadCounts = new LinkedHashMap<>();

        if (chatRoomIds.isEmpty()) {
            return unreadCounts;
        }

        List<ChatRoomUnreadCount> counts = chatMessageRepository.countUnreadMessagesByChatRoomIds(chatRoomIds, viewerId);
        if (counts == null) {
            return unreadCounts;
        }

        for (ChatRoomUnreadCount count : counts) {
            unreadCounts.put(count.chatRoomId(), count.unreadCount() == null ? 0L : count.unreadCount());
        }

        return unreadCounts;
    }

    private Map<Long, String> findPrimaryImageUrls(List<ChatRoom> chatRooms) {
        List<Long> productIds = chatRooms.stream()
            .map(chatRoom -> chatRoom.getProduct().getId())
            .distinct()
            .toList();
        Map<Long, String> primaryImageUrls = new LinkedHashMap<>();

        if (productIds.isEmpty()) {
            return primaryImageUrls;
        }

        List<ProductImage> images = productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(productIds);
        if (images == null) {
            return primaryImageUrls;
        }

        for (ProductImage image : images) {
            primaryImageUrls.putIfAbsent(image.getProduct().getId(), image.getImageUrl());
        }

        return primaryImageUrls;
    }

    private ChatRoom findActiveRoom(Long chatRoomId) {
        return chatRoomRepository.findByIdAndStatus(chatRoomId, ChatRoomStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByIdAndStatusAndDeletedAtIsNull(userId, UserStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
