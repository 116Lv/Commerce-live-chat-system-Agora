package com.team7.agora.domain.search.repository;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import java.util.List;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface PopularKeywordRepository {

    boolean tryMarkSearched(Long userId, String keyword);

    void increment(String keyword);

    List<PopularKeywordResponse> getTopKeywords(int limit);

    void incrementDaily(String keyword, String dateKey);

    List<PopularKeywordResponse> getTopDailyKeywords(String dateKey, int limit);

    void incrementWeekly(String keyword, String weekKey);

    List<PopularKeywordResponse> getTopWeeklyKeywords(String weekKey, int limit);
}
