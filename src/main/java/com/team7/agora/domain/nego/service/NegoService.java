package com.team7.agora.domain.nego.service;

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
import com.team7.agora.domain.product.enums.ProductStatus;
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
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 네고 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class NegoService {

    public static final List<NegoOfferStatus> CURRENT_OFFER_STATUSES = List.of(
        NegoOfferStatus.PENDING,
        NegoOfferStatus.EXTENSION_REQUESTED,
        NegoOfferStatus.EXTENDED,
        NegoOfferStatus.ACCEPTED
    );

    private final NegoOfferRepository negoOfferRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;
    private final TradeService tradeService;
    private final TradeRepository tradeRepository;
    private final ChatSystemMessageService chatSystemMessageService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param negoOfferRepository 데이터를 조회하고 저장하는 리포지토리
     * @param chatRoomRepository 데이터를 조회하고 저장하는 리포지토리
     * @param tradeService 거래 비즈니스 로직을 처리하는 서비스
     * @param chatSystemMessageService 채팅방에 네고 진행 상황을 시스템 메시지로 남기는 서비스
     */
    public NegoService(
        NegoOfferRepository negoOfferRepository,
        ChatRoomRepository chatRoomRepository,
        ProductRepository productRepository,
        TradeService tradeService,
        TradeRepository tradeRepository,
        ChatSystemMessageService chatSystemMessageService
    ) {
        this.negoOfferRepository = negoOfferRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.productRepository = productRepository;
        this.tradeService = tradeService;
        this.tradeRepository = tradeRepository;
        this.chatSystemMessageService = chatSystemMessageService;
    }

    /**
     * 구매자가 채팅방에서 제안한 가격으로 네고 제안을 생성한다.
     * @param requesterId 가격 제안을 생성한 구매자 ID
     * @param chatRoomId 채팅방 ID
     * @param offerPrice 제안 가격
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public NegoOfferResponse createOffer(Long requesterId, Long chatRoomId, BigDecimal offerPrice) {
        ChatRoom chatRoom = findActiveRoom(chatRoomId);
        Product lockedProduct = findProductForUpdate(chatRoom.getProduct().getId());

        if (!chatRoom.isParticipant(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 가격 제안을 할 수 있습니다.");
        }
        if (chatRoom.getSeller().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "판매자는 가격 제안을 만들 수 없습니다.");
        }
        if (isAlreadyReservedOrSold(lockedProduct)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 거래가 진행 중이거나 완료된 상품입니다.");
        }
        if (lockedProduct.getApprovalStatus() != ProductApprovalStatus.APPROVED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only approved products can receive offers.");
        }
        validateOfferPrice(offerPrice, lockedProduct);

        if (negoOfferRepository.existsByChatRoomAndRequesterAndStatusIn(
            chatRoom,
            chatRoom.getBuyer(),
            NegoOffer.ACTIVE_STATUSES
        )) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 진행 중인 가격 제안이 있습니다.");
        }

        NegoOffer offer = NegoOffer.create(chatRoom, chatRoom.getBuyer(), offerPrice);
        NegoOfferResponse response = NegoOfferResponse.from(negoOfferRepository.save(offer));
        chatSystemMessageService.send(chatRoom, chatRoom.getBuyer(), "제안이 생성되었습니다.");
        return response;
    }

    /**
     * 채팅방에서 진행 중인 최신 가격 제안을 조회한다.
     * @param requesterId 조회하는 사용자 ID
     * @param chatRoomId 채팅방 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public NegoOfferResponse getCurrentOffer(Long requesterId, Long chatRoomId) {
        ChatRoom chatRoom = findReadableRoom(chatRoomId);

        if (!chatRoom.isParticipant(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 가격 제안을 조회할 수 있습니다.");
        }

        return negoOfferRepository.findFirstByChatRoomIdAndStatusInOrderByCreatedAtDesc(
                chatRoomId,
                CURRENT_OFFER_STATUSES
            )
            .map(this::toCurrentOfferResponse)
            .orElse(null);
    }

    private NegoOfferResponse toCurrentOfferResponse(NegoOffer offer) {
        if (offer.getStatus() != NegoOfferStatus.ACCEPTED) {
            return NegoOfferResponse.from(offer);
        }

        return tradeRepository.findByProductAndBuyer(offer.getChatRoom().getProduct(), offer.getRequester())
            .map(trade -> NegoOfferResponse.from(offer, trade))
            .orElseGet(() -> NegoOfferResponse.from(offer));
    }

    /**
     * 판매자가 가격 제안을 수락하고 해당 상품의 거래를 생성한다.
     * @param sellerId 상품 판매자 ID
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public NegoOfferResponse acceptOffer(Long sellerId, Long offerId) {
        NegoOffer offer = findOfferForUpdate(offerId);
        validateSeller(offer, sellerId);
        expireIfNeededAndThrow(offer);
        validateRespondable(offer);

        ChatRoom chatRoom = offer.getChatRoom();
        Product product = chatRoom.getProduct();
        Trade trade = tradeService.createTradeFromAcceptedOffer(product, offer);
        offer.accept();
        cancelOtherActiveOffers(product, offer, chatRoom.getSeller());

        chatSystemMessageService.send(chatRoom, chatRoom.getSeller(), "판매자가 제안을 최종 승인했습니다.");
        return NegoOfferResponse.from(offer, trade);
    }

    private void cancelOtherActiveOffers(Product product, NegoOffer acceptedOffer, User seller) {
        List<NegoOffer> otherOffers = negoOfferRepository.findAllByChatRoomProductIdAndStatusInForUpdate(
            product.getId(),
            NegoOffer.ACTIVE_STATUSES
        );
        for (NegoOffer other : otherOffers) {
            if (!other.getId().equals(acceptedOffer.getId())) {
                other.cancel();
                chatSystemMessageService.send(other.getChatRoom(), seller, "판매자가 다른 구매자를 선택했습니다.");
            }
        }
    }

    /**
     * 판매자가 구매자의 가격 제안을 거절 상태로 변경한다.
     * @param sellerId 상품 판매자 ID
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public NegoOfferResponse rejectOffer(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        expireIfNeededAndThrow(offer);
        validateRespondable(offer);
        offer.reject();
        chatSystemMessageService.send(offer.getChatRoom(), offer.getChatRoom().getSeller(), "제안이 거절되었습니다.");
        return NegoOfferResponse.from(offer);
    }

    /**
     * 구매자가 가격 제안의 응답 기한 연장을 요청한다.
     * @param buyerId 가격 제안 연장을 요청하는 구매자 ID
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public NegoOfferResponse requestExtension(Long buyerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateBuyer(offer, buyerId);
        expireIfNeededAndThrow(offer);
        validateExtensionRequestable(offer);
        offer.requestExtension();
        chatSystemMessageService.send(
            offer.getChatRoom(), offer.getRequester(), "구매자가 제안 연장을 요청했습니다."
        );
        return NegoOfferResponse.from(offer);
    }

    /**
     * 판매자가 가격 제안 연장 요청을 승인하고 만료 시간을 연장한다.
     * @param sellerId 가격 제안 연장 요청을 승인하는 판매자 ID
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public NegoOfferResponse approveExtension(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        expireIfNeededAndThrow(offer);
        validateExtensionRequested(offer);
        offer.approveExtension();
        chatSystemMessageService.send(
            offer.getChatRoom(), offer.getChatRoom().getSeller(), "판매자가 제안 연장을 승인했습니다."
        );
        return NegoOfferResponse.from(offer);
    }

    /**
     * 판매자가 가격 제안 연장 요청을 거절한다.
     * @param sellerId 가격 제안 연장 요청을 거절하는 판매자 ID
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public NegoOfferResponse rejectExtension(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        expireIfNeededAndThrow(offer);
        validateExtensionRequested(offer);
        offer.rejectExtension();
        return NegoOfferResponse.from(offer);
    }

    /**
     * 관리자가 특정 가격 제안을 수동으로 만료 처리한다.
     * @param authUser 인증 사용자 정보
     * @param offerId 네고 제안 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public NegoOfferResponse expireOffer(AuthUser authUser, Long offerId) {
        if (!isExpiryAuthority(authUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "가격 제안 만료 처리는 관리자만 수행할 수 있습니다.");
        }
        NegoOffer offer = findOffer(offerId);
        offer.expire(AgoraClock.now());
        chatSystemMessageService.send(offer.getChatRoom(), offer.getChatRoom().getSeller(), "제안이 만료되었습니다.");
        return NegoOfferResponse.from(offer);
    }

    private boolean isAlreadyReservedOrSold(Product product) {
        return product.getStatus() == ProductStatus.RESERVED || product.getStatus() == ProductStatus.SOLD;
    }

    private void validateOfferPrice(BigDecimal offerPrice, Product product) {
        if (offerPrice == null
            || offerPrice.compareTo(BigDecimal.ZERO) <= 0
            || offerPrice.compareTo(product.getPrice()) >= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "가격 제안은 0보다 크고 상품 정가보다 작아야 합니다.");
        }
    }

    private boolean isExpiryAuthority(AuthUser authUser) {
        return authUser != null && "ROOT_ADMIN".equals(authUser.role());
    }

    private ChatRoom findActiveRoom(Long chatRoomId) {
        return chatRoomRepository.findByIdAndStatusForUpdate(chatRoomId, ChatRoomStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }

    private ChatRoom findReadableRoom(Long chatRoomId) {
        return chatRoomRepository.findByIdAndStatus(chatRoomId, ChatRoomStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }

    private Product findProductForUpdate(Long productId) {
        return productRepository.findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(productId, ProductApprovalStatus.APPROVED)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private NegoOffer findOffer(Long offerId) {
        return negoOfferRepository.findById(offerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 제안을 찾을 수 없습니다."));
    }

    private void validateSeller(NegoOffer offer, Long sellerId) {
        if (!offer.getChatRoom().getSeller().getId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "판매자만 가격 제안에 응답할 수 있습니다.");
        }
    }

    private void validateBuyer(NegoOffer offer, Long buyerId) {
        if (!offer.getRequester().getId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "가격 제안 구매자만 연장 요청에 응답할 수 있습니다.");
        }
    }

    private void expireIfNeededAndThrow(NegoOffer offer) {
        if (offer.isExpired(AgoraClock.now())) {
            offer.expire(AgoraClock.now());
            chatSystemMessageService.send(offer.getChatRoom(), offer.getChatRoom().getSeller(), "제안이 만료되었습니다.");
            throw new BusinessException(ErrorCode.CONFLICT, "만료된 가격 제안입니다.");
        }
    }

    private void validateRespondable(NegoOffer offer) {
        if (!NegoOffer.ACTIVE_STATUSES.contains(offer.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "응답 가능한 가격 제안 상태가 아닙니다.");
        }
    }

    private void validateExtensionRequestable(NegoOffer offer) {
        if (offer.getStatus() != NegoOfferStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "연장 요청 가능한 가격 제안 상태가 아닙니다.");
        }
        if (offer.isExtensionRequested()) {
            throw new BusinessException(ErrorCode.CONFLICT, "연장 요청은 한 번만 할 수 있습니다.");
        }
    }

    private void validateExtensionRequested(NegoOffer offer) {
        if (offer.getStatus() != NegoOfferStatus.EXTENSION_REQUESTED) {
            throw new BusinessException(ErrorCode.CONFLICT, "연장 요청 상태의 가격 제안만 응답할 수 있습니다.");
        }
    }

    private NegoOffer findOfferForUpdate(Long offerId) {
        return negoOfferRepository.findByIdForUpdate(offerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 제안을 찾을 수 없습니다."));
    }
}
