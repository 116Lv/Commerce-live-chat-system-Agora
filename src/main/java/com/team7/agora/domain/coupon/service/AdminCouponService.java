package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.AdminCouponResponse;
import com.team7.agora.domain.coupon.dto.response.CouponBroadcastResponse;
import com.team7.agora.domain.coupon.dto.response.CouponIssueHistoryResponse;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.coupon.enums.CouponType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponIssueRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.lock.LockService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminCouponService {

    private static final int BROADCAST_PAGE_SIZE = 500;

    private final CouponRepository couponRepository;
    private final CouponEventRepository couponEventRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final UserRepository userRepository;
    private final LockService lockService;

    public AdminCouponService(
        CouponRepository couponRepository,
        CouponEventRepository couponEventRepository,
        CouponIssueRepository couponIssueRepository,
        UserRepository userRepository,
        LockService lockService
    ) {
        this.couponRepository = couponRepository;
        this.couponEventRepository = couponEventRepository;
        this.couponIssueRepository = couponIssueRepository;
        this.userRepository = userRepository;
        this.lockService = lockService;
    }

    @Transactional
    public AdminCouponResponse create(
        CustomUserDetails admin,
        String name,
        int discountAmount,
        int minOrderAmount,
        CouponType type,
        int validDays
    ) {
        validateAdminAuthority(admin);
        Coupon coupon = couponRepository.save(Coupon.create(name, discountAmount, minOrderAmount, type, validDays));
        return AdminCouponResponse.from(coupon);
    }

    public AdminCouponResponse getDetail(CustomUserDetails admin, Long couponId) {
        validateAdminAuthority(admin);
        Coupon coupon = findCoupon(couponId);
        return AdminCouponResponse.from(coupon);
    }

    @Transactional
    public CouponBroadcastResponse issueToUsers(CustomUserDetails admin, Long couponId, List<Long> userIds) {
        validateAdminAuthority(admin);
        Coupon coupon = findCoupon(couponId);

        return lockService.withLock(adminCouponLockKey(couponId), () -> issueToUsersWithLock(coupon, couponId, userIds));
    }

    private CouponBroadcastResponse issueToUsersWithLock(Coupon coupon, Long couponId, List<Long> userIds) {
        List<User> targets = userRepository.findAllById(userIds);
        if (targets.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "발급 대상 사용자가 없습니다.");
        }

        IssueResult result = issueTargets(coupon, targets, coupon.getName() + " 지정 발급");
        return new CouponBroadcastResponse(couponId, result.issuedCount(), result.skippedCount());
    }

    @Transactional
    public CouponBroadcastResponse broadcast(CustomUserDetails admin, Long couponId) {
        validateAdminAuthority(admin);
        Coupon coupon = findCoupon(couponId);

        return lockService.withLock(adminCouponLockKey(couponId), () -> broadcastWithLock(coupon, couponId));
    }

    private CouponBroadcastResponse broadcastWithLock(Coupon coupon, Long couponId) {
        int issuedCount = 0;
        int skippedCount = 0;
        boolean targetFound = false;
        Pageable pageable = PageRequest.of(0, BROADCAST_PAGE_SIZE);
        Page<User> page;
        do {
            page = userRepository.findAllByStatus(UserStatus.ACTIVE, pageable);
            if (!page.isEmpty()) {
                targetFound = true;
            }

            IssueResult result = issueTargets(coupon, page.getContent(), coupon.getName() + " 전체 발송");
            issuedCount += result.issuedCount();
            skippedCount += result.skippedCount();
            pageable = page.nextPageable();
        } while (page.hasNext());

        if (!targetFound) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "발급 대상 사용자가 없습니다.");
        }

        return new CouponBroadcastResponse(couponId, issuedCount, skippedCount);
    }

    private String adminCouponLockKey(Long couponId) {
        return "lock:admin-coupon:" + couponId;
    }

    private IssueResult issueTargets(Coupon coupon, List<User> targets, String eventName) {
        int skippedCount = 0;
        List<User> issueTargets = new ArrayList<>();
        for (User user : targets) {
            if (couponIssueRepository.existsByCouponAndUser(coupon, user)) {
                skippedCount++;
                continue;
            }
            issueTargets.add(user);
        }

        if (issueTargets.isEmpty()) {
            return new IssueResult(0, skippedCount);
        }

        LocalDateTime now = LocalDateTime.now();
        CouponEvent event = couponEventRepository.save(CouponEvent.create(
            eventName,
            issueTargets.size(),
            now.minusMinutes(1),
            now.plusDays(coupon.getValidDays())
        ));

        int issuedCount = 0;
        for (User user : issueTargets) {
            event.issue();
            couponIssueRepository.save(CouponIssue.issue(coupon, event, user));
            issuedCount++;
        }

        return new IssueResult(issuedCount, skippedCount);
    }

    private Coupon findCoupon(Long couponId) {
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 정책을 찾을 수 없습니다."));
    }

    public CouponIssueHistoryResponse getIssueHistory(CustomUserDetails admin, Long couponId) {
        validateAdminAuthority(admin);
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 정책을 찾을 수 없습니다."));
        return CouponIssueHistoryResponse.of(coupon.getId(), couponIssueRepository.findAllByCoupon(coupon));
    }

    private void validateAdminAuthority(CustomUserDetails admin) {
        if (admin == null || !(admin.getRole() == UserRole.ROOT_ADMIN || admin.getRole() == UserRole.USER_ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "쿠폰 정책 관리는 관리자만 수행할 수 있습니다.");
        }
    }

    private record IssueResult(int issuedCount, int skippedCount) {
    }
}
