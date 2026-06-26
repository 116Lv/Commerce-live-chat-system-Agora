package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaCouponDataRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Coupon> findFirstByCouponEventIdAndUserIsNullAndStatusOrderByIdAsc(Long eventId, CouponStatus status);

    List<Coupon> findAllByUserIdAndStatusIn(Long userId, Collection<CouponStatus> statuses);

    List<Coupon> findAllByCouponEventIdAndUserIsNotNull(Long eventId);

    boolean existsByCouponEventIdAndUserId(Long eventId, Long userId);

    void deleteByCouponEventIdAndUserIsNull(Long eventId);

    @Modifying
    @Query("""
        delete from Coupon c
        where c.status = com.team7.agora.domain.coupon.enums.CouponStatus.AVAILABLE
          and c.user is null
          and c.couponEvent.id in (
              select ce.id
              from CouponEvent ce
              where ce.status = com.team7.agora.domain.coupon.enums.CouponEventStatus.ENDED
                and ce.endAt < :now
          )
        """)
    int deleteAvailableSlotsForEndedEventsBefore(@Param("now") LocalDateTime now);

    @Modifying
    @Query("""
        update Coupon c
        set c.status = com.team7.agora.domain.coupon.enums.CouponStatus.EXPIRED
        where c.status = com.team7.agora.domain.coupon.enums.CouponStatus.ISSUED
          and c.expiresAt < :now
        """)
    int expireIssuedCouponsBefore(@Param("now") LocalDateTime now);
}
