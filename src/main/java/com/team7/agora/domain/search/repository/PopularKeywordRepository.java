package com.team7.agora.domain.search.repository;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import java.util.List;

/**
 * 인기 검색어 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
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
