package com.team7.agora.domain.nego.service;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.chat.service.ChatSystemMessageService;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.trade.repository.TradeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NegoExpirationService {

    private final NegoOfferRepository negoOfferRepository;
    private final TradeRepository tradeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatSystemMessageService chatSystemMessageService;

    public NegoExpirationService(
        NegoOfferRepository negoOfferRepository,
        TradeRepository tradeRepository,
        ChatRoomRepository chatRoomRepository,
        ChatSystemMessageService chatSystemMessageService
    ) {
        this.negoOfferRepository = negoOfferRepository;
        this.tradeRepository = tradeRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatSystemMessageService = chatSystemMessageService;
    }

    @Transactional
    public int expireDueOffers(LocalDateTime now) {
        List<NegoOffer> dueOffers = negoOfferRepository.findAllByStatusInAndExpiresAtLessThanEqual(
            NegoOffer.ACTIVE_STATUSES,
            now
        );
        dueOffers.forEach(offer -> {
            offer.expire(now);
            chatSystemMessageService.send(offer.getChatRoom(), offer.getChatRoom().getSeller(), "제안이 만료되었습니다.");
        });
        return dueOffers.size();
    }

    @Transactional
    public int expireDuePaymentReservations(LocalDateTime now) {
        List<Trade> dueTrades = tradeRepository.findAllByStatusAndPaymentDueAtLessThanEqual(
            TradeStatus.PAYMENT_PENDING,
            now
        );
        dueTrades.forEach(trade -> {
            trade.expire();
            chatRoomRepository.findByProductAndSellerAndBuyer(trade.getProduct(), trade.getSeller(), trade.getBuyer())
                .ifPresent((ChatRoom chatRoom) -> chatSystemMessageService.send(
                    chatRoom, chatRoom.getSeller(), "결제 기한이 만료되어 예약이 취소되었습니다."
                ));
        });
        return dueTrades.size();
    }
}
