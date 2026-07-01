package com.team7.agora.domain.trade.service;

import com.team7.agora.domain.chat.dto.response.ChatMessageResponse;
import com.team7.agora.domain.chat.entity.ChatMessage;
import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.repository.ChatMessageRepository;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.domain.trade.dto.request.MyTradeRole;
import com.team7.agora.domain.trade.dto.response.MyTradeResponse;
import com.team7.agora.domain.trade.dto.response.TradeDetailResponse;
import com.team7.agora.domain.trade.dto.response.TradeResponse;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.trade.repository.TradeQueryRepository;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 거래 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeQueryRepository tradeQueryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final NegoOfferRepository negoOfferRepository;
    private final SettlementRepository settlementRepository;
    private final PaymentRepository paymentRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRedisPublisher chatRedisPublisher;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param tradeRepository 데이터를 조회하고 저장하는 리포지토리
     * @param productRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param chatRoomRepository 데이터를 조회하고 저장하는 리포지토리
     * @param negoOfferRepository 데이터를 조회하고 저장하는 리포지토리
     * @param settlementRepository 데이터를 조회하고 저장하는 리포지토리
     * @param paymentRepository 데이터를 조회하고 저장하는 리포지토리
     * @param chatMessageRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public TradeService(
        TradeRepository tradeRepository,
        TradeQueryRepository tradeQueryRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        ChatRoomRepository chatRoomRepository,
        NegoOfferRepository negoOfferRepository,
        SettlementRepository settlementRepository,
        PaymentRepository paymentRepository,
        ChatMessageRepository chatMessageRepository,
        ChatRedisPublisher chatRedisPublisher
    ) {
        this.tradeRepository = tradeRepository;
        this.tradeQueryRepository = tradeQueryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.negoOfferRepository = negoOfferRepository;
        this.settlementRepository = settlementRepository;
        this.paymentRepository = paymentRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRedisPublisher = chatRedisPublisher;
    }

    public Page<MyTradeResponse> getMyTrades(Long userId, MyTradeRole role, Pageable pageable) {
        return tradeQueryRepository.findMyTrades(userId, role, pageable);
    }

    /**
     * 'getTradeDetail' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param userId 회원 ID
     * @param tradeId 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public TradeDetailResponse getTradeDetail(Long userId, Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (!trade.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "거래 참여자만 조회할 수 있습니다.");
        }

        Payment payment = paymentRepository.findByTrade(trade).orElse(null);
        Settlement settlement = payment != null
            ? settlementRepository.findByPaymentTradeId(tradeId).orElse(null)
            : null;

        return TradeDetailResponse.of(trade, payment, settlement);
    }

    /**
     * 'startTrade' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param buyerId 구매자 ID
     * @param productId 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public TradeResponse startTrade(Long buyerId, Long productId) {
        Product product = productRepository.findByIdForUpdateAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (product.isSeller(buyerId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "판매자는 구매자가 될 수 없습니다.");
        }

        User buyer = userRepository.findByIdAndStatusAndDeletedAtIsNull(buyerId, UserStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매자를 찾을 수 없습니다."));

        if (tradeRepository.existsByProductAndStatusIn(product, TradeStatus.blockingStatuses())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 진행 중이거나 완료된 거래입니다.");
        }

        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByProductAndSellerAndBuyer(product, product.getSeller(), buyer);
        if (chatRoomOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "판매자가 승인한 네고가 있어야 거래를 시작할 수 있습니다.");
        }
        NegoOffer acceptedOffer = negoOfferRepository
            .findFirstByChatRoomIdAndStatusOrderByCreatedAtDesc(chatRoomOpt.get().getId(), NegoOfferStatus.ACCEPTED)
            .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "판매자가 승인한 네고가 있어야 거래를 시작할 수 있습니다."));

        Trade trade = createTradeFromLockedProduct(product, acceptedOffer);
        return TradeResponse.from(trade);
    }

    /**
     * 수락된 네고 제안을 기준으로 결제 대기 거래를 생성한다.
     * @param product 상품 엔티티
     * @param acceptedOffer 판매자가 수락한 네고 제안
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public Trade createTradeFromAcceptedOffer(Product product, NegoOffer acceptedOffer) {
        Product lockedProduct = productRepository.findByIdForUpdateAndDeletedAtIsNull(product.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        return createTradeFromLockedProduct(lockedProduct, acceptedOffer);
    }

    private Trade createTradeFromLockedProduct(Product lockedProduct, NegoOffer acceptedOffer) {
        if (lockedProduct.getStatus() != ProductStatus.SELLING) {
            throw new BusinessException(ErrorCode.CONFLICT, "거래 가능한 상태의 상품이 아닙니다.");
        }
        if (tradeRepository.existsByProductAndStatusIn(lockedProduct, TradeStatus.blockingStatuses())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 진행 중이거나 완료된 거래입니다.");
        }

        Trade trade = Trade.start(lockedProduct, lockedProduct.getSeller(), acceptedOffer.getRequester(), acceptedOffer.getOfferPrice());
        lockedProduct.markReserved();
        try {
            return tradeRepository.save(trade);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 진행 중이거나 완료된 거래입니다.");
        }
    }

    /**
     * 구매 확정 요청을 검증하고 거래를 완료 상태로 변경한다.
     * @param buyerId 구매자 ID
     * @param tradeId 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public TradeResponse completeTrade(Long buyerId, Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (!trade.getBuyer().getId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "구매자만 구매 확정을 할 수 있습니다.");
        }
        if (trade.getStatus() != TradeStatus.PAID) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "결제가 완료된 거래만 구매 확정할 수 있습니다.");
        }

        trade.complete();

        Settlement settlement = settlementRepository.findByPaymentTradeId(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "결제가 완료되지 않은 거래는 구매 확정할 수 없습니다."));
        settlement.complete();
        sendRatingRequestMessage(trade);

        return TradeResponse.from(trade);
    }

    /**
     * 'expireReservation' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param authUser 인증 사용자 정보
     * @param tradeId 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public TradeResponse expireReservation(AuthUser authUser, Long tradeId) {
        if (!isReservationExpiryAuthority(authUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "예약 만료 처리는 관리자만 수행할 수 있습니다.");
        }
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (trade.getStatus() != TradeStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "결제 대기 중인 거래만 예약 만료 처리할 수 있습니다.");
        }

        trade.expire();
        return TradeResponse.from(trade);
    }

    private boolean isReservationExpiryAuthority(AuthUser authUser) {
        return authUser != null
            && ("ROOT_ADMIN".equals(authUser.role()) || "SETTLEMENT_ADMIN".equals(authUser.role()));
    }

    /**
     * 'sendRatingRequestMessage' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param authUser 인증 사용자 정보
     * @param tradeId 거래 ID
     */
    @Transactional
    public void sendRatingRequestMessage(AuthUser authUser, Long tradeId) {
        if (!isSystemAuthority(authUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "평가 요청 메시지 발송은 시스템만 수행할 수 있습니다.");
        }
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (trade.getStatus() != TradeStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "거래가 완료된 경우에만 평가 요청 메시지를 보낼 수 있습니다.");
        }

        sendRatingRequestMessage(trade);
    }

    private void sendRatingRequestMessage(Trade trade) {
        ChatRoom chatRoom = chatRoomRepository
            .findByProductAndSellerAndBuyer(trade.getProduct(), trade.getSeller(), trade.getBuyer())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래에 연결된 채팅방을 찾을 수 없습니다."));

        ChatMessage message = chatMessageRepository.save(
            ChatMessage.send(chatRoom, trade.getSeller(), "거래가 완료되었습니다. 상대방에 대한 후기를 남겨주세요!")
        );
        chatRedisPublisher.publish(chatRoom.getId(), ChatMessageResponse.from(message));
    }

    private boolean isSystemAuthority(AuthUser authUser) {
        return authUser != null && "ROOT_ADMIN".equals(authUser.role());
    }
}
