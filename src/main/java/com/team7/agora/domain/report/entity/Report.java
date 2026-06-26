package com.team7.agora.domain.report.entity;

import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.report.enums.ReportStatus;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
    name = "reports",
    indexes = {
        @Index(name = "idx_reports_status_created", columnList = "status, created_at"),
        @Index(name = "idx_reports_reported_user", columnList = "reported_user_id")
    }
/**
 * JPA 엔티티이다.
 */
)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(length = 1000)
    private String adminMemo;

    private LocalDateTime resolvedAt;

    private Report(User reporter, User reportedUser, Product product, String reason) {
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.product = product;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
        markCreatedNow();
    }

    /**
     * 요청한 동작을 처리한다.
     * @param reporter 입력 값
     * @param reportedUser 입력 값
     * @param product 입력 값
     * @param reason 입력 값
     * @return 처리 결과
     */
    public static Report product(User reporter, User reportedUser, Product product, String reason) {
        return new Report(reporter, reportedUser, product, reason);
    }

    /**
     * 요청한 동작을 처리한다.
     * @param reporter 입력 값
     * @param reportedUser 입력 값
     * @param reason 입력 값
     * @return 처리 결과
     */
    public static Report user(User reporter, User reportedUser, String reason) {
        return new Report(reporter, reportedUser, null, reason);
    }

    /**
     * 요청한 동작을 처리한다.
     * @param adminMemo 입력 값
     */
    public void resolve(String adminMemo) {
        if (status != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 처리된 신고입니다.");
        }
        this.status = ReportStatus.RESOLVED;
        this.adminMemo = adminMemo;
        this.resolvedAt = LocalDateTime.now();
    }
}
