package com.team7.agora.domain.trade.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.enums.SettlementStatus;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.domain.trade.dto.response.TradeResponse;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private NegoOfferRepository negoOfferRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private com.team7.agora.domain.payment.repository.PaymentRepository paymentRepository;

    @Mock
    private com.team7.agora.domain.chat.repository.ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatRedisPublisher chatRedisPublisher;

    private TradeService tradeService;
    private User seller;
    private User buyer;
    private Product product;

    @BeforeEach
    void setUp() {
        tradeService = new TradeService(
            tradeRepository,
            productRepository,
            userRepository,
            chatRoomRepository,
            negoOfferRepository,
            settlementRepository,
            paymentRepository,
            chatMessageRepository,
            chatRedisPublisher
        );
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
    }

    @Test
    void startTradeRejectsWhenNoAcceptedNego() {
        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findByProductAndSellerAndBuyer(product, seller, buyer)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.startTrade(2L, 10L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void startTradeCreatesTradingTradeWithNegoPriceWhenNegoAccepted() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 50L);
        NegoOffer acceptedOffer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        acceptedOffer.accept();

        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(buyer));
        when(tradeRepository.existsByProductAndStatusNot(product, TradeStatus.CANCELLED)).thenReturn(false);
        when(chatRoomRepository.findByProductAndSellerAndBuyer(product, seller, buyer)).thenReturn(Optional.of(chatRoom));
        when(negoOfferRepository.findFirstByChatRoomIdAndStatusOrderByCreatedAtDesc(50L, NegoOfferStatus.ACCEPTED))
            .thenReturn(Optional.of(acceptedOffer));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade trade = invocation.getArgument(0);
            assignId(trade, 100L);
            return trade;
        });

        TradeResponse response = tradeService.startTrade(2L, 10L);

        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(45000));
        assertThat(response.status()).isEqualTo("PAYMENT_PENDING");
        verify(productRepository).findByIdForUpdateAndDeletedAtIsNull(10L);
    }

    @Test
    void startTradeRejectsNonActiveBuyer() {
        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.startTrade(2L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void createTradeFromAcceptedOfferLocksProductBeforeCreatingTrade() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 50L);
        NegoOffer acceptedOffer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        acceptedOffer.accept();

        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(tradeRepository.existsByProductAndStatusNot(product, TradeStatus.CANCELLED)).thenReturn(false);
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade trade = invocation.getArgument(0);
            assignId(trade, 100L);
            return trade;
        });

        Trade trade = tradeService.createTradeFromAcceptedOffer(product, acceptedOffer);

        assertThat(trade.getId()).isEqualTo(100L);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.RESERVED);
        verify(productRepository).findByIdForUpdateAndDeletedAtIsNull(10L);
    }

    @Test
    void createTradeMapsDuplicateTradeConstraintViolationToBusinessException() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 50L);
        NegoOffer acceptedOffer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        acceptedOffer.accept();

        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(tradeRepository.existsByProductAndStatusNot(product, TradeStatus.CANCELLED)).thenReturn(false);
        when(tradeRepository.save(any(Trade.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate active trade"));

        assertThatThrownBy(() -> tradeService.createTradeFromAcceptedOffer(product, acceptedOffer))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void completeTradeChangesStatusToCompletedAndSettlementToCompleted() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);
        trade.markPaid(); // Set trade status to PAID and product to SOLD

        com.team7.agora.domain.payment.entity.Payment payment = mock(com.team7.agora.domain.payment.entity.Payment.class);
        when(payment.getTrade()).thenReturn(trade);
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(45000));

        Settlement settlement = Settlement.pending(payment);
        assignId(settlement, 200L);
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 50L);

        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));
        when(settlementRepository.findByPaymentTradeId(100L)).thenReturn(Optional.of(settlement));
        when(chatRoomRepository.findByProductAndSellerAndBuyer(product, seller, buyer)).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.save(any(com.team7.agora.domain.chat.entity.ChatMessage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        TradeResponse response = tradeService.completeTrade(2L, 100L);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.READY);
        verify(chatMessageRepository, times(1)).save(any(com.team7.agora.domain.chat.entity.ChatMessage.class));
        verify(chatRedisPublisher, times(1)).publish(eq(50L), any());
    }

    @Test
    void completeTradeRejectsAlreadyCompletedTradeAsBusinessException() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);
        trade.markPaid();
        trade.complete();
        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.completeTrade(2L, 100L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void expireReservationChangesTradeStatusToExpiredAndProductStatusToSelling() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);
        product.markReserved();

        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        AuthUser admin = new AuthUser(99L, "admin@test.com", "ROOT_ADMIN", "관리자");

        TradeResponse response = tradeService.expireReservation(admin, 100L);

        assertThat(response.status()).isEqualTo("EXPIRED");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SELLING);
    }

    @Test
    void expireReservationRejectsNonPaymentPendingTrade() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);
        trade.markPaid();
        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        AuthUser admin = new AuthUser(99L, "admin@test.com", "ROOT_ADMIN", "admin");

        assertThatThrownBy(() -> tradeService.expireReservation(admin, 100L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void expireReservationRejectsNormalUser() {
        AuthUser normalUser = new AuthUser(2L, "buyer@test.com", "ROLE_USER", "구매자");

        assertThatThrownBy(() -> tradeService.expireReservation(normalUser, 100L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getTradeDetailReturnsPaymentAndSettlementStatusForParticipant() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);
        trade.markPaid();

        com.team7.agora.domain.payment.entity.Payment payment = mock(com.team7.agora.domain.payment.entity.Payment.class);
        when(payment.getTrade()).thenReturn(trade);
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(45000));
        when(payment.getStatus()).thenReturn(com.team7.agora.domain.payment.enums.PaymentStatus.PAID);

        Settlement settlement = Settlement.pending(payment);
        assignId(settlement, 200L);

        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));
        when(paymentRepository.findByTrade(trade)).thenReturn(Optional.of(payment));
        when(settlementRepository.findByPaymentTradeId(100L)).thenReturn(Optional.of(settlement));

        var response = tradeService.getTradeDetail(2L, 100L);

        assertThat(response.tradeStatus()).isEqualTo("PAID");
        assertThat(response.paymentStatus()).isEqualTo("PAID");
        assertThat(response.settlementStatus()).isEqualTo("HELD");
    }

    @Test
    void getTradeDetailRejectsNonParticipant() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);

        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeService.getTradeDetail(99L, 100L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void sendRatingRequestMessageSendsMessageForCompletedTrade() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);
        trade.markPaid();
        trade.complete();

        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 50L);

        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));
        when(chatRoomRepository.findByProductAndSellerAndBuyer(product, seller, buyer)).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.save(any(com.team7.agora.domain.chat.entity.ChatMessage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AuthUser system = new AuthUser(0L, "system@test.com", "ROOT_ADMIN", "시스템");

        tradeService.sendRatingRequestMessage(system, 100L);

        verify(chatMessageRepository).save(any(com.team7.agora.domain.chat.entity.ChatMessage.class));
        verify(chatRedisPublisher, times(1)).publish(eq(50L), any());
    }

    @Test
    void sendRatingRequestMessageRejectsNonSystemCaller() {
        AuthUser normalUser = new AuthUser(2L, "buyer@test.com", "ROLE_USER", "구매자");

        assertThatThrownBy(() -> tradeService.sendRatingRequestMessage(normalUser, 100L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void sendRatingRequestMessageRejectsWhenTradeNotCompleted() {
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 100L);

        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        AuthUser system = new AuthUser(0L, "system@test.com", "ROOT_ADMIN", "시스템");

        assertThatThrownBy(() -> tradeService.sendRatingRequestMessage(system, 100L))
            .isInstanceOf(BusinessException.class);
    }
}
