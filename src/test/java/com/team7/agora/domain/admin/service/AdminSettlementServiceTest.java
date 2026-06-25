package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminSettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    private AdminSettlementService adminSettlementService;
    private CustomUserDetails rootAdmin;

    @BeforeEach
    void setUp() {
        adminSettlementService = new AdminSettlementService(settlementRepository);
        rootAdmin = new CustomUserDetails(
            99L,
            "root@admin.com",
            "encoded",
            UserRole.ROOT_ADMIN,
            UserStatus.ACTIVE,
            "최고관리자"
        );
    }

    @Test
    void settleChangesReadySettlementToSettled() {
        Settlement settlement = Settlement.pending(payment());
        settlement.complete();
        assignId(settlement, 200L);
        when(settlementRepository.findById(200L)).thenReturn(Optional.of(settlement));

        var response = adminSettlementService.settle(rootAdmin, 200L);

        assertThat(response.settlementId()).isEqualTo(200L);
        assertThat(response.status()).isEqualTo("SETTLED");
        assertThat(settlement.getSettledAt()).isNotNull();
    }

    @Test
    void normalUserCannotSettle() {
        CustomUserDetails user = new CustomUserDetails(
            1L,
            "user@test.com",
            "encoded",
            UserRole.ROLE_USER,
            UserStatus.ACTIVE,
            "일반사용자"
        );

        assertThatThrownBy(() -> adminSettlementService.settle(user, 200L))
            .isInstanceOf(BusinessException.class);
    }

    private Payment payment() {
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        User buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        Product product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
        product.markReserved();
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));
        assignId(trade, 100L);
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        return payment;
    }
}
