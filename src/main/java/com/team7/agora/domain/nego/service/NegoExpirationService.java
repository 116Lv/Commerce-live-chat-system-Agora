package com.team7.agora.domain.nego.service;

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

    public NegoExpirationService(NegoOfferRepository negoOfferRepository, TradeRepository tradeRepository) {
        this.negoOfferRepository = negoOfferRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public int expireDueOffers(LocalDateTime now) {
        List<NegoOffer> dueOffers = negoOfferRepository.findAllByStatusInAndExpiresAtLessThanEqual(
            NegoOffer.ACTIVE_STATUSES,
            now
        );
        dueOffers.forEach(offer -> offer.expire(now));
        return dueOffers.size();
    }

    @Transactional
    public int expireDuePaymentReservations(LocalDateTime now) {
        List<Trade> dueTrades = tradeRepository.findAllByStatusAndPaymentDueAtLessThanEqual(
            TradeStatus.PAYMENT_PENDING,
            now
        );
        dueTrades.forEach(Trade::expire);
        return dueTrades.size();
    }
}
