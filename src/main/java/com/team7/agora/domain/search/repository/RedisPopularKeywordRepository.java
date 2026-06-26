package com.team7.agora.domain.search.repository;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

/**
 * Redis 인기 검색어 영속성 작업을 구현하는 저장소 어댑터이다.
 */
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

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param redisTemplate Redis에 검색어 통계를 저장하고 조회하는 템플릿
     */
    public RedisPopularKeywordRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 'tryMarkSearched' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param keyword 검색어
     */
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

    /**
     * 'getTopKeywords' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param limit 조회 개수 제한
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public List<PopularKeywordResponse> getTopKeywords(int limit) {
        return getTopKeywords(KEY, limit);
    }

    /**
     * 'incrementDaily' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param keyword 검색어
     * @param dateKey 일별 Redis 키
     */
    @Override
    public void incrementDaily(String keyword, String dateKey) {
        String key = DAILY_KEY_PREFIX + dateKey;
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisTemplate.expire(key, DAILY_TTL);
    }

    /**
     * 'getTopDailyKeywords' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param dateKey 일별 Redis 키
     * @param limit 조회 개수 제한
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public List<PopularKeywordResponse> getTopDailyKeywords(String dateKey, int limit) {
        return getTopKeywords(DAILY_KEY_PREFIX + dateKey, limit);
    }

    /**
     * 'incrementWeekly' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param keyword 검색어
     * @param weekKey 주별 Redis 키
     */
    @Override
    public void incrementWeekly(String keyword, String weekKey) {
        String key = WEEKLY_KEY_PREFIX + weekKey;
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisTemplate.expire(key, WEEKLY_TTL);
    }

    /**
     * 'getTopWeeklyKeywords' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param weekKey 주별 Redis 키
     * @param limit 조회 개수 제한
     * @return 클라이언트에 반환할 API 응답
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
