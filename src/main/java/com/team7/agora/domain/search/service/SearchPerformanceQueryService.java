package com.team7.agora.domain.search.service;

import com.team7.agora.domain.search.dto.SearchPerformanceResponse;
import com.team7.agora.domain.search.metric.SearchPerformanceRecorder;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Search Performance Query 愿??鍮꾩쫰?덉뒪 ?좎뒪耳?댁뒪瑜?泥섎━?섎뒗 ?쒕퉬?ㅼ씠??
 */
@Service
public class SearchPerformanceQueryService {

    private final SearchPerformanceRecorder searchPerformanceRecorder;

    /**
     * ?꾩슂???섏〈?깆쓣 二쇱엯諛쏆븘 而댄룷?뚰듃瑜??앹꽦?쒕떎.
     * @param searchPerformanceRecorder 寃???깅뒫 吏?쒕? 湲곕줉?섎뒗 而댄룷?뚰듃
     */
    public SearchPerformanceQueryService(SearchPerformanceRecorder searchPerformanceRecorder) {
        this.searchPerformanceRecorder = searchPerformanceRecorder;
    }

    /**
     * 'getComparison' 硫붿꽌?쒕뒗 ?꾩슂???곗씠?곕? 議고쉶???몄텧??履쎌뿉 諛섑솚?쒕떎.
     * @param authUser ?몄쬆 ?ъ슜???뺣낫
     * @return ?대씪?댁뼵?몄뿉 諛섑솚??API ?묐떟
     */
    public SearchPerformanceResponse getComparison(AdminPrincipal authUser) {
        if (authUser == null || !"ROOT_ADMIN".equals(authUser.getRole().name())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "寃???깅뒫 鍮꾧탳 議고쉶??ROOT_ADMIN留??섑뻾?????덉뒿?덈떎.");
        }
        return SearchPerformanceResponse.of(
            searchPerformanceRecorder.getStats("v1"),
            searchPerformanceRecorder.getStats("v2")
        );
    }
}
