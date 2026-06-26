package com.team7.agora.domain.search.service;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
public class SearchPerformanceQueryService {

    private final SearchPerformanceRecorder searchPerformanceRecorder;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param searchPerformanceRecorder 입력 값
     */
    public SearchPerformanceQueryService(SearchPerformanceRecorder searchPerformanceRecorder) {
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    /**
     * 데이터를 반환한다.
     * @param authUser 입력 값
     * @return 처리 결과
     */
    public SearchPerformanceResponse getComparison(AuthUser authUser) {
        if (authUser == null || !"ROOT_ADMIN".equals(authUser.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "검색 성능 비교 조회는 ROOT_ADMIN만 수행할 수 있습니다.");
        }
        return SearchPerformanceResponse.of(
            searchPerformanceRecorder.getStats("v1"),
            searchPerformanceRecorder.getStats("v2")
        );
    }
}
