package com.team7.agora.domain.admin.entity;

import com.team7.agora.domain.admin.enums.AdminApprovalOperation;
import com.team7.agora.domain.admin.enums.AdminApprovalStatus;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.time.AgoraClock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admin_approval_requests")
public class AdminApprovalRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdminApprovalOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminApprovalStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Admin requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_admin_id")
    private Admin targetAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", length = 30)
    private AdminRole requestedRole;

    private Long targetProductId;

    @Column(length = 100)
    private String targetProductTitle;

    @Column(nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Admin approver;

    @Column(length = 500)
    private String decisionMemo;

    private LocalDateTime decidedAt;

    @Column(length = 120, unique = true)
    private String pendingRequestKey;

    private AdminApprovalRequest(Admin requester, Admin targetAdmin, AdminRole requestedRole, String reason) {
        markCreatedNow();
        this.operation = AdminApprovalOperation.ADMIN_ROLE_CHANGE;
        this.status = AdminApprovalStatus.PENDING;
        this.requester = requester;
        this.targetAdmin = targetAdmin;
        this.requestedRole = requestedRole;
        this.reason = normalizeText(reason);
        this.pendingRequestKey = buildPendingRequestKey(requester, targetAdmin, requestedRole);
    }

    private AdminApprovalRequest(AdminApprovalOperation operation, Admin requester, String reason, String pendingRequestKey) {
        markCreatedNow();
        this.operation = operation;
        this.status = AdminApprovalStatus.PENDING;
        this.requester = requester;
        this.reason = normalizeText(reason);
        this.pendingRequestKey = pendingRequestKey;
    }

    private AdminApprovalRequest(Admin requester, Long targetProductId, String targetProductTitle, String reason) {
        markCreatedNow();
        this.operation = AdminApprovalOperation.PRODUCT_HIDE;
        this.status = AdminApprovalStatus.PENDING;
        this.requester = requester;
        this.targetProductId = targetProductId;
        this.targetProductTitle = normalizeOptionalText(targetProductTitle);
        this.reason = normalizeText(reason);
        this.pendingRequestKey = buildProductHidePendingRequestKey(targetProductId);
    }

    public static AdminApprovalRequest createRoleChange(
            Admin requester,
            Admin targetAdmin,
            AdminRole requestedRole,
            String reason
    ) {
        if (requester == null || targetAdmin == null || requestedRole == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        return new AdminApprovalRequest(requester, targetAdmin, requestedRole, reason);
    }

    public static AdminApprovalRequest createCouponOperation(
            AdminApprovalOperation operation,
            Admin requester,
            String reason,
            String pendingRequestKey
    ) {
        if (operation == null || requester == null || operation == AdminApprovalOperation.ADMIN_ROLE_CHANGE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        return new AdminApprovalRequest(operation, requester, reason, pendingRequestKey);
    }

    public static AdminApprovalRequest createProductHide(Admin requester, Long targetProductId, String targetProductTitle, String reason) {
        if (requester == null || targetProductId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        return new AdminApprovalRequest(requester, targetProductId, targetProductTitle, reason);
    }

    public void approve(Admin approver, String memo) {
        validatePending();
        this.status = AdminApprovalStatus.APPROVED;
        this.approver = approver;
        this.decisionMemo = normalizeOptionalText(memo);
        this.decidedAt = AgoraClock.now();
        this.pendingRequestKey = null;
    }

    public void assertPending() {
        validatePending();
    }

    public void applyRoleChange() {
        if (operation != AdminApprovalOperation.ADMIN_ROLE_CHANGE || targetAdmin == null || requestedRole == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        this.targetAdmin.changeRole(requestedRole);
    }

    public void reject(Admin approver, String memo) {
        validatePending();
        this.status = AdminApprovalStatus.REJECTED;
        this.approver = approver;
        this.decisionMemo = normalizeOptionalText(memo);
        this.decidedAt = AgoraClock.now();
        this.pendingRequestKey = null;
    }

    private void validatePending() {
        if (status != AdminApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "Approval request has already been decided.");
        }
    }

    private String normalizeText(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Approval request reason is required.");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String buildPendingRequestKey(Admin requester, Admin targetAdmin, AdminRole requestedRole) {
        return requester.getId() + ":" + targetAdmin.getId() + ":" + requestedRole.name();
    }

    public static String buildProductHidePendingRequestKey(Long productId) {
        return "PRODUCT_HIDE:" + productId;
    }
}
