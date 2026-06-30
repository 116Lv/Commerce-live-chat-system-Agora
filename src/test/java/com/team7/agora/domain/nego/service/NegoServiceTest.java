package com.team7.agora.domain.nego.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.chat.service.ChatSystemMessageService;
import com.team7.agora.domain.nego.dto.response.NegoOfferResponse;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.time.AgoraClock;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.mockito.InOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NegoServiceTest {

    @Mock
    private NegoOfferRepository negoOfferRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TradeService tradeService;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private com.team7.agora.domain.payment.repository.PaymentRepository paymentRepository;

    @Mock
    private ChatSystemMessageService chatSystemMessageService;

    private NegoService negoService;
    private User seller;
    private User buyer;
    private User stranger;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        negoService = new NegoService(
            negoOfferRepository,
            chatRoomRepository,
            productRepository,
            tradeService,
            tradeRepository,
            paymentRepository,
            chatSystemMessageService
        );
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        stranger = User.signup("stranger@test.com", "password", "제3자", "01055556666");
        assignId(stranger, 3L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        Product product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
        product.approve();
        chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
    }

    @Test
    void createOfferAllowsBuyerParticipant() {
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(chatRoom.getProduct()));
        when(negoOfferRepository.save(any(NegoOffer.class))).thenAnswer(invocation -> {
            NegoOffer offer = invocation.getArgument(0);
            assignId(offer, 1000L);
            return offer;
        });

        NegoOfferResponse response = negoService.createOffer(2L, 100L, BigDecimal.valueOf(45000));

        assertThat(response.offerId()).isEqualTo(1000L);
        assertThat(response.requesterId()).isEqualTo(2L);
        assertThat(response.offerPrice()).isEqualByComparingTo(BigDecimal.valueOf(45000));
        assertThat(response.status()).isEqualTo("PENDING");
        InOrder inOrder = inOrder(productRepository, negoOfferRepository);
        inOrder.verify(productRepository)
            .findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED);
        inOrder.verify(negoOfferRepository).existsByChatRoomAndRequesterAndStatusIn(
            chatRoom,
            buyer,
            NegoOffer.ACTIVE_STATUSES
        );
        inOrder.verify(negoOfferRepository).save(any(NegoOffer.class));
        verify(chatSystemMessageService).send(chatRoom, buyer, "제안이 생성되었습니다.");
    }

    @Test
    void createOfferSetsExpirationAfter24Hours() {
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(chatRoom.getProduct()));
        when(negoOfferRepository.save(any(NegoOffer.class))).thenAnswer(invocation -> {
            NegoOffer offer = invocation.getArgument(0);
            assignId(offer, 1000L);
            return offer;
        });
        LocalDateTime before = AgoraClock.now();

        NegoOfferResponse response = negoService.createOffer(2L, 100L, BigDecimal.valueOf(45000));

        LocalDateTime after = AgoraClock.now();
        assertThat(response.expiresAt()).isAfterOrEqualTo(before.plusHours(24));
        assertThat(response.expiresAt()).isBeforeOrEqualTo(after.plusHours(24));
    }

    @Test
    void createOfferRejectsZeroOrNegativeOfferPriceWithInvalidRequest() {
        assertInvalidOfferPriceRejected(BigDecimal.ZERO);
        assertInvalidOfferPriceRejected(BigDecimal.valueOf(-1));
    }

    @Test
    void createOfferRejectsListPriceOrHigherOfferPriceWithInvalidRequest() {
        assertInvalidOfferPriceRejected(BigDecimal.valueOf(50000));
        assertInvalidOfferPriceRejected(BigDecimal.valueOf(50001));
    }

    @Test
    void createOfferRejectsWhenProductAlreadyReserved() {
        chatRoom.getProduct().markReserved();
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(chatRoom.getProduct()));

        assertThatThrownBy(() -> negoService.createOffer(2L, 100L, BigDecimal.valueOf(45000)))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void createOfferRejectsWhenRequesterAlreadyHasActiveOfferInRoom() {
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(chatRoom.getProduct()));
        when(negoOfferRepository.existsByChatRoomAndRequesterAndStatusIn(chatRoom, buyer, NegoOffer.ACTIVE_STATUSES))
            .thenReturn(true);

        assertThatThrownBy(() -> negoService.createOffer(2L, 100L, BigDecimal.valueOf(45000)))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void createOfferRejectsWhenLockedProductIsAlreadyReserved() {
        Product reservedProduct = Product.create(
            seller,
            chatRoom.getProduct().getRegion(),
            "reserved",
            "already reserved",
            BigDecimal.valueOf(50000),
            "sports"
        );
        assignId(reservedProduct, 10L);
        reservedProduct.markReserved();
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(reservedProduct));

        assertThatThrownBy(() -> negoService.createOffer(2L, 100L, BigDecimal.valueOf(45000)))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void createOfferUsesApprovedProductLockQueryAndTreatsUnapprovedTargetAsNotFound() {
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> negoService.createOffer(2L, 100L, BigDecimal.valueOf(45000)))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND)
            );
        verify(productRepository)
            .findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED);
        verify(productRepository, never()).findByIdForUpdateAndDeletedAtIsNull(10L);
        verify(negoOfferRepository, never()).save(any(NegoOffer.class));
        verify(chatSystemMessageService, never()).send(any(), any(), any());
    }

    @Test
    void createOfferRejectsSeller() {
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> negoService.createOffer(1L, 100L, BigDecimal.valueOf(45000)))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getCurrentOfferReturnsLatestActiveOfferForParticipant() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(negoOfferRepository.findFirstByChatRoomIdAndStatusInOrderByCreatedAtDesc(100L, NegoService.CURRENT_OFFER_STATUSES))
            .thenReturn(Optional.of(offer));

        NegoOfferResponse response = negoService.getCurrentOffer(2L, 100L);

        assertThat(response.offerId()).isEqualTo(1000L);
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void getCurrentOfferReturnsAcceptedOfferWithTradeIdForParticipant() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.accept();
        Trade trade = Trade.start(chatRoom.getProduct(), seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 2000L);
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(negoOfferRepository.findFirstByChatRoomIdAndStatusInOrderByCreatedAtDesc(100L, NegoService.CURRENT_OFFER_STATUSES))
            .thenReturn(Optional.of(offer));
        when(tradeRepository.findByProductAndBuyer(chatRoom.getProduct(), buyer))
            .thenReturn(Optional.of(trade));

        NegoOfferResponse response = negoService.getCurrentOffer(2L, 100L);

        assertThat(response.offerId()).isEqualTo(1000L);
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.tradeId()).isEqualTo(2000L);
        assertThat(response.tradeStatus()).isEqualTo("PAYMENT_PENDING");
    }

    @Test
    void cancelAcceptedOfferCancelsPaymentPendingTradeAndReopensProduct() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.accept();
        chatRoom.getProduct().markReserved();
        Trade trade = Trade.start(chatRoom.getProduct(), seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 2000L);

        when(negoOfferRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(offer));
        when(tradeRepository.findByProductAndBuyer(chatRoom.getProduct(), buyer)).thenReturn(Optional.of(trade));

        NegoOfferResponse response = negoService.cancelOffer(2L, 1000L);

        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(response.tradeId()).isEqualTo(2000L);
        assertThat(response.tradeStatus()).isEqualTo("CANCELLED");
        assertThat(trade.getStatus()).isEqualTo(com.team7.agora.domain.trade.enums.TradeStatus.CANCELLED);
        assertThat(chatRoom.getProduct().getStatus()).isEqualTo(com.team7.agora.domain.product.enums.ProductStatus.SELLING);
    }

    @Test
    void getCurrentOfferRejectsNonParticipant() {
        when(chatRoomRepository.findByIdAndStatus(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> negoService.getCurrentOffer(3L, 100L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void acceptOfferAllowsSellerOnly() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(negoOfferRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(offer));
        when(tradeService.createTradeFromAcceptedOffer(any(Product.class), any(NegoOffer.class)))
            .thenAnswer(invocation -> {
                Trade trade = Trade.start(chatRoom.getProduct(), seller, buyer, BigDecimal.valueOf(45000));
                assignId(trade, 2000L);
                return trade;
            });
        when(negoOfferRepository.findAllByChatRoomProductIdAndStatusInForUpdate(
            10L,
            NegoOffer.ACTIVE_STATUSES
        )).thenReturn(Collections.emptyList());

        NegoOfferResponse response = negoService.acceptOffer(1L, 1000L);

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.tradeId()).isEqualTo(2000L);
    }

    @Test
    void acceptOfferRejectsNonSeller() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(negoOfferRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.acceptOffer(2L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void acceptOfferChangesStatusesAndCreatesTradeAndCancelsOtherOffers() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        
        ChatRoom otherChatRoom = ChatRoom.open(chatRoom.getProduct(), stranger);
        assignId(otherChatRoom, 101L);
        NegoOffer otherOffer = NegoOffer.create(otherChatRoom, stranger, BigDecimal.valueOf(48000));
        assignId(otherOffer, 1001L);
        
        when(negoOfferRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(offer));
        when(tradeService.createTradeFromAcceptedOffer(any(Product.class), any(NegoOffer.class)))
            .thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                NegoOffer acceptedOffer = invocation.getArgument(1);
                product.markReserved();
                return Trade.start(product, product.getSeller(), acceptedOffer.getRequester(), acceptedOffer.getOfferPrice());
            });
        when(negoOfferRepository.findAllByChatRoomProductIdAndStatusInForUpdate(
            10L,
            NegoOffer.ACTIVE_STATUSES
        )).thenReturn(java.util.Arrays.asList(offer, otherOffer));

        NegoOfferResponse response = negoService.acceptOffer(1L, 1000L);

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(chatRoom.getProduct().getStatus()).isEqualTo(com.team7.agora.domain.product.enums.ProductStatus.RESERVED);
        assertThat(otherOffer.getStatus()).isEqualTo(NegoOfferStatus.CANCELLED);
        verify(tradeService).createTradeFromAcceptedOffer(chatRoom.getProduct(), offer);
        verify(chatSystemMessageService).send(chatRoom, seller, "판매자가 제안을 최종 승인했습니다.");
        verify(chatSystemMessageService).send(otherChatRoom, seller, "판매자가 다른 구매자를 선택했습니다.");
    }

    @Test
    void acceptOfferRejectsWhenProductAlreadyHasActiveTrade() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(negoOfferRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(offer));
        when(tradeService.createTradeFromAcceptedOffer(any(Product.class), any(NegoOffer.class)))
            .thenThrow(new BusinessException(ErrorCode.CONFLICT, "이미 진행 중이거나 완료된 거래입니다."));

        assertThatThrownBy(() -> negoService.acceptOffer(1L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectOfferRejectsAlreadyRespondedOfferAsBusinessException() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.accept();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.rejectOffer(1L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void requestExtensionAllowsBuyerAndMarksExtensionRequested() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        NegoOfferResponse response = negoService.requestExtension(2L, 1000L);

        assertThat(response.status()).isEqualTo("EXTENSION_REQUESTED");
    }

    @Test
    void requestExtensionRejectsNonBuyer() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.requestExtension(1L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void approveExtensionAllowsSellerAndAdds24Hours() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.requestExtension();
        LocalDateTime previousExpiresAt = offer.getExpiresAt();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        NegoOfferResponse response = negoService.approveExtension(1L, 1000L);

        assertThat(response.status()).isEqualTo("EXTENDED");
        assertThat(Duration.between(previousExpiresAt, response.expiresAt())).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void requestExtensionRejectsAlreadyExtendedOffer() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.requestExtension();
        offer.approveExtension();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.requestExtension(2L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void approveExtensionRejectsAlreadyExtendedOffer() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.requestExtension();
        offer.approveExtension();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.approveExtension(1L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void requestExtensionRejectsClosedOffer() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.accept();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.requestExtension(2L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void approveExtensionRejectsClosedOffer() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.accept();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.approveExtension(1L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectExtensionAllowsSellerAndKeepsExpiration() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.requestExtension();
        LocalDateTime previousExpiresAt = offer.getExpiresAt();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        NegoOfferResponse response = negoService.rejectExtension(1L, 1000L);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.expiresAt()).isEqualTo(previousExpiresAt);
    }

    @Test
    void requestExtensionRejectsSecondRequestAfterRejection() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        offer.requestExtension();
        offer.rejectExtension();
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.requestExtension(2L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectExtensionRejectsNonExtensionRequestedOfferAsBusinessException() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> negoService.rejectExtension(1L, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void expireOfferAllowsRootAdminAndMarksExpiredWhenDue() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        ReflectionTestUtils.setField(offer, "expiresAt", AgoraClock.now().minusHours(1));
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));
        AuthUser rootAdmin = new AuthUser(99L, "root@admin.com", "ROOT_ADMIN", "최고관리자");

        NegoOfferResponse response = negoService.expireOffer(rootAdmin, 1000L);

        assertThat(response.status()).isEqualTo("EXPIRED");
    }

    @Test
    void expireOfferRejectsNonRootAdmin() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        AuthUser regularUser = new AuthUser(2L, "buyer@test.com", "USER", "구매자");

        assertThatThrownBy(() -> negoService.expireOffer(regularUser, 1000L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void expireOfferRejectsWhenNotYetExpired() {
        NegoOffer offer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(offer, 1000L);
        when(negoOfferRepository.findById(1000L)).thenReturn(Optional.of(offer));
        AuthUser rootAdmin = new AuthUser(99L, "root@admin.com", "ROOT_ADMIN", "최고관리자");

        assertThatThrownBy(() -> negoService.expireOffer(rootAdmin, 1000L))
            .isInstanceOf(IllegalStateException.class);
    }

    private void assertInvalidOfferPriceRejected(BigDecimal offerPrice) {
        when(chatRoomRepository.findByIdAndStatusForUpdate(100L, ChatRoomStatus.ACTIVE))
            .thenReturn(Optional.of(chatRoom));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(10L, ProductApprovalStatus.APPROVED))
            .thenReturn(Optional.of(chatRoom.getProduct()));
        lenient().when(negoOfferRepository.save(any(NegoOffer.class))).thenAnswer(invocation -> {
            NegoOffer offer = invocation.getArgument(0);
            assignId(offer, 1000L);
            return offer;
        });

        assertThatThrownBy(() -> negoService.createOffer(2L, 100L, offerPrice))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST)
            );
    }
}
