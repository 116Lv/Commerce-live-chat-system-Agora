package com.team7.agora.domain.trade.entity;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.user.entity.User;
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
        this.paymentDueAt = LocalDateTime.now().plusHours(24);
    }

    public static Trade start(Product product, User seller, User buyer, BigDecimal price) {
        return new Trade(product, seller, buyer, price);
    }

    public void markPaid() {
        if (this.status != TradeStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("결제 대기 상태인 거래만 결제 완료 처리할 수 있습니다.");
        }
        this.status = TradeStatus.PAID;
        this.product.markSold();
        this.paymentDueAt = null;
    }

    public void complete() {
        if (this.status != TradeStatus.PAID) {
            throw new IllegalStateException("결제가 완료된 거래만 완료할 수 있습니다.");
        }
        this.status = TradeStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != TradeStatus.PAYMENT_PENDING && this.status != TradeStatus.PAID) {
            throw new IllegalStateException("취소 가능한 상태의 거래가 아닙니다.");
        }
        this.status = TradeStatus.CANCELLED;
        this.product.restoreSelling();
        this.paymentDueAt = null;
    }

    public void expire() {
        if (this.status != TradeStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("결제 대기 상태인 거래만 만료할 수 있습니다.");
        }
        this.status = TradeStatus.EXPIRED;
        this.product.restoreSelling();
        this.paymentDueAt = null;
    }

    public boolean isParticipant(Long userId) {
        return seller.getId().equals(userId) || buyer.getId().equals(userId);
    }

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
