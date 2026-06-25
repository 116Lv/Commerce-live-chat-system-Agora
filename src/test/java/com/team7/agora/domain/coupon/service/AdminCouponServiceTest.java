package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.dto.response.CouponBroadcastResponse;
import com.team7.agora.domain.coupon.dto.response.AdminCouponResponse;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminCouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponIssueRepository couponIssueRepository;

    @Mock
    private UserRepository userRepository;

    private AdminCouponService newService() {
        return new AdminCouponService(
            couponRepository,
            couponEventRepository,
            couponIssueRepository,
            userRepository,
            LockService.local()
        );
    }

    private AdminCouponService newService(LockService lockService) {
        return new AdminCouponService(
            couponRepository,
            couponEventRepository,
            couponIssueRepository,
            userRepository,
            lockService
        );
    }

    @Test
    void createSavesCouponPolicy() {
        AdminCouponService service = newService();
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon coupon = invocation.getArgument(0);
            assignId(coupon, 1L);
            return coupon;
        });

        AdminCouponResponse response = service.create(
            principal(UserRole.ROOT_ADMIN),
            "신규 쿠폰",
            5000,
            10000,
            CouponType.FIRST_COME,
            30
        );

        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("신규 쿠폰");
        assertThat(response.type()).isEqualTo("FIRST_COME");
    }

    @Test
    void getDetailReturnsCouponPolicy() {
        AdminCouponService service = newService();
        Coupon coupon = Coupon.create("신규 쿠폰", 5000, 10000, CouponType.FIRST_COME, 30);
        assignId(coupon, 1L);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        AdminCouponResponse response = service.getDetail(principal(UserRole.USER_ADMIN), 1L);

        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("신규 쿠폰");
        assertThat(response.type()).isEqualTo("FIRST_COME");
    }

    @Test
    void getDetailRejectsMissingCoupon() {
        AdminCouponService service = newService();
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(principal(UserRole.ROOT_ADMIN), 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getDetailRejectsNonAdmin() {
        AdminCouponService service = newService();

        assertThatThrownBy(() -> service.getDetail(principal(UserRole.ROLE_USER), 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void issueToUsersIssuesCouponAndSkipsDuplicatedUsers() {
        AdminCouponService service = newService();
        Coupon coupon = Coupon.create("신규 쿠폰", 5000, 10000, CouponType.FIRST_COME, 30);
        assignId(coupon, 1L);
        User user = User.signup("user@test.com", "encoded", "사용자", "01012345678");
        User duplicatedUser = User.signup("dup@test.com", "encoded", "중복사용자", "01011112222");
        assignId(user, 10L);
        assignId(duplicatedUser, 11L);

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(userRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(user, duplicatedUser));
        when(couponEventRepository.save(any(CouponEvent.class))).thenAnswer(invocation -> {
            CouponEvent event = invocation.getArgument(0);
            assertThat(event.getTotalQuantity()).isEqualTo(1);
            return event;
        });
        when(couponIssueRepository.existsByCouponAndUser(any(Coupon.class), any(User.class)))
            .thenReturn(false, true);

        CouponBroadcastResponse response = service.issueToUsers(
            principal(UserRole.USER_ADMIN),
            1L,
            List.of(10L, 11L)
        );

        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.issuedCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isEqualTo(1);
    }

    @Test
    void issueToUsersDoesNotCreateEventWhenAllTargetsAreDuplicated() {
        AdminCouponService service = newService();
        Coupon coupon = Coupon.create("신규 쿠폰", 5000, 10000, CouponType.FIRST_COME, 30);
        assignId(coupon, 1L);
        User first = User.signup("first@test.com", "encoded", "첫번째", "01011112222");
        User second = User.signup("second@test.com", "encoded", "두번째", "01033334444");
        assignId(first, 10L);
        assignId(second, 11L);

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(userRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(first, second));
        when(couponIssueRepository.existsByCouponAndUser(any(Coupon.class), any(User.class)))
            .thenReturn(true, true);

        CouponBroadcastResponse response = service.issueToUsers(
            principal(UserRole.USER_ADMIN),
            1L,
            List.of(10L, 11L)
        );

        assertThat(response.issuedCount()).isEqualTo(0);
        assertThat(response.skippedCount()).isEqualTo(2);
        verify(couponEventRepository, never()).save(any(CouponEvent.class));
    }

    @Test
    void issueToUsersRejectsMissingCoupon() {
        AdminCouponService service = newService();
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issueToUsers(principal(UserRole.ROOT_ADMIN), 1L, List.of(10L)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void issueToUsersRejectsEmptyTargets() {
        AdminCouponService service = newService();
        Coupon coupon = Coupon.create("신규 쿠폰", 5000, 10000, CouponType.FIRST_COME, 30);
        assignId(coupon, 1L);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(userRepository.findAllById(List.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.issueToUsers(principal(UserRole.ROOT_ADMIN), 1L, List.of(10L)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void issueToUsersRejectsNonAdmin() {
        AdminCouponService service = newService();

        assertThatThrownBy(() -> service.issueToUsers(principal(UserRole.ROLE_USER), 1L, List.of(10L)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void getIssueHistoryReturnsCouponIssueRecords() {
        AdminCouponService service = newService();
        Coupon coupon = Coupon.create("신규 쿠폰", 5000, 10000, CouponType.FIRST_COME, 30);
        assignId(coupon, 1L);
        CouponEvent event = CouponEvent.create(
            "신규 쿠폰 발급",
            10,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1)
        );
        User user = User.signup("user@test.com", "encoded", "사용자", "01012345678");
        assignId(user, 10L);
        CouponIssue issue = CouponIssue.issue(coupon, event, user);
        assignId(issue, 100L);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponIssueRepository.findAllByCoupon(coupon)).thenReturn(List.of(issue));

        CouponIssueHistoryResponse response = service.getIssueHistory(principal(UserRole.USER_ADMIN), 1L);

        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.totalIssued()).isEqualTo(1);
        assertThat(response.issues()).hasSize(1);
        assertThat(response.issues().get(0).issueId()).isEqualTo(100L);
        assertThat(response.issues().get(0).userId()).isEqualTo(10L);
        assertThat(response.issues().get(0).userNickname()).isEqualTo("사용자");
    }

    @Test
    void getIssueHistoryRejectsMissingCoupon() {
        AdminCouponService service = newService();
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getIssueHistory(principal(UserRole.ROOT_ADMIN), 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getIssueHistoryRejectsNonAdmin() {
        AdminCouponService service = newService();

        assertThatThrownBy(() -> service.getIssueHistory(principal(UserRole.ROLE_USER), 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void createRejectsNonAdmin() {
        AdminCouponService service = newService();

        assertThatThrownBy(() -> service.create(
            principal(UserRole.ROLE_USER),
            "신규 쿠폰",
            5000,
            10000,
            CouponType.FIRST_COME,
            30
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(99L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "관리자");
    }
}
