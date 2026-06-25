package com.team7.agora.domain.nego.service;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.nego.dto.response.NegoOfferResponse;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NegoService {

    private final NegoOfferRepository negoOfferRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TradeService tradeService;

    public NegoService(
        NegoOfferRepository negoOfferRepository,
        ChatRoomRepository chatRoomRepository,
        TradeService tradeService
    ) {
        this.negoOfferRepository = negoOfferRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.tradeService = tradeService;
    }

    @Transactional
    public NegoOfferResponse createOffer(Long requesterId, Long chatRoomId, BigDecimal offerPrice) {
        ChatRoom chatRoom = findActiveRoom(chatRoomId);

        if (!chatRoom.isParticipant(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "채팅방 참여자만 가격 제안을 할 수 있습니다.");
        }
        if (chatRoom.getSeller().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "판매자는 가격 제안을 만들 수 없습니다.");
        }

        NegoOffer offer = NegoOffer.create(chatRoom, chatRoom.getBuyer(), offerPrice);
        return NegoOfferResponse.from(negoOfferRepository.save(offer));
    }

    @Transactional
    public NegoOfferResponse acceptOffer(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        validateNotExpired(offer);

        Product product = offer.getChatRoom().getProduct();
        tradeService.createTradeFromAcceptedOffer(product, offer);
        offer.accept();
        cancelOtherActiveOffers(product, offer);

        return NegoOfferResponse.from(offer);
    }

    private void cancelOtherActiveOffers(Product product, NegoOffer acceptedOffer) {
        List<NegoOffer> otherOffers = negoOfferRepository.findAllByChatRoomProductIdAndStatusIn(
            product.getId(),
            NegoOffer.ACTIVE_STATUSES
        );
        for (NegoOffer other : otherOffers) {
            if (!other.getId().equals(acceptedOffer.getId())) {
                other.cancel();
            }
        }
    }

    @Transactional
    public NegoOfferResponse rejectOffer(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        validateNotExpired(offer);
        offer.reject();
        return NegoOfferResponse.from(offer);
    }

    @Transactional
    public NegoOfferResponse requestExtension(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        validateNotExpired(offer);
        offer.requestExtension();
        return NegoOfferResponse.from(offer);
    }

    @Transactional
    public NegoOfferResponse approveExtension(Long buyerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateBuyer(offer, buyerId);
        validateNotExpired(offer);
        offer.approveExtension();
        return NegoOfferResponse.from(offer);
    }

    @Transactional
    public NegoOfferResponse rejectExtension(Long buyerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateBuyer(offer, buyerId);
        validateNotExpired(offer);
        offer.rejectExtension();
        return NegoOfferResponse.from(offer);
    }

    @Transactional
    public NegoOfferResponse expireOffer(AuthUser authUser, Long offerId) {
        if (!isExpiryAuthority(authUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "가격 제안 만료 처리는 관리자만 수행할 수 있습니다.");
        }
        NegoOffer offer = findOffer(offerId);
        offer.expire(LocalDateTime.now());
        return NegoOfferResponse.from(offer);
    }

    private boolean isExpiryAuthority(AuthUser authUser) {
        return authUser != null && "ROOT_ADMIN".equals(authUser.role());
    }

    private ChatRoom findActiveRoom(Long chatRoomId) {
        return chatRoomRepository.findByIdAndStatus(chatRoomId, ChatRoomStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
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

    private void validateNotExpired(NegoOffer offer) {
        if (offer.isExpired(LocalDateTime.now())) {
            offer.expire(LocalDateTime.now());
            throw new BusinessException(ErrorCode.CONFLICT, "만료된 가격 제안입니다.");
        }
    }
}
