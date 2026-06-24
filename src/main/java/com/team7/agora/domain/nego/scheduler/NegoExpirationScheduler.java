package com.team7.agora.domain.nego.scheduler;

import com.team7.agora.domain.nego.service.NegoExpirationService;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NegoExpirationScheduler {

    private final NegoExpirationService negoExpirationService;

    public NegoExpirationScheduler(NegoExpirationService negoExpirationService) {
        this.negoExpirationService = negoExpirationService;
    }

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
