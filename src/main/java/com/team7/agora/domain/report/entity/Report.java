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
 * 신고 도메인 정보를 영속화하는 JPA 엔티티이다.
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
     * 'product' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param reporter 신고를 등록한 회원 엔티티
     * @param reportedUser 신고 대상 회원 엔티티
     * @param product 상품 엔티티
     * @param reason 처리 사유
     * @return 클라이언트에 반환할 API 응답
     */
    public static Report product(User reporter, User reportedUser, Product product, String reason) {
        return new Report(reporter, reportedUser, product, reason);
    }

    /**
     * 'user' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param reporter 신고를 등록한 회원 엔티티
     * @param reportedUser 신고 대상 회원 엔티티
     * @param reason 처리 사유
     * @return 클라이언트에 반환할 API 응답
     */
    public static Report user(User reporter, User reportedUser, String reason) {
        return new Report(reporter, reportedUser, null, reason);
    }

    /**
     * 관리자가 신고 내용을 확인하고 처리 상태와 메모를 저장한다.
     * @param adminMemo 관리자가 신고 처리 시 남기는 메모
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
