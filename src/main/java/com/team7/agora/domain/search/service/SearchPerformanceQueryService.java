package com.team7.agora.domain.search.service;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Search Performance Query 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
public class SearchPerformanceQueryService {

    private final SearchPerformanceRecorder searchPerformanceRecorder;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param searchPerformanceRecorder 검색 성능 지표를 기록하는 컴포넌트
     */
    public SearchPerformanceQueryService(SearchPerformanceRecorder searchPerformanceRecorder) {
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    /**
     * 'getComparison' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param authUser 인증 사용자 정보
     * @return 클라이언트에 반환할 API 응답
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
