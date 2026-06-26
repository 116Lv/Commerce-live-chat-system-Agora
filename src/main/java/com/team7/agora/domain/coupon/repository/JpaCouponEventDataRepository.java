package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaCouponEventDataRepository extends JpaRepository<CouponEvent, Long> {

    @Query("""
        select ce
        from CouponEvent ce
        where ce.status = com.team7.agora.domain.coupon.enums.CouponEventStatus.ACTIVE
          and ce.type <> com.team7.agora.domain.coupon.enums.CouponEventType.ADMIN_INDIVIDUAL
          and ce.startAt <= :now
          and ce.endAt >= :now
          and ce.issuedQuantity < ce.totalQuantity
        order by ce.startAt asc, ce.id asc
        """)
    List<CouponEvent> findPublicIssueableEvents(@Param("now") LocalDateTime now);
}
