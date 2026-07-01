package com.team7.agora.domain.coupon.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.global.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class CouponEventTest {

    @Test
    void createKeepsActiveStatusForCompatibility() {
        CouponEvent event = activeEvent();

        assertThat(event.getStatus()).isEqualTo(CouponEventStatus.ACTIVE);
    }

    @Test
    void createPendingCreatesPendingApprovalEvent() {
        CouponEvent event = pendingEvent();

        assertThat(event.getStatus()).isEqualTo(CouponEventStatus.PENDING_APPROVAL);
    }

    @Test
    void onlyActiveEventCanIssue() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 30, 12, 0);

        assertThatThrownBy(() -> pendingEvent().validateIssueable(now))
            .isInstanceOf(BusinessException.class);

        CouponEvent rejected = pendingEvent();
        rejected.reject();
        assertThatThrownBy(() -> rejected.validateIssueable(now))
            .isInstanceOf(BusinessException.class);

        CouponEvent stopRequested = activeEvent();
        stopRequested.requestStop();
        assertThatThrownBy(() -> stopRequested.validateIssueable(now))
            .isInstanceOf(BusinessException.class);

        CouponEvent stopped = stoppedEvent();
        assertThatThrownBy(() -> stopped.validateIssueable(now))
            .isInstanceOf(BusinessException.class);

        CouponEvent ended = activeEvent();
        ended.end();
        assertThatThrownBy(() -> ended.validateIssueable(now))
            .isInstanceOf(BusinessException.class);

        CouponEvent active = activeEvent();
        active.validateIssueable(now);
        assertThat(active.getStatus()).isEqualTo(CouponEventStatus.ACTIVE);
    }

    @Test
    void issueFailsForNonActiveStatuses() {
        assertThatThrownBy(() -> pendingEvent().issue())
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> rejectedEvent().issue())
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> stopRequestedEvent().issue())
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> stoppedEvent().issue())
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> endedEvent().issue())
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void approveChangesPendingApprovalToActive() {
        CouponEvent pending = pendingEvent();
        pending.approve();

        assertThat(pending.getStatus()).isEqualTo(CouponEventStatus.ACTIVE);
    }

    @Test
    void approveFailsForUnsupportedStatuses() {
        assertTransitionRejected(rejectedEvent(), CouponEvent::approve);
        assertTransitionRejected(activeEvent(), CouponEvent::approve);
        assertTransitionRejected(stopRequestedEvent(), CouponEvent::approve);
        assertTransitionRejected(stoppedEvent(), CouponEvent::approve);
        assertTransitionRejected(endedEvent(), CouponEvent::approve);
    }

    @Test
    void rejectChangesOnlyPendingApprovalToRejected() {
        CouponEvent pending = pendingEvent();
        pending.reject();

        assertThat(pending.getStatus()).isEqualTo(CouponEventStatus.REJECTED);
        assertThatThrownBy(() -> activeEvent().reject())
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectFailsForUnsupportedStatuses() {
        assertTransitionRejected(rejectedEvent(), CouponEvent::reject);
        assertTransitionRejected(activeEvent(), CouponEvent::reject);
        assertTransitionRejected(stopRequestedEvent(), CouponEvent::reject);
        assertTransitionRejected(stoppedEvent(), CouponEvent::reject);
        assertTransitionRejected(endedEvent(), CouponEvent::reject);
    }

    @Test
    void requestStopChangesOnlyActiveToStopRequested() {
        CouponEvent active = activeEvent();
        active.requestStop();

        assertThat(active.getStatus()).isEqualTo(CouponEventStatus.STOP_REQUESTED);
        assertThatThrownBy(() -> pendingEvent().requestStop())
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void requestStopFailsForUnsupportedStatuses() {
        assertTransitionRejected(pendingEvent(), CouponEvent::requestStop);
        assertTransitionRejected(rejectedEvent(), CouponEvent::requestStop);
        assertTransitionRejected(stopRequestedEvent(), CouponEvent::requestStop);
        assertTransitionRejected(stoppedEvent(), CouponEvent::requestStop);
        assertTransitionRejected(endedEvent(), CouponEvent::requestStop);
    }

    @Test
    void stopChangesStopRequestedToStopped() {
        CouponEvent stopRequested = activeEvent();
        stopRequested.requestStop();
        stopRequested.stop();

        assertThat(stopRequested.getStatus()).isEqualTo(CouponEventStatus.STOPPED);
    }

    @Test
    void stoppedEventKeepsIssuedCouponRights() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 6, 30, 12, 0);
        CouponEvent event = activeEvent();
        Coupon issued = Coupon.createAvailableSlot(event);
        issued.assign(null, issuedAt, 7);

        event.requestStop();
        event.stop();

        assertThat(event.getStatus()).isEqualTo(CouponEventStatus.STOPPED);
        assertThat(issued.getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(issued.isUsableAt(issuedAt.plusDays(1))).isTrue();
    }

    @Test
    void stopFailsForUnsupportedStatuses() {
        assertTransitionRejected(pendingEvent(), CouponEvent::stop);
        assertTransitionRejected(rejectedEvent(), CouponEvent::stop);
        assertTransitionRejected(activeEvent(), CouponEvent::stop);
        assertTransitionRejected(stoppedEvent(), CouponEvent::stop);
        assertTransitionRejected(endedEvent(), CouponEvent::stop);
    }

    @Test
    void invalidTransitionMessageIncludesActionAndCurrentStatus() {
        assertThatThrownBy(() -> activeEvent().approve())
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("approve")
            .hasMessageContaining("ACTIVE");
    }

    @Test
    void endChangesOnlyActiveToEnded() {
        CouponEvent active = activeEvent();
        active.end();

        assertThat(active.getStatus()).isEqualTo(CouponEventStatus.ENDED);
        assertTransitionRejected(pendingEvent(), CouponEvent::end);
        assertTransitionRejected(rejectedEvent(), CouponEvent::end);
        assertTransitionRejected(stopRequestedEvent(), CouponEvent::end);
        assertTransitionRejected(stoppedEvent(), CouponEvent::end);
        assertTransitionRejected(endedEvent(), CouponEvent::end);
    }

    @Test
    void endedEventKeepsIssuedAndUsedCouponRights() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 6, 30, 12, 0);
        CouponEvent event = activeEvent();
        Coupon issued = Coupon.createAvailableSlot(event);
        Coupon used = Coupon.createAvailableSlot(event);
        issued.assign(null, issuedAt, 7);
        used.assign(null, issuedAt, 7);
        used.use(issuedAt.plusDays(1));

        event.end();

        assertThat(event.getStatus()).isEqualTo(CouponEventStatus.ENDED);
        assertThat(issued.getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(used.getStatus()).isEqualTo(CouponStatus.USED);
    }

    private void assertTransitionRejected(CouponEvent event, Consumer<CouponEvent> transition) {
        assertThatThrownBy(() -> transition.accept(event))
            .isInstanceOf(BusinessException.class);
    }

    private CouponEvent activeEvent() {
        return CouponEvent.create(
            CouponEventType.FIRST_COME,
            "First purchase coupon",
            5,
            LocalDateTime.of(2026, 6, 30, 0, 0),
            LocalDateTime.of(2026, 6, 30, 23, 59),
            5000,
            10000,
            30
        );
    }

    private CouponEvent pendingEvent() {
        return CouponEvent.createPending(
            CouponEventType.FIRST_COME,
            "First purchase coupon",
            5,
            LocalDateTime.of(2026, 6, 30, 0, 0),
            LocalDateTime.of(2026, 6, 30, 23, 59),
            5000,
            10000,
            30
        );
    }

    private CouponEvent rejectedEvent() {
        CouponEvent event = pendingEvent();
        event.reject();
        return event;
    }

    private CouponEvent stopRequestedEvent() {
        CouponEvent event = activeEvent();
        event.requestStop();
        return event;
    }

    private CouponEvent stoppedEvent() {
        CouponEvent event = activeEvent();
        event.requestStop();
        event.stop();
        return event;
    }

    private CouponEvent endedEvent() {
        CouponEvent event = activeEvent();
        event.end();
        return event;
    }
}
