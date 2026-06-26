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
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "agora.redisson.enabled=false",
    "agora.chat.redis-listener.enabled=false"
})
class CouponSlotConcurrencyIntegrationTest {

    private final CouponSlotService couponSlotService;
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Autowired
    CouponSlotConcurrencyIntegrationTest(
        CouponSlotService couponSlotService,
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        UserRepository userRepository
    ) {
        this.couponSlotService = couponSlotService;
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    @Test
    void concurrentRequestsDoNotIssueMoreThanAvailableSlots() throws Exception {
        int slotCount = 5;
        int requestCount = 30;
        CouponEvent event = couponEventRepository.save(CouponEvent.create(
            CouponEventType.FIRST_COME,
            "concurrency coupon " + System.nanoTime(),
            slotCount,
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusMinutes(10),
            5000,
            10000,
            30
        ));
        couponRepository.saveAll(IntStream.range(0, slotCount)
            .mapToObj(ignored -> Coupon.createAvailableSlot(event))
            .toList());
        List<User> users = userRepository.saveAll(IntStream.range(0, requestCount)
            .mapToObj(i -> User.signup(
                "coupon-concurrency-" + System.nanoTime() + "-" + i + "@test.com",
                "encoded",
                "couponUser" + i,
                "0100000" + String.format("%04d", i)
            ))
            .toList());

        var executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futures = users.stream()
                .map(user -> executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        couponSlotService.assignSlot(event.getId(), user.getId());
                        return true;
                    } catch (BusinessException e) {
                        return false;
                    }
                }))
                .toList();

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            long successCount = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(10, TimeUnit.SECONDS)) {
                    successCount++;
                }
            }

            assertThat(successCount).isEqualTo(slotCount);
            assertThat(couponRepository.findAllByCouponEventIdAndUserIsNotNull(event.getId())).hasSize(slotCount);
            assertThat(couponEventRepository.findById(event.getId()).orElseThrow().getIssuedQuantity())
                .isEqualTo(slotCount);
        } finally {
            executor.shutdownNow();
        }
    }
}
