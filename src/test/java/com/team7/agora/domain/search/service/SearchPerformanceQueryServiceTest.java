package com.team7.agora.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

class SearchPerformanceQueryServiceTest {

    @Test
    void getComparisonReturnsStatsForRootAdmin() {
        SearchPerformanceRecorder recorder = new SearchPerformanceRecorder();
        recorder.record("v1", 1_000_000L, true);
        SearchPerformanceQueryService service = new SearchPerformanceQueryService(recorder);
        AuthUser rootAdmin = new AuthUser(1L, "root@admin.com", "ROOT_ADMIN", "최고관리자");

        SearchPerformanceResponse response = service.getComparison(rootAdmin);

        assertThat(response.v1().callCount()).isEqualTo(1);
        assertThat(response.v2().callCount()).isZero();
    }

    @Test
    void getComparisonRejectsNonRootAdmin() {
        SearchPerformanceQueryService service = new SearchPerformanceQueryService(new SearchPerformanceRecorder());
        AuthUser settlementAdmin = new AuthUser(2L, "settlement@admin.com", "SETTLEMENT_ADMIN", "정산관리자");

        assertThatThrownBy(() -> service.getComparison(settlementAdmin))
            .isInstanceOf(BusinessException.class);
    }
}
