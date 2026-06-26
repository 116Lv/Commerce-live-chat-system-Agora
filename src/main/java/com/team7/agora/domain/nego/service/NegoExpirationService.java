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

/**
 * Application service that coordinates nego expiration use cases.
 */
@Service
@Transactional(readOnly = true)
public class NegoExpirationService {

    private final NegoOfferRepository negoOfferRepository;
    private final TradeRepository tradeRepository;

    /**
     * Creates a nego expiration service instance.
     * @param negoOfferRepository the nego offer repository value
     * @param tradeRepository the trade repository value
     */
    public NegoExpirationService(NegoOfferRepository negoOfferRepository, TradeRepository tradeRepository) {
        this.negoOfferRepository = negoOfferRepository;
        this.tradeRepository = tradeRepository;
    }

    /**
     * Handles expire due offers behavior.
     * @param now the now value
     * @return the expire due offers result
     */
    @Transactional
    public int expireDueOffers(LocalDateTime now) {
        List<NegoOffer> dueOffers = negoOfferRepository.findAllByStatusInAndExpiresAtLessThanEqual(
            NegoOffer.ACTIVE_STATUSES,
            now
        );
        dueOffers.forEach(offer -> offer.expire(now));
        return dueOffers.size();
    }

    /**
     * Handles expire due payment reservations behavior.
     * @param now the now value
     * @return the expire due payment reservations result
     */
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
