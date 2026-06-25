package com.team7.agora.domain.nego.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.chat.service.ChatSystemMessageService;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NegoExpirationServiceTest {

    @Mock
    private NegoOfferRepository negoOfferRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatSystemMessageService chatSystemMessageService;

    private NegoExpirationService negoExpirationService;
    private User seller;
    private User buyer;
    private Product product;

    @BeforeEach
    void setUp() {
        negoExpirationService = new NegoExpirationService(
            negoOfferRepository, tradeRepository, chatRoomRepository, chatSystemMessageService
        );
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
    }

    @Test
    void expireDueOffersMarksOnlyOverdueActiveOffersExpired() {
        ChatRoom chatRoom = ChatRoom.open(product, buyer);
        assignId(chatRoom, 100L);
        NegoOffer overdueOffer = NegoOffer.create(chatRoom, buyer, BigDecimal.valueOf(45000));
        assignId(overdueOffer, 1000L);
        LocalDateTime now = overdueOffer.getExpiresAt().plusSeconds(1);
        when(negoOfferRepository.findAllByStatusInAndExpiresAtLessThanEqual(
            List.of(NegoOfferStatus.PENDING, NegoOfferStatus.EXTENSION_REQUESTED, NegoOfferStatus.EXTENDED),
            now
        )).thenReturn(List.of(overdueOffer));

        int expiredCount = negoExpirationService.expireDueOffers(now);

        assertThat(expiredCount).isEqualTo(1);
        assertThat(overdueOffer.getStatus()).isEqualTo(NegoOfferStatus.EXPIRED);
    }

    @Test
    void expireDuePaymentReservationsRestoresProductSelling() {
        product.markReserved();
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(45000));
        assignId(trade, 2000L);
        LocalDateTime now = trade.getPaymentDueAt().plusSeconds(1);
        when(tradeRepository.findAllByStatusAndPaymentDueAtLessThanEqual(TradeStatus.PAYMENT_PENDING, now))
            .thenReturn(List.of(trade));

        int expiredCount = negoExpirationService.expireDuePaymentReservations(now);

        assertThat(expiredCount).isEqualTo(1);
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.EXPIRED);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SELLING);
    }
}
