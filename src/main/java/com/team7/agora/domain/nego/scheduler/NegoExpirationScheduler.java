package com.team7.agora.domain.nego.scheduler;

import com.team7.agora.domain.nego.service.NegoExpirationService;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 네고 만료 처리를 담당하는 스케줄러이다.
 */
@Component
public class NegoExpirationScheduler {

    private final NegoExpirationService negoExpirationService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param negoExpirationService 입력 값
     */
    public NegoExpirationScheduler(NegoExpirationService negoExpirationService) {
        this.negoExpirationService = negoExpirationService;
    }

    /**
     * 요청한 동작을 처리한다.
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
