package com.team7.agora.domain.search.repository;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

@Repository
public class RedisPopularKeywordRepository implements PopularKeywordRepository {

    private static final String KEY = "search:popular";
    private static final String DAILY_KEY_PREFIX = "popular:keyword:daily:";
    private static final String WEEKLY_KEY_PREFIX = "popular:keyword:weekly:";
    private static final String DEDUP_KEY_PREFIX = "popular:keyword:dedup:";
    private static final Duration DAILY_TTL = Duration.ofDays(2);
    private static final Duration WEEKLY_TTL = Duration.ofDays(15);
    private static final Duration DEDUP_TTL = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;

    public RedisPopularKeywordRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryMarkSearched(Long userId, String keyword) {
        String key = DEDUP_KEY_PREFIX + userId + ":" + keyword;
        Boolean firstSearch = redisTemplate.opsForValue().setIfAbsent(key, "1", DEDUP_TTL);
        return Boolean.TRUE.equals(firstSearch);
    }

    @Override
    public void increment(String keyword) {
        redisTemplate.opsForZSet().incrementScore(KEY, keyword, 1);
    }

    @Override
    public List<PopularKeywordResponse> getTopKeywords(int limit) {
        return getTopKeywords(KEY, limit);
    }

    @Override
    public void incrementDaily(String keyword, String dateKey) {
        String key = DAILY_KEY_PREFIX + dateKey;
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisTemplate.expire(key, DAILY_TTL);
    }

    @Override
    public List<PopularKeywordResponse> getTopDailyKeywords(String dateKey, int limit) {
        return getTopKeywords(DAILY_KEY_PREFIX + dateKey, limit);
    }

    @Override
    public void incrementWeekly(String keyword, String weekKey) {
        String key = WEEKLY_KEY_PREFIX + weekKey;
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisTemplate.expire(key, WEEKLY_TTL);
    }

    @Override
    public List<PopularKeywordResponse> getTopWeeklyKeywords(String weekKey, int limit) {
        return getTopKeywords(WEEKLY_KEY_PREFIX + weekKey, limit);
    }

    private List<PopularKeywordResponse> getTopKeywords(String key, int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .reverseRangeWithScores(key, 0, Math.max(0, limit - 1));
        if (tuples == null) {
            return List.of();
        }
        return tuples.stream()
            .map(tuple -> new PopularKeywordResponse(
                tuple.getValue(),
                tuple.getScore() == null ? 0 : tuple.getScore().longValue()
            ))
            .toList();
    }
}
