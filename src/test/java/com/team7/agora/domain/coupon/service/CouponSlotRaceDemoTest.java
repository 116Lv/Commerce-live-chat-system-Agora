// 선착순 쿠폰 동시성: naive 구현(락·FOR UPDATE 없음) vs 실제 구현 before/after 비교 데모
package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.time.AgoraClock;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 발표용 동시성 before/after 데모.
 * - BEFORE: 락도 FOR UPDATE도 없는 naive 발급 → 재고 초과 발급 발생
 * - AFTER : 실제 코드(비관적 락 FOR UPDATE + Redisson 분산락) → 정확히 재고만 발급
 */
@SpringBootTest(properties = {
    "agora.redisson.enabled=false",
    "agora.chat.redis-listener.enabled=false"
})
class CouponSlotRaceDemoTest {

    private static final int SLOT_COUNT = 5;
    private static final int REQUEST_COUNT = 30;

    private final CouponSlotService couponSlotService;
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate tx;

    @Autowired
    CouponSlotRaceDemoTest(
        CouponSlotService couponSlotService,
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        UserRepository userRepository,
        PlatformTransactionManager txManager
    ) {
        this.couponSlotService = couponSlotService;
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
        this.tx = new TransactionTemplate(txManager);
    }

    @Test
    void before_naiveWithoutLock_overIssues() throws Exception {
        CouponEvent event = newEvent();
        List<User> users = newUsers();

        runConcurrently(users, user ->
            tx.executeWithoutResult(status -> naiveAssign(event.getId(), user.getId())));

        int issued = assignedCount(event.getId());
        printSummary("BEFORE  (naive: 락 없음 · FOR UPDATE 없음)", issued);
        assertThat(issued).isGreaterThan(SLOT_COUNT); // 초과 발급이 발생함을 증명
    }

    @Test
    void after_actualWithLock_exactlyStock() throws Exception {
        CouponEvent event = newEvent();
        couponRepository.saveAll(IntStream.range(0, SLOT_COUNT)
            .mapToObj(ignored -> Coupon.createAvailableSlot(event))
            .toList());
        List<User> users = newUsers();

        runConcurrently(users, user -> {
            try {
                couponSlotService.assignSlot(event.getId(), user.getId());
            } catch (BusinessException ignored) {
                // 매진 시 정상적으로 거절됨
            }
        });

        int issued = assignedCount(event.getId());
        printSummary("AFTER   (실제 코드: 비관적 락 FOR UPDATE + 분산락)", issued);
        assertThat(issued).isEqualTo(SLOT_COUNT); // 정확히 재고만 발급
    }

    /** 락/FOR UPDATE 없이 "개수 세고 → 발급"하는 전형적 race condition 구현(비교 기준). */
    private void naiveAssign(Long eventId, Long userId) {
        CouponEvent event = couponEventRepository.findById(eventId).orElseThrow();
        int issued = assignedCount(eventId); // 락 없는 단순 카운트 조회
        if (issued >= event.getTotalQuantity()) {
            return; // 매진으로 판단(하지만 동시 조회라 막지 못함)
        }
        sleepQuietly(20); // 조회와 발급 사이 race window를 넓혀 재현성 확보
        User user = userRepository.findById(userId).orElseThrow();
        Coupon coupon = Coupon.createAvailableSlot(event);
        coupon.assign(user, AgoraClock.now(), event.getValidDays());
        couponRepository.save(coupon);
    }

    private int assignedCount(Long eventId) {
        return couponRepository.findAllByCouponEventIdAndUserIsNotNull(eventId).size();
    }

    private CouponEvent newEvent() {
        return couponEventRepository.save(CouponEvent.create(
            CouponEventType.FIRST_COME,
            "race-demo-" + System.nanoTime(),
            SLOT_COUNT,
            AgoraClock.now().minusMinutes(1),
            AgoraClock.now().plusMinutes(10),
            5000, 10000, 30
        ));
    }

    private List<User> newUsers() {
        return userRepository.saveAll(IntStream.range(0, REQUEST_COUNT)
            .mapToObj(i -> User.signup(
                "race-" + System.nanoTime() + "-" + i + "@test.com",
                "encoded",
                "raceUser" + i,
                "0100000" + String.format("%04d", i)
            ))
            .toList());
    }

    private void runConcurrently(List<User> users, Consumer<User> action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Void>> futures = users.stream()
                .map(user -> executor.submit(() -> {
                    ready.countDown();
                    awaitQuietly(start);
                    action.accept(user);
                    return (Void) null;
                }))
                .toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown(); // 모든 스레드 동시 출발
            for (Future<Void> future : futures) {
                future.get(20, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private void printSummary(String label, int issued) {
        System.out.println();
        System.out.println("===== 동시성 데모 | " + label + " =====");
        System.out.println("  재고(slot)      : " + SLOT_COUNT);
        System.out.println("  동시 요청 수     : " + REQUEST_COUNT);
        System.out.println("  실제 발급된 수   : " + issued);
        System.out.println("  초과 발급        : " + Math.max(0, issued - SLOT_COUNT));
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
