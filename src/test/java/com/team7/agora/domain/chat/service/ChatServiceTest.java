package com.team7.agora.domain.chat.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.storage.ImageStorageClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageClient imageStorageClient;

    private ChatService chatService;
    private User seller;
    private User buyer;
    private User stranger;
    private Product product;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
            chatRoomRepository,
            chatMessageRepository,
            productRepository,
            productImageRepository,
            userRepository,
            imageStorageClient
        );
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        stranger = User.signup("stranger@test.com", "password", "제3자", "01055556666");
        assignId(stranger, 3L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
        product.approve();
    }

    @Test
    void openRoomCreatesRoomForBuyerAndSeller() {
        when(productRepository.findByIdAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findByProductAndSellerAndBuyer(product, seller, buyer)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom chatRoom = invocation.getArgument(0);
            assignId(chatRoom, 100L);
            return chatRoom;
        });

        ChatRoomResponse response = chatService.openRoom(2L, 10L);

        assertThat(response.chatRoomId()).isEqualTo(100L);
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.buyerId()).isEqualTo(2L);
    }

    @Test
    void openRoomReturnsExistingRoomWhenConcurrentCreateConflicts() {
        ChatRoom existingRoom = ChatRoom.open(product, buyer);
        assignId(existingRoom, 100L);

        when(productRepository.findByIdAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findByProductAndSellerAndBuyer(product, seller, buyer))
            .thenReturn(Optional.empty(), Optional.of(existingRoom));
        when(chatRoomRepository.save(any(ChatRoom.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate chat room"));

        ChatRoomResponse response = chatService.openRoom(2L, 10L);

        assertThat(response.chatRoomId()).isEqualTo(100L);
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.buyerId()).isEqualTo(2L);
    }

    @Test
    void openRoomRejectsProductSeller() {
        when(productRepository.findByIdAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> chatService.openRoom(1L, 10L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void openRoomRejectsNonActiveBuyer() {
        when(productRepository.findByIdAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.openRoom(2L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void openRoomUsesApprovedProductQueryAndTreatsUnapprovedBuyerTargetAsNotFound() {
        when(productRepository.findByIdAndDeletedAtIsNullAndApprovalStatus(11L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.empty());
        when(productRepository.existsByIdAndSellerIdAndDeletedAtIsNull(11L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> chatService.openRoom(2L, 11L))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND)
            );
        verify(productRepository).findByIdAndDeletedAtIsNullAndApprovalStatus(11L, ProductApprovalStatus.APPROVED);
        verify(productRepository, never()).findByIdAndDeletedAtIsNull(11L);
        verify(userRepository, never()).findByIdAndStatusAndDeletedAtIsNull(any(), any());
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void openRoomPreservesSellerSelfChatRejectionWithoutLoadingUnapprovedProduct() {
        when(productRepository.findByIdAndDeletedAtIsNullAndApprovalStatus(11L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.empty());
        when(productRepository.existsByIdAndSellerIdAndDeletedAtIsNull(11L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> chatService.openRoom(1L, 11L))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST)
            );
        verify(productRepository, never()).findByIdAndDeletedAtIsNull(11L);
        verify(userRepository, never()).findByIdAndStatusAndDeletedAtIsNull(any(), any());
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void sendMessageRejectsNonParticipant() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.sendMessage(3L, 100L, "안녕하세요"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void sendMessageSavesParticipantMessage() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(buyer));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessageResponse response = chatService.sendMessage(2L, 100L, "거래 가능할까요?");

        assertThat(response.chatRoomId()).isEqualTo(100L);
        assertThat(response.senderId()).isEqualTo(2L);
        assertThat(response.content()).isEqualTo("거래 가능할까요?");

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSender()).isEqualTo(buyer);
    }

    @Test
    void sendImageMessageSavesParticipantImageMessage() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        MultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "data".getBytes());
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(buyer));
        when(imageStorageClient.store("chat", image)).thenReturn("/uploads/chat/abc.jpg");
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessageResponse response = chatService.sendImageMessage(2L, 100L, image);

        assertThat(response.content()).isEqualTo("/uploads/chat/abc.jpg");
        assertThat(response.messageType()).isEqualTo("IMAGE");
    }

    @Test
    void sendImageMessageRejectsNonParticipant() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        MultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "data".getBytes());
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.sendImageMessage(3L, 100L, image))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getMessagesWithoutCursorFetchesLatestPage() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        ChatMessage message = ChatMessage.send(chatRoom, buyer, "안녕하세요");
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.findLatestByChatRoom(eq(chatRoom), any(Pageable.class)))
            .thenReturn(List.of(message));

        List<ChatMessageResponse> responses = chatService.getMessages(2L, 100L, null, 20);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).content()).isEqualTo("안녕하세요");
    }

    @Test
    void getMessagesWithCursorFetchesMessagesBeforeLastMessageId() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        ChatMessage message = ChatMessage.send(chatRoom, buyer, "이전 메시지");
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.findByChatRoomBeforeMessageId(eq(chatRoom), eq(50L), any(Pageable.class)))
            .thenReturn(List.of(message));

        List<ChatMessageResponse> responses = chatService.getMessages(2L, 100L, 50L, 20);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).content()).isEqualTo("이전 메시지");
    }

    @Test
    void getMessagesRejectsNonParticipant() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.getMessages(3L, 100L, null, 20))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getMyRoomsReturnsRoomsForParticipant() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findAllBySellerOrBuyer(buyer, buyer)).thenReturn(List.of(chatRoom));

        var responses = chatService.getMyRooms(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).chatRoomId()).isEqualTo(100L);
    }

    @Test
    void getMyRoomsIncludesProductParticipantsLastMessageAndUnreadCount() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        ChatMessage message = ChatMessage.send(chatRoom, seller, "Still available?");
        assignId(message, 900L);
        ProductImage image = ProductImage.create(product, "/uploads/products/bike.jpg", 0);

        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findAllBySellerOrBuyer(buyer, buyer)).thenReturn(List.of(chatRoom));
        when(productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(List.of(10L)))
            .thenReturn(List.of(image));
        when(chatMessageRepository.findLatestMessagesByChatRoomIds(List.of(100L))).thenReturn(List.of(message));
        when(chatMessageRepository.countUnreadMessagesByChatRoomIds(List.of(100L), 2L))
            .thenReturn(List.of(new ChatRoomUnreadCount(100L, 2L)));

        var responses = chatService.getMyRooms(2L);

        assertThat(responses).hasSize(1);
        ChatRoomResponse response = responses.get(0);
        assertThat(response.productTitle()).isEqualTo(product.getTitle());
        assertThat(response.productPrice()).isEqualByComparingTo(product.getPrice());
        assertThat(response.productStatus()).isEqualTo(product.getStatus().name());
        assertThat(response.productThumbnailUrl()).isEqualTo("/uploads/products/bike.jpg");
        assertThat(response.sellerNickname()).isEqualTo(seller.getNickname());
        assertThat(response.buyerNickname()).isEqualTo(buyer.getNickname());
        assertThat(response.lastMessageId()).isEqualTo(900L);
        assertThat(response.lastMessagePreview()).isEqualTo("Still available?");
        assertThat(response.lastMessageType()).isEqualTo("TEXT");
        assertThat(response.lastMessageSenderId()).isEqualTo(1L);
        assertThat(response.lastMessageSenderNickname()).isEqualTo(seller.getNickname());
        assertThat(response.lastMessageCreatedAt()).isNotNull();
        assertThat(response.unreadCount()).isEqualTo(2L);
        verify(chatMessageRepository, never()).findFirstByChatRoomOrderByIdDesc(any(ChatRoom.class));
        verify(chatMessageRepository, never()).countUnreadMessagesForUser(any(ChatRoom.class), eq(2L), any());
    }

    @Test
    void getMyRoomsBatchesMessageMetadataForMultipleRooms() {
        Product secondProduct = Product.create(seller, product.getRegion(), "Helmet", "Clean", BigDecimal.valueOf(15000), "Sports");
        assignId(secondProduct, 11L);
        ChatRoom firstRoom = ChatRoom.open(product, buyer);
        assignId(firstRoom, 100L);
        ChatRoom secondRoom = ChatRoom.open(secondProduct, buyer);
        assignId(secondRoom, 101L);
        ChatMessage firstMessage = ChatMessage.send(firstRoom, seller, "First room");
        assignId(firstMessage, 900L);
        ChatMessage secondMessage = ChatMessage.send(secondRoom, seller, "Second room");
        assignId(secondMessage, 901L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findAllBySellerOrBuyer(buyer, buyer)).thenReturn(List.of(firstRoom, secondRoom));
        when(chatMessageRepository.findLatestMessagesByChatRoomIds(List.of(100L, 101L)))
            .thenReturn(List.of(firstMessage, secondMessage));
        when(chatMessageRepository.countUnreadMessagesByChatRoomIds(List.of(100L, 101L), 2L))
            .thenReturn(List.of(new ChatRoomUnreadCount(100L, 1L), new ChatRoomUnreadCount(101L, 3L)));

        var responses = chatService.getMyRooms(2L);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ChatRoomResponse::lastMessagePreview)
            .containsExactly("First room", "Second room");
        assertThat(responses).extracting(ChatRoomResponse::unreadCount)
            .containsExactly(1L, 3L);
        verify(chatMessageRepository).findLatestMessagesByChatRoomIds(List.of(100L, 101L));
        verify(chatMessageRepository).countUnreadMessagesByChatRoomIds(List.of(100L, 101L), 2L);
        verify(chatMessageRepository, never()).findFirstByChatRoomOrderByIdDesc(any(ChatRoom.class));
        verify(chatMessageRepository, never()).countUnreadMessagesForUser(any(ChatRoom.class), eq(2L), any());
        verify(productImageRepository).findAllByProductIdInOrderByProductIdAscSortOrderAsc(List.of(10L, 11L));
    }

    @Test
    void getMyRoomsAllowsDeletedUserForHistoryView() {
        buyer.changeStatus(UserStatus.DELETED);
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findAllBySellerOrBuyer(buyer, buyer)).thenReturn(List.of(chatRoom));

        var responses = chatService.getMyRooms(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).chatRoomId()).isEqualTo(100L);
    }

    @Test
    void markReadUpdatesParticipantReadTimestamp() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));

        ChatRoomResponse response = chatService.markRead(2L, 100L);

        assertThat(response.chatRoomId()).isEqualTo(100L);
    }

    @Test
    void markReadRejectsNonParticipant() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.markRead(3L, 100L))
            .isInstanceOf(BusinessException.class);
    }
}
