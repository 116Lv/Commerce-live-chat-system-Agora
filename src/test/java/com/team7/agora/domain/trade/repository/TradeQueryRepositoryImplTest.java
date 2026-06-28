package com.team7.agora.domain.trade.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.trade.dto.request.MyTradeRole;
import com.team7.agora.domain.trade.dto.response.MyTradeResponse;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TradeQueryRepositoryImplTest {

    @Autowired
    private TradeQueryRepository tradeQueryRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser;
    private User seller;
    private User buyer;
    private User unrelatedSeller;
    private User unrelatedBuyer;
    private Region region;

    @BeforeEach
    void setUp() {
        currentUser = userRepository.save(User.signup("my-trades-current@test.com", "encoded", "current", "01000000001"));
        seller = userRepository.save(User.signup("my-trades-seller@test.com", "encoded", "seller", "01000000002"));
        buyer = userRepository.save(User.signup("my-trades-buyer@test.com", "encoded", "buyer", "01000000003"));
        unrelatedSeller = userRepository.save(User.signup("my-trades-unrelated-seller@test.com", "encoded", "unrelatedSeller", "01000000004"));
        unrelatedBuyer = userRepository.save(User.signup("my-trades-unrelated-buyer@test.com", "encoded", "unrelatedBuyer", "01000000005"));
        region = regionRepository.save(Region.create("my trades region", "MYTRADES001", "sido", "sigungu", "dong"));
    }

    @Test
    void findMyTrades_filtersBuyerTradesAndUsesSellerAsCounterpart() {
        Trade buyerTrade = tradeRepository.save(startTrade(seller, currentUser, "buyer product", 12000, 10000));
        paymentRepository.save(Payment.ready(buyerTrade, currentUser, BigDecimal.valueOf(10000), "my-trades-order-1"));
        tradeRepository.save(startTrade(currentUser, buyer, "seller product", 22000, 20000));
        tradeRepository.save(startTrade(unrelatedSeller, unrelatedBuyer, "unrelated product", 32000, 30000));

        Page<MyTradeResponse> responses = tradeQueryRepository.findMyTrades(
            currentUser.getId(),
            MyTradeRole.BUYER,
            PageRequest.of(0, 20)
        );

        assertThat(responses.getTotalElements()).isEqualTo(1);
        MyTradeResponse response = responses.getContent().get(0);
        assertThat(response.tradeId()).isEqualTo(buyerTrade.getId());
        assertThat(response.role()).isEqualTo("BUYER");
        assertThat(response.counterpartNickname()).isEqualTo("seller");
        assertThat(response.paymentStatus()).isEqualTo("READY");
    }

    @Test
    void findMyTrades_filtersSellerTradesAndUsesBuyerAsCounterpart() {
        tradeRepository.save(startTrade(seller, currentUser, "buyer product", 12000, 10000));
        Trade sellerTrade = tradeRepository.save(startTrade(currentUser, buyer, "seller product", 22000, 20000));
        tradeRepository.save(startTrade(unrelatedSeller, unrelatedBuyer, "unrelated product", 32000, 30000));

        Page<MyTradeResponse> responses = tradeQueryRepository.findMyTrades(
            currentUser.getId(),
            MyTradeRole.SELLER,
            PageRequest.of(0, 20)
        );

        assertThat(responses.getTotalElements()).isEqualTo(1);
        MyTradeResponse response = responses.getContent().get(0);
        assertThat(response.tradeId()).isEqualTo(sellerTrade.getId());
        assertThat(response.role()).isEqualTo("SELLER");
        assertThat(response.counterpartNickname()).isEqualTo("buyer");
        assertThat(response.paymentStatus()).isNull();
    }

    @Test
    void findMyTrades_allIncludesOnlyParticipatingBuyerAndSellerTrades() {
        Trade buyerTrade = tradeRepository.save(startTrade(seller, currentUser, "buyer product", 12000, 10000));
        Trade sellerTrade = tradeRepository.save(startTrade(currentUser, buyer, "seller product", 22000, 20000));
        tradeRepository.save(startTrade(unrelatedSeller, unrelatedBuyer, "unrelated product", 32000, 30000));

        Page<MyTradeResponse> responses = tradeQueryRepository.findMyTrades(
            currentUser.getId(),
            MyTradeRole.ALL,
            PageRequest.of(0, 20)
        );

        assertThat(responses.getTotalElements()).isEqualTo(2);
        assertThat(responses).extracting(MyTradeResponse::tradeId)
            .containsExactly(sellerTrade.getId(), buyerTrade.getId());
        assertThat(responses).extracting(MyTradeResponse::role)
            .containsExactly("SELLER", "BUYER");
    }

    private Trade startTrade(User seller, User buyer, String title, long productPrice, long tradePrice) {
        Product product = productRepository.save(Product.create(
            seller,
            region,
            title,
            "description",
            BigDecimal.valueOf(productPrice),
            "MY_TRADES_TEST"
        ));
        return Trade.start(product, seller, buyer, BigDecimal.valueOf(tradePrice));
    }
}
