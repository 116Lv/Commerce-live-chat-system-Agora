package com.team7.agora.domain.nego.repository;

import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface NegoOfferRepository extends JpaRepository<NegoOffer, Long> {
    Optional<NegoOffer> findFirstByChatRoomIdAndStatusOrderByCreatedAtDesc(Long chatRoomId, NegoOfferStatus status);
    List<NegoOffer> findAllByChatRoomProductIdAndStatusIn(Long productId, Collection<NegoOfferStatus> statuses);
    List<NegoOffer> findAllByStatusInAndExpiresAtLessThanEqual(Collection<NegoOfferStatus> statuses, LocalDateTime now);
}
