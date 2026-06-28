package com.team7.agora.domain.nego.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "agora.redisson.enabled=false",
    "agora.chat.redis-listener.enabled=false"
})
class NegoServiceConcurrencyIntegrationTest {

    private final NegoService negoService;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ProductRepository productRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final NegoOfferRepository negoOfferRepository;
    private final TradeRepository tradeRepository;
    private final EntityManager entityManager;

    @MockitoBean
    private ChatRedisPublisher chatRedisPublisher;

    @Autowired
    NegoServiceConcurrencyIntegrationTest(
        NegoService negoService,
        UserRepository userRepository,
        RegionRepository regionRepository,
        ProductRepository productRepository,
        ChatRoomRepository chatRoomRepository,
        NegoOfferRepository negoOfferRepository,
        TradeRepository tradeRepository,
        EntityManager entityManager
    ) {
        this.negoService = negoService;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.productRepository = productRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.negoOfferRepository = negoOfferRepository;
        this.tradeRepository = tradeRepository;
        this.entityManager = entityManager;
    }

    @Test
    void concurrentCreateAndAcceptLeavesNoActiveOfferAfterProductReserved() throws Exception {
        int createRequestCount = 12;
        String suffix = String.valueOf(System.nanoTime());
        User seller = userRepository.save(User.signup(
            "nego-race-seller-" + suffix + "@test.com",
            "encoded",
            "raceSeller",
            "01090000000"
        ));
        User acceptedBuyer = userRepository.save(User.signup(
            "nego-race-accepted-" + suffix + "@test.com",
            "encoded",
            "acceptedBuyer",
            "01090000001"
        ));
        List<User> competingBuyers = userRepository.saveAll(createCompetingBuyers(suffix, createRequestCount));
        Region region = regionRepository.save(Region.create(
            "nego race region " + suffix,
            "NR" + suffix.substring(Math.max(0, suffix.length() - 10)),
            "Seoul",
            "Gangnam",
            "Yeoksam"
        ));
        Product product = productRepository.save(Product.create(
            seller,
            region,
            "nego race product " + suffix,
            "product for concurrent create accept test",
            BigDecimal.valueOf(100000),
            "digital"
        ));
        ChatRoom acceptedRoom = chatRoomRepository.save(ChatRoom.open(product, acceptedBuyer));
        NegoOffer acceptedOffer = negoOfferRepository.save(NegoOffer.create(
            acceptedRoom,
            acceptedBuyer,
            BigDecimal.valueOf(90000)
        ));
        List<ChatRoom> competingRooms = chatRoomRepository.saveAll(competingBuyers.stream()
            .map(buyer -> ChatRoom.open(product, buyer))
            .toList());

        // H2 lock scheduling is nondeterministic, so verify final invariants instead of the winning thread order.
        var executor = Executors.newFixedThreadPool(createRequestCount + 1);
        CountDownLatch ready = new CountDownLatch(createRequestCount + 1);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            tasks.add(() -> {
                ready.countDown();
                start.await();
                negoService.acceptOffer(seller.getId(), acceptedOffer.getId());
                return true;
            });
            for (int i = 0; i < competingRooms.size(); i++) {
                ChatRoom room = competingRooms.get(i);
                User buyer = competingBuyers.get(i);
                tasks.add(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        negoService.createOffer(buyer.getId(), room.getId(), BigDecimal.valueOf(91000));
                        return true;
                    } catch (BusinessException e) {
                        return false;
                    }
                });
            }

            List<Future<Boolean>> futures = tasks.stream()
                .map(executor::submit)
                .toList();

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            long acceptedCount = 0;
            long createSuccessCount = 0;
            for (int i = 0; i < futures.size(); i++) {
                boolean success = futures.get(i).get(15, TimeUnit.SECONDS);
                if (i == 0 && success) {
                    acceptedCount++;
                } else if (i > 0 && success) {
                    createSuccessCount++;
                }
            }

            assertThat(acceptedCount).isEqualTo(1);
            assertThat(createSuccessCount).isBetween(0L, (long) createRequestCount);
        } finally {
            executor.shutdownNow();
        }

        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();
        List<NegoOffer> productOffers = findOffersByProductId(product.getId());
        List<Trade> productTrades = tradeRepository.findAll().stream()
            .filter(trade -> trade.getProduct().getId().equals(product.getId()))
            .toList();

        assertThat(reloadedProduct.getStatus()).isEqualTo(ProductStatus.RESERVED);
        assertThat(productTrades).hasSize(1);
        assertThat(productOffers)
            .filteredOn(offer -> offer.getId().equals(acceptedOffer.getId()))
            .singleElement()
            .extracting(NegoOffer::getStatus)
            .isEqualTo(NegoOfferStatus.ACCEPTED);
        assertThat(productOffers)
            .filteredOn(offer -> NegoOffer.ACTIVE_STATUSES.contains(offer.getStatus()))
            .isEmpty();
    }

    private List<User> createCompetingBuyers(String suffix, int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(User.signup(
                "nego-race-buyer-" + suffix + "-" + i + "@test.com",
                "encoded",
                "raceBuyer" + i,
                "01091" + String.format("%06d", i)
            ));
        }
        return users;
    }

    private List<NegoOffer> findOffersByProductId(Long productId) {
        return entityManager.createQuery(
                "select n from NegoOffer n join n.chatRoom cr where cr.product.id = :productId",
                NegoOffer.class
            )
            .setParameter("productId", productId)
            .getResultList();
    }
}
