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

@Service
public class PopularKeywordService {

    private static final Logger log = LoggerFactory.getLogger(PopularKeywordService.class);
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PopularKeywordRepository popularKeywordRepository;

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

    public List<PopularKeywordResponse> getTopKeywords(int limit) {
        return readWithoutRedisFailure(
            "read realtime popular keywords",
            () -> popularKeywordRepository.getTopKeywords(limit)
        );
    }

    public List<PopularKeywordResponse> getTopDailyKeywords(int limit) {
        return readWithoutRedisFailure(
            "read daily popular keywords",
            () -> popularKeywordRepository.getTopDailyKeywords(dateKeyOf(LocalDate.now()), limit)
        );
    }

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
