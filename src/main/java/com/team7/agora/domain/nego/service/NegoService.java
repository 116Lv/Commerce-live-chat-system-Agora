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
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.domain.user.entity.User;
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
    private final ChatSystemMessageService chatSystemMessageService;

    public NegoService(
        NegoOfferRepository negoOfferRepository,
        ChatRoomRepository chatRoomRepository,
        TradeService tradeService,
        ChatSystemMessageService chatSystemMessageService
    ) {
        this.negoOfferRepository = negoOfferRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.tradeService = tradeService;
        this.chatSystemMessageService = chatSystemMessageService;
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
        if (isAlreadyReservedOrSold(chatRoom.getProduct())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 거래가 진행 중이거나 완료된 상품입니다.");
        }

        NegoOffer offer = NegoOffer.create(chatRoom, chatRoom.getBuyer(), offerPrice);
        NegoOfferResponse response = NegoOfferResponse.from(negoOfferRepository.save(offer));
        chatSystemMessageService.send(chatRoom, chatRoom.getBuyer(), "제안이 생성되었습니다.");
        return response;
    }

    @Transactional
    public NegoOfferResponse acceptOffer(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        validateNotExpired(offer);
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
        List<NegoOffer> otherOffers = negoOfferRepository.findAllByChatRoomProductIdAndStatusIn(
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

    @Transactional
    public NegoOfferResponse rejectOffer(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        validateNotExpired(offer);
        validateRespondable(offer);
        offer.reject();
        chatSystemMessageService.send(offer.getChatRoom(), offer.getChatRoom().getSeller(), "제안이 거절되었습니다.");
        return NegoOfferResponse.from(offer);
    }

    @Transactional
    public NegoOfferResponse requestExtension(Long buyerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateBuyer(offer, buyerId);
        validateNotExpired(offer);
        validateRespondable(offer);
        offer.requestExtension();
        chatSystemMessageService.send(
            offer.getChatRoom(), offer.getRequester(), "구매자가 제안 연장을 요청했습니다."
        );
        return NegoOfferResponse.from(offer);
    }

    @Transactional
    public NegoOfferResponse approveExtension(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        validateNotExpired(offer);
        validateExtensionRequested(offer);
        offer.approveExtension();
        chatSystemMessageService.send(
            offer.getChatRoom(), offer.getChatRoom().getSeller(), "판매자가 제안 연장을 승인했습니다."
        );
        return NegoOfferResponse.from(offer);
    }

    @Transactional
    public NegoOfferResponse rejectExtension(Long sellerId, Long offerId) {
        NegoOffer offer = findOffer(offerId);
        validateSeller(offer, sellerId);
        validateNotExpired(offer);
        validateExtensionRequested(offer);
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
        chatSystemMessageService.send(offer.getChatRoom(), offer.getChatRoom().getSeller(), "제안이 만료되었습니다.");
        return NegoOfferResponse.from(offer);
    }

    private boolean isAlreadyReservedOrSold(Product product) {
        return product.getStatus() == ProductStatus.RESERVED || product.getStatus() == ProductStatus.SOLD;
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
            chatSystemMessageService.send(offer.getChatRoom(), offer.getChatRoom().getSeller(), "제안이 만료되었습니다.");
            throw new BusinessException(ErrorCode.CONFLICT, "만료된 가격 제안입니다.");
        }
    }

    private void validateRespondable(NegoOffer offer) {
        if (!NegoOffer.ACTIVE_STATUSES.contains(offer.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "응답 가능한 가격 제안 상태가 아닙니다.");
        }
    }

    private void validateExtensionRequested(NegoOffer offer) {
        if (offer.getStatus() != NegoOfferStatus.EXTENSION_REQUESTED) {
            throw new BusinessException(ErrorCode.CONFLICT, "연장 요청 상태의 가격 제안만 응답할 수 있습니다.");
        }
    }
}
