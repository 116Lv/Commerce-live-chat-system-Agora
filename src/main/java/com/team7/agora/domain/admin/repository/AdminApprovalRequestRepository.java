package com.team7.agora.domain.admin.repository;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminApprovalStatus;
import com.team7.agora.domain.admin.enums.AdminRole;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminApprovalRequestRepository extends JpaRepository<AdminApprovalRequest, Long> {

    @EntityGraph(attributePaths = {"requester", "targetAdmin", "approver"})
    List<AdminApprovalRequest> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"requester", "targetAdmin", "approver"})
    List<AdminApprovalRequest> findAllByStatusOrderByCreatedAtDesc(AdminApprovalStatus status);

    @EntityGraph(attributePaths = {"requester", "targetAdmin", "approver"})
    List<AdminApprovalRequest> findAllByRequesterOrderByCreatedAtDesc(Admin requester);

    boolean existsByRequesterAndTargetAdminAndRequestedRoleAndStatus(
            Admin requester,
            Admin targetAdmin,
            AdminRole requestedRole,
            AdminApprovalStatus status
    );

    boolean existsByPendingRequestKey(String pendingRequestKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AdminApprovalRequest r join fetch r.requester left join fetch r.targetAdmin left join fetch r.approver where r.id = :id")
    Optional<AdminApprovalRequest> findByIdForUpdate(@Param("id") Long id);
}
