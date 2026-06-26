package com.team7.agora.domain.search.service;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import com.team7.agora.domain.search.repository.PopularKeywordRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 인기 검색어 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
public class PopularKeywordService {

    private static final Logger log = LoggerFactory.getLogger(PopularKeywordService.class);
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PopularKeywordRepository popularKeywordRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param popularKeywordRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public PopularKeywordService(PopularKeywordRepository popularKeywordRepository) {
        this.popularKeywordRepository = popularKeywordRepository;
    }

    public void recordSearchKeyword(Long userId, String keyword) {
        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword.isBlank()) {
            return;
        }
        LocalDate today = LocalDate.now();
        runWithoutRedisFailure("record popular keyword. keyword=" + normalizedKeyword, () -> {
            if (userId != null && !popularKeywordRepository.tryMarkSearched(userId, normalizedKeyword)) {
                return;
            }
            popularKeywordRepository.increment(normalizedKeyword);
            popularKeywordRepository.incrementDaily(normalizedKeyword, dateKeyOf(today));
            popularKeywordRepository.incrementWeekly(normalizedKeyword, weekKeyOf(today));
        });
    }

    /**
     * 'getTopKeywords' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param limit 조회 개수 제한
     * @return 클라이언트에 반환할 API 응답
     */
    public List<PopularKeywordResponse> getTopKeywords(int limit) {
        return readWithoutRedisFailure(
            "read realtime popular keywords",
            () -> popularKeywordRepository.getTopKeywords(limit)
        );
    }

    /**
     * 'getTopDailyKeywords' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param limit 조회 개수 제한
     * @return 클라이언트에 반환할 API 응답
     */
    public List<PopularKeywordResponse> getTopDailyKeywords(int limit) {
        return readWithoutRedisFailure(
            "read daily popular keywords",
            () -> popularKeywordRepository.getTopDailyKeywords(dateKeyOf(LocalDate.now()), limit)
        );
    }

    /**
     * 'getTopWeeklyKeywords' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param limit 조회 개수 제한
     * @return 클라이언트에 반환할 API 응답
     */
    public List<PopularKeywordResponse> getTopWeeklyKeywords(int limit) {
        return readWithoutRedisFailure(
            "read weekly popular keywords",
            () -> popularKeywordRepository.getTopWeeklyKeywords(weekKeyOf(LocalDate.now()), limit)
        );
    }

    private void runWithoutRedisFailure(String action, Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            log.warn("Failed to {}. reason={}", action, e.toString());
        }
    }

    private List<PopularKeywordResponse> readWithoutRedisFailure(
        String action,
        Supplier<List<PopularKeywordResponse>> supplier
    ) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            log.warn("Failed to {}. reason={}", action, e.toString());
            return List.of();
        }
    }

    private String dateKeyOf(LocalDate date) {
        return date.format(DATE_KEY_FORMATTER);
    }

    private String weekKeyOf(LocalDate date) {
        WeekFields weekFields = WeekFields.ISO;
        int weekBasedYear = date.get(weekFields.weekBasedYear());
        int weekOfYear = date.get(weekFields.weekOfWeekBasedYear());
        return String.format("%04d%02d", weekBasedYear, weekOfYear);
    }

    private String normalize(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase();
    }
}
