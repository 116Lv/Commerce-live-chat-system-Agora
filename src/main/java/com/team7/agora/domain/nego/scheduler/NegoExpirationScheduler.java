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
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param negoExpirationService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public NegoExpirationScheduler(NegoExpirationService negoExpirationService) {
        this.negoExpirationService = negoExpirationService;
    }

    /**
     * 'expireDueOffersAndReservations' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
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
