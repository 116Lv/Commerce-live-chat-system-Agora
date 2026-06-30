package com.team7.agora.domain.nego.repository;

import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Nego Offer 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface NegoOfferRepository extends JpaRepository<NegoOffer, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NegoOffer n where n.id = :id")
    Optional<NegoOffer> findByIdForUpdate(@Param("id") Long id);

    Optional<NegoOffer> findFirstByChatRoomIdAndStatusOrderByCreatedAtDesc(Long chatRoomId, NegoOfferStatus status);

    Optional<NegoOffer> findFirstByChatRoomIdAndStatusInOrderByCreatedAtDesc(
        Long chatRoomId,
        Collection<NegoOfferStatus> statuses
    );

    boolean existsByChatRoomAndRequesterAndStatusIn(
        ChatRoom chatRoom,
        User requester,
        Collection<NegoOfferStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NegoOffer n where n.chatRoom.product.id = :productId and n.status in :statuses")
    List<NegoOffer> findAllByChatRoomProductIdAndStatusInForUpdate(
        @Param("productId") Long productId,
        @Param("statuses") Collection<NegoOfferStatus> statuses
    );

    List<NegoOffer> findAllByStatusInAndExpiresAtLessThanEqual(Collection<NegoOfferStatus> statuses, LocalDateTime now);
}
