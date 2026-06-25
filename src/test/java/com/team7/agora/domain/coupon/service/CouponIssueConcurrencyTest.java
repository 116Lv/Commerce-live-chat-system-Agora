package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.repository.InMemoryCouponEventRepository;
import com.team7.agora.domain.coupon.repository.InMemoryCouponIssueRepository;
import com.team7.agora.domain.coupon.repository.InMemoryCouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.lock.LockService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueConcurrencyTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void issue_concurrentlyNeverExceedsTotalQuantity() throws InterruptedException {
        InMemoryCouponEventRepository eventRepository = new InMemoryCouponEventRepository();
        InMemoryCouponRepository couponRepository = new InMemoryCouponRepository();
        InMemoryCouponIssueRepository issueRepository = new InMemoryCouponIssueRepository();
        CouponEvent event = eventRepository.save(CouponEvent.create(
            "동네 선착순 쿠폰",
            5,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1)
        ));
        couponRepository.save(Coupon.firstCome("5천원 할인", 5000, 10000, 30));
        for (long i = 1; i <= 30; i++) {
            when(userRepository.existsById(i)).thenReturn(true);
            when(userRepository.findById(i)).thenReturn(
                Optional.of(User.signup("user" + i + "@test.com", "encoded", "유저" + i, "01012345678"))
            );
        }
        CouponIssueTransactionExecutor executor = new CouponIssueTransactionExecutor(
            eventRepository, couponRepository, issueRepository, userRepository
        );
        CouponIssueService service = new CouponIssueService(userRepository, LockService.local(), executor);
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(30);

        for (long userId = 1; userId <= 30; userId++) {
            long currentUserId = userId;
            executorService.submit(() -> {
                try {
                    start.await();
                    service.issue(currentUserId, event.getId());
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await();
        executorService.shutdown();

        assertThat(event.getIssuedQuantity()).isEqualTo(5);
        assertThat(issueRepository.count()).isEqualTo(5);
    }
}
