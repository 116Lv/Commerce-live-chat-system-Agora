// 발표용 단일 비관적 락 데모: 네고(오퍼 락) / 거래(상품 락) before/after 비교
package com.team7.agora.domain.trade.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.realtime.ChatRedisPublisher;
import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
import com.team7.agora.domain.nego.repository.NegoOfferRepository;
import com.team7.agora.domain.nego.service.NegoService;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 발표용 데모: 쿠폰은 분산락+비관적 락 2중이지만, 네고·거래는 상황에 맞춰 "비관적 락 단일"만 적용했음을 보여준다.
 * - 네고: 같은 오퍼 동시 수락 → 비관적 락(오퍼 행)으로 거래 1건만 생성
 * - 거래: 같은 상품 동시 거래 시작 → 비관적 락(상품 행)으로 거래 1건만 생성(이중 판매 방지)
 * 각 케이스를 락 없는 naive 구현(before)과 실제 구현(after)으로 비교한다.
 */
@SpringBootTest(properties = {
    "agora.redisson.enabled=false",
    "agora.chat.redis-listener.enabled=false"
})
class LockStrategyDemoTest {

    private static final int REQUEST_COUNT = 20;

    private final NegoService negoService;
    private final TradeService tradeService;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ProductRepository productRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final NegoOfferRepository negoOfferRepository;
    private final TradeRepository tradeRepository;
    private final TransactionTemplate tx;

    @MockitoBean
    private ChatRedisPublisher chatRedisPublisher; // Redis 발행은 목 처리(테스트에 Redis 불필요)

    @Autowired
    LockStrategyDemoTest(
        NegoService negoService,
        TradeService tradeService,
        UserRepository userRepository,
        RegionRepository regionRepository,
        ProductRepository productRepository,
        ChatRoomRepository chatRoomRepository,
        NegoOfferRepository negoOfferRepository,
        TradeRepository tradeRepository,
        PlatformTransactionManager txManager
    ) {
        this.negoService = negoService;
        this.tradeService = tradeService;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.productRepository = productRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.negoOfferRepository = negoOfferRepository;
        this.tradeRepository = tradeRepository;
        this.tx = new TransactionTemplate(txManager);
    }

    // ===================== 네고: 오퍼 비관적 락 =====================

    @Test
    void nego_before_naiveWithoutLock_overCreatesTrades() throws Exception {
        Fixture f = newFixturePendingOffer();
        runConcurrently(user ->
            tx.executeWithoutResult(status -> naiveAcceptNego(f.offerId)));

        long trades = countTrades(f.productId);
        printSummary("네고 BEFORE (naive: 오퍼 락 없음)", trades);
        assertThat(trades).isGreaterThan(1); // 같은 오퍼가 여러 번 수락되어 거래 중복 생성
    }

    @Test
    void nego_after_actualWithPessimisticLock_singleTrade() throws Exception {
        Fixture f = newFixturePendingOffer();
        AtomicLong success = new AtomicLong();
        runConcurrently(user -> {
            try {
                negoService.acceptOffer(f.sellerId, f.offerId);
                success.incrementAndGet();
            } catch (BusinessException | IllegalStateException ignored) {
                // 비관적 락으로 직렬화되어 두 번째부터는 거절됨
            }
        });

        long trades = countTrades(f.productId);
        printSummary("네고 AFTER (실제: 오퍼 비관적 락 FOR UPDATE)", trades);
        assertThat(trades).isEqualTo(1);
        assertThat(success.get()).isEqualTo(1);
    }

    // ===================== 거래: 상품 비관적 락 =====================

    @Test
    void trade_before_naiveWithoutLock_doubleSells() throws Exception {
        Fixture f = newFixtureAcceptedOffer();
        runConcurrently(user ->
            tx.executeWithoutResult(status -> naiveStartTrade(f.productId, f.offerId)));

        long trades = countTrades(f.productId);
        printSummary("거래 BEFORE (naive: 상품 락 없음 → 이중 판매)", trades);
        assertThat(trades).isGreaterThan(1);
    }

    @Test
    void trade_after_actualWithPessimisticLock_singleSell() throws Exception {
        Fixture f = newFixtureAcceptedOffer();
        NegoOffer acceptedOffer = negoOfferRepository.findById(f.offerId).orElseThrow();
        Product product = productRepository.findById(f.productId).orElseThrow();
        AtomicLong success = new AtomicLong();
        runConcurrently(user -> {
            try {
                tradeService.createTradeFromAcceptedOffer(product, acceptedOffer);
                success.incrementAndGet();
            } catch (BusinessException ignored) {
                // 상품 비관적 락으로 직렬화 → 두 번째부터 "이미 거래 중" 거절
            }
        });

        long trades = countTrades(f.productId);
        printSummary("거래 AFTER (실제: 상품 비관적 락 FOR UPDATE)", trades);
        assertThat(trades).isEqualTo(1);
        assertThat(success.get()).isEqualTo(1);
    }

    // ===================== naive 구현(비교 기준) =====================

    /** 오퍼 락 없이 "PENDING이면 거래 생성" — 동시 수락 시 거래 중복 생성. */
    private void naiveAcceptNego(Long offerId) {
        NegoOffer offer = negoOfferRepository.findById(offerId).orElseThrow(); // 락 없는 조회
        if (offer.getStatus() != NegoOfferStatus.PENDING) {
            return;
        }
        sleepQuietly(20);
        Product product = offer.getChatRoom().getProduct();
        Trade trade = Trade.start(product, product.getSeller(), offer.getRequester(), offer.getOfferPrice());
        tradeRepository.save(trade);
    }

    /** 상품 락 없이 "SELLING이고 거래 없으면 생성" — 동시 시작 시 이중 판매. */
    private void naiveStartTrade(Long productId, Long offerId) {
        Product product = productRepository.findById(productId).orElseThrow(); // 락 없는 조회
        if (product.getStatus() != ProductStatus.SELLING
            || tradeRepository.existsByProductAndStatusNot(product, TradeStatus.CANCELLED)) {
            return;
        }
        sleepQuietly(20);
        NegoOffer offer = negoOfferRepository.findById(offerId).orElseThrow();
        Trade trade = Trade.start(product, product.getSeller(), offer.getRequester(), offer.getOfferPrice());
        product.markReserved();
        productRepository.save(product);
        tradeRepository.save(trade);
    }

    // ===================== 픽스처 =====================

    private record Fixture(Long sellerId, Long productId, Long offerId) {
    }

    private Fixture newFixturePendingOffer() {
        Ctx c = newProductWithChatRoom();
        NegoOffer offer = negoOfferRepository.save(NegoOffer.create(c.room, c.buyer, BigDecimal.valueOf(90000)));
        return new Fixture(c.seller.getId(), c.product.getId(), offer.getId());
    }

    private Fixture newFixtureAcceptedOffer() {
        Ctx c = newProductWithChatRoom();
        NegoOffer offer = NegoOffer.create(c.room, c.buyer, BigDecimal.valueOf(90000));
        offer.accept(); // PENDING → ACCEPTED
        offer = negoOfferRepository.save(offer);
        return new Fixture(c.seller.getId(), c.product.getId(), offer.getId());
    }

    private record Ctx(User seller, User buyer, Product product, ChatRoom room) {
    }

    private Ctx newProductWithChatRoom() {
        String s = String.valueOf(System.nanoTime());
        User seller = userRepository.save(User.signup("lock-seller-" + s + "@test.com", "encoded", "lockSeller", "0109" + tail(s)));
        User buyer = userRepository.save(User.signup("lock-buyer-" + s + "@test.com", "encoded", "lockBuyer", "0108" + tail(s)));
        Region region = regionRepository.save(Region.create(
            "lock region " + s, "LK" + s.substring(Math.max(0, s.length() - 10)), "Seoul", "Gangnam", "Yeoksam"));
        Product product = productRepository.save(Product.create(
            seller, region, "lock product " + s, "demo", BigDecimal.valueOf(100000), "digital"));
        ChatRoom room = chatRoomRepository.save(ChatRoom.open(product, buyer));
        return new Ctx(seller, buyer, product, room);
    }

    private String tail(String s) {
        String digits = s.replaceAll("\\D", "");
        return digits.substring(Math.max(0, digits.length() - 7));
    }

    // ===================== 동시성 하니스 =====================

    private void runConcurrently(Consumer<Integer> action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Void>> futures = IntStream.range(0, REQUEST_COUNT)
                .mapToObj(i -> executor.submit(() -> {
                    ready.countDown();
                    awaitQuietly(start);
                    action.accept(i);
                    return (Void) null;
                }))
                .toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<Void> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private long countTrades(Long productId) {
        return tradeRepository.findAll().stream()
            .filter(trade -> trade.getProduct().getId().equals(productId))
            .count();
    }

    private void printSummary(String label, long trades) {
        System.out.println();
        System.out.println("===== 단일 락 데모 | " + label + " =====");
        System.out.println("  동시 요청 수        : " + REQUEST_COUNT);
        System.out.println("  한 상품에 생성된 거래 : " + trades + "  (정상=1)");
        System.out.println("==================================================");
        System.out.println();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
