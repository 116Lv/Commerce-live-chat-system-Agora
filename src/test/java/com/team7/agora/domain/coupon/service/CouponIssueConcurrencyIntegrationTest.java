package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponIssueConcurrencyIntegrationTest {

    private static final int TOTAL_QUANTITY = 5;
    private static final int CONCURRENT_USERS = 30;
    private static final int MAX_RETRY_ATTEMPTS = 30;
    private static final long RETRY_BACKOFF_MILLIS = 20L;

    @Autowired
    private CouponIssueService couponIssueService;

    @Autowired
    private CouponEventRepository couponEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void issue_withRealDbAndRedisLock_neverExceedsTotalQuantity() throws InterruptedException {
        CouponEvent event = couponEventRepository.save(CouponEvent.create(
            "통합테스트 선착순 쿠폰",
            TOTAL_QUANTITY,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1)
        ));

        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            User user = userRepository.save(
                User.signup("concurrency-it-" + i + "@test.com", "encoded", "동시성유저" + i, "010" + (10000000 + i))
            );
            userIds.add(user.getId());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger();

        for (Long userId : userIds) {
            executorService.submit(() -> {
                try {
                    start.await();
                    if (issueWithRetry(userId, event.getId())) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await();
        executorService.shutdown();

        CouponEvent reloaded = couponEventRepository.findById(event.getId()).orElseThrow();

        assertThat(reloaded.getIssuedQuantity()).isLessThanOrEqualTo(TOTAL_QUANTITY);
        assertThat(successCount.get()).isEqualTo(reloaded.getIssuedQuantity());
        assertThat(successCount.get()).isEqualTo(TOTAL_QUANTITY);
    }

    // 서버는 락 경합 시 즉시 실패(Fail Fast)하므로, 클라이언트 쪽 재시도를 흉내내야 5장이 5명에게 분배되는 것까지 검증된다.
    private boolean issueWithRetry(Long userId, Long couponEventId) {
        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                couponIssueService.issue(userId, couponEventId);
                return true;
            } catch (Exception e) {
                sleep();
            }
        }
        return false;
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_BACKOFF_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
