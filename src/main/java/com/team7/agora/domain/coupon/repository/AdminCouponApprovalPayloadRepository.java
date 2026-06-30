package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCouponApprovalPayloadRepository extends JpaRepository<AdminCouponApprovalPayload, Long> {

    @EntityGraph(attributePaths = "approvalRequest")
    Optional<AdminCouponApprovalPayload> findByApprovalRequest(AdminApprovalRequest approvalRequest);
}
