package com.team7.agora.domain.trade.entity;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.user.entity.User;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "trades",
    indexes = {
        @Index(name = "idx_trades_product", columnList = "product_id"),
        @Index(name = "idx_trades_buyer", columnList = "buyer_id"),
        @Index(name = "idx_trades_seller", columnList = "seller_id")
    }
/**
 * 거래 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradeStatus status;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime paymentDueAt;

    private Trade(Product product, User seller, User buyer, BigDecimal price) {
        this.product = product;
        this.seller = seller;
        this.buyer = buyer;
        this.price = price;
        this.status = TradeStatus.PAYMENT_PENDING;
        this.paymentDueAt = AgoraClock.now().plusHours(24);
    }

    /**
     * 'start' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param product 상품 엔티티
     * @param seller 판매자 엔티티
     * @param buyer 구매자 엔티티
     * @param price 가격
     * @return 클라이언트에 반환할 API 응답
     */
    public static Trade start(Product product, User seller, User buyer, BigDecimal price) {
        return new Trade(product, seller, buyer, price);
    }

    /**
     * 결제 승인 정보를 반영해 결제를 완료 상태로 변경한다.
     */
    public void markPaid() {
        if (this.status != TradeStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "결제 대기 상태인 거래만 결제 완료 처리할 수 있습니다.");
        }
        this.status = TradeStatus.PAID;
        this.product.markSold();
        this.paymentDueAt = null;
    }

    /**
     * 도메인 객체를 완료 상태로 변경한다.
     */
    public void complete() {
        if (this.status != TradeStatus.PAID) {
            throw new BusinessException(ErrorCode.CONFLICT, "결제가 완료된 거래만 완료할 수 있습니다.");
        }
        this.status = TradeStatus.COMPLETED;
        this.completedAt = AgoraClock.now();
    }

    /**
     * 도메인 객체를 취소 상태로 변경한다.
     */
    public void cancel() {
        if (this.status != TradeStatus.PAYMENT_PENDING && this.status != TradeStatus.PAID) {
            throw new BusinessException(ErrorCode.CONFLICT, "취소 가능한 상태의 거래가 아닙니다.");
        }
        this.status = TradeStatus.CANCELLED;
        this.product.restoreSelling();
        this.paymentDueAt = null;
    }

    /**
     * 도메인 객체를 만료 상태로 변경한다.
     */
    public void expire() {
        if (this.status != TradeStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "결제 대기 상태인 거래만 만료할 수 있습니다.");
        }
        this.status = TradeStatus.EXPIRED;
        this.product.restoreSelling();
        this.paymentDueAt = null;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param userId 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public boolean isParticipant(Long userId) {
        return seller.getId().equals(userId) || buyer.getId().equals(userId);
    }

    /**
     * 'getCounterpart' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param userId 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public User getCounterpart(Long userId) {
        if (seller.getId().equals(userId)) {
            return buyer;
        }
        if (buyer.getId().equals(userId)) {
            return seller;
        }
        throw new IllegalArgumentException("거래 참여자가 아닙니다.");
    }
}
