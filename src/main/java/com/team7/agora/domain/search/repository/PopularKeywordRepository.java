package com.team7.agora.domain.search.repository;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import java.util.List;

public interface PopularKeywordRepository {

    void increment(String keyword);

    List<PopularKeywordResponse> getTopKeywords(int limit);

    void incrementDaily(String keyword, String dateKey);

    List<PopularKeywordResponse> getTopDailyKeywords(String dateKey, int limit);

    void incrementWeekly(String keyword, String weekKey);

    List<PopularKeywordResponse> getTopWeeklyKeywords(String weekKey, int limit);
}
