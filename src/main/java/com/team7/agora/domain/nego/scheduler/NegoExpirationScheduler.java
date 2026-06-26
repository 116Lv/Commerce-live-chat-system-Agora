package com.team7.agora.domain.nego.scheduler;

import com.team7.agora.domain.nego.service.NegoExpirationService;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that manages nego expiration processing.
 */
@Component
public class NegoExpirationScheduler {

    private final NegoExpirationService negoExpirationService;

    /**
     * Creates a nego expiration scheduler instance.
     * @param negoExpirationService the nego expiration service value
     */
    public NegoExpirationScheduler(NegoExpirationService negoExpirationService) {
        this.negoExpirationService = negoExpirationService;
    }

    /**
     * Handles expire due offers and reservations behavior.
     */
    @Scheduled(
        fixedDelayString = "${agora.scheduler.nego-expiration.fixed-delay:300000}",
        initialDelayString = "${agora.scheduler.nego-expiration.initial-delay:300000}"
    )
    public void expireDueOffersAndReservations() {
        LocalDateTime now = LocalDateTime.now();
        negoExpirationService.expireDueOffers(now);
        negoExpirationService.expireDuePaymentReservations(now);
    }
}
