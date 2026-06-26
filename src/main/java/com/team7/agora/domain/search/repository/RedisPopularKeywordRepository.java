package com.team7.agora.domain.search.repository;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

/**
 * Repository adapter that implements persistence operations for Redis popular keyword data.
 */
@Repository
public class RedisPopularKeywordRepository implements PopularKeywordRepository {

    private static final String KEY = "search:popular";
    private static final String DAILY_KEY_PREFIX = "popular:keyword:daily:";
    private static final String WEEKLY_KEY_PREFIX = "popular:keyword:weekly:";
    private static final Duration DAILY_TTL = Duration.ofDays(2);
    private static final Duration WEEKLY_TTL = Duration.ofDays(15);

    private final StringRedisTemplate redisTemplate;

    /**
     * Creates a redis popular keyword repository instance.
     * @param redisTemplate the redis template value
     */
    public RedisPopularKeywordRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Handles increment behavior.
     * @param keyword the keyword value
     */
    @Override
    public void increment(String keyword) {
        redisTemplate.opsForZSet().incrementScore(KEY, keyword, 1);
    }

    /**
     * Returns top keywords data.
     * @param limit the limit value
     * @return the get top keywords result
     */
    @Override
    public List<PopularKeywordResponse> getTopKeywords(int limit) {
        return getTopKeywords(KEY, limit);
    }

    /**
     * Handles increment daily behavior.
     * @param keyword the keyword value
     * @param dateKey the date key value
     */
    @Override
    public void incrementDaily(String keyword, String dateKey) {
        String key = DAILY_KEY_PREFIX + dateKey;
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisTemplate.expire(key, DAILY_TTL);
    }

    /**
     * Returns top daily keywords data.
     * @param dateKey the date key value
     * @param limit the limit value
     * @return the get top daily keywords result
     */
    @Override
    public List<PopularKeywordResponse> getTopDailyKeywords(String dateKey, int limit) {
        return getTopKeywords(DAILY_KEY_PREFIX + dateKey, limit);
    }

    /**
     * Handles increment weekly behavior.
     * @param keyword the keyword value
     * @param weekKey the week key value
     */
    @Override
    public void incrementWeekly(String keyword, String weekKey) {
        String key = WEEKLY_KEY_PREFIX + weekKey;
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisTemplate.expire(key, WEEKLY_TTL);
    }

    /**
     * Returns top weekly keywords data.
     * @param weekKey the week key value
     * @param limit the limit value
     * @return the get top weekly keywords result
     */
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
