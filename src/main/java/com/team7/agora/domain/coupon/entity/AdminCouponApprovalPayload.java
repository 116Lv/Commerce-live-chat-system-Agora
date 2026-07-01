package com.team7.agora.domain.coupon.entity;

import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admin_coupon_approval_payloads")
public class AdminCouponApprovalPayload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_request_id", nullable = false, unique = true)
    private AdminApprovalRequest approvalRequest;

    @Column(name = "coupon_event_id", nullable = false)
    private Long couponEventId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CouponEventType eventType;

    @Column(length = 100)
    private String eventName;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer totalQuantity;

    private Integer discountAmount;

    private Integer minOrderAmount;

    private Integer validDays;

    @Column(columnDefinition = "TEXT")
    private String targetUserIds;

    private int inputCount;

    private int validTargetCount;

    private int duplicateCount;

    private int excludedCount;

    private int plannedIssueCount;

    private int expectedIssuedQuantity;

    private boolean exceedsRemainingQuantity;

    private AdminCouponApprovalPayload(AdminApprovalRequest approvalRequest, CouponEvent event) {
        this.approvalRequest = approvalRequest;
        this.couponEventId = event.getId();
        this.eventType = event.getType();
        this.eventName = event.getName();
        this.startAt = event.getStartAt();
        this.endAt = event.getEndAt();
        this.totalQuantity = event.getTotalQuantity();
        this.discountAmount = event.getDiscountAmount();
        this.minOrderAmount = event.getMinOrderAmount();
        this.validDays = event.getValidDays();
    }

    private AdminCouponApprovalPayload(AdminApprovalRequest approvalRequest, Long couponEventId) {
        this.approvalRequest = approvalRequest;
        this.couponEventId = couponEventId;
    }

    public static AdminCouponApprovalPayload forCreate(AdminApprovalRequest approvalRequest, CouponEvent event) {
        return new AdminCouponApprovalPayload(approvalRequest, event);
    }

    public static AdminCouponApprovalPayload forStop(AdminApprovalRequest approvalRequest, Long couponEventId) {
        return new AdminCouponApprovalPayload(approvalRequest, couponEventId);
    }

    public static AdminCouponApprovalPayload forIssue(
            AdminApprovalRequest approvalRequest,
            Long couponEventId,
            List<Long> targetUserIds,
            int inputCount,
            int validTargetCount,
            int duplicateCount,
            int excludedCount,
            int plannedIssueCount,
            int expectedIssuedQuantity,
            boolean exceedsRemainingQuantity
    ) {
        AdminCouponApprovalPayload payload = new AdminCouponApprovalPayload(approvalRequest, couponEventId);
        payload.targetUserIds = joinTargetUserIds(targetUserIds);
        payload.inputCount = inputCount;
        payload.validTargetCount = validTargetCount;
        payload.duplicateCount = duplicateCount;
        payload.excludedCount = excludedCount;
        payload.plannedIssueCount = plannedIssueCount;
        payload.expectedIssuedQuantity = expectedIssuedQuantity;
        payload.exceedsRemainingQuantity = exceedsRemainingQuantity;
        return payload;
    }

    public List<Long> targetUserIdList() {
        if (targetUserIds == null || targetUserIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(targetUserIds.split(","))
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }

    private static String joinTargetUserIds(List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return null;
        }
        return String.join(",", targetUserIds.stream().map(String::valueOf).toList());
    }
}
