// 발표용 성능 측정: MySQL agora_perf에 5만건 적재 → 인덱스 EXPLAIN before/after → 캐싱 v1/v2 비교
package com.team7.agora.domain.search;

import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 캐싱·인덱스 도전과제 성능 측정 (로컬 MySQL + Redis 필요).
 * 실행: PERF_ENABLED=true ./gradlew.bat test --tests "*PerfCachingIndexTest*"
 * CI에서는 PERF_ENABLED 미설정이라 자동 skip.
 */
@EnabledIfEnvironmentVariable(named = "PERF_ENABLED", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/agora_perf?createDatabaseIfNotExist=true&serverTimezone=UTC&characterEncoding=UTF-8&rewriteBatchedStatements=true",
    "spring.datasource.username=root",
    "spring.datasource.password=12345678",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
    "spring.jpa.show-sql=false",
    "spring.flyway.enabled=false",
    "spring.sql.init.mode=never",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "agora.redisson.enabled=false",
    "agora.chat.redis-listener.enabled=false"
})
class PerfCachingIndexTest {

    private static final int TOTAL = 50_000;
    private static final int BATCH = 1_000;
    private static final int REGION_COUNT = 5;
    private static final String INDEX = "idx_products_category_status_deleted"; // category엔 FK가 없어 drop 가능
    private static final String[] CATEGORIES = {"디지털기기", "가구/인테리어", "스포츠/레저", "생활/식품", "의류"};

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ProductSearchService productSearchService;

    private static Long targetRegionId;

    @Autowired
    PerfCachingIndexTest(JdbcTemplate jdbc, UserRepository userRepository,
                         RegionRepository regionRepository, ProductSearchService productSearchService) {
        this.jdbc = jdbc;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.productSearchService = productSearchService;
    }

    @Test
    @Order(1)
    void step1_seed50k() {
        User seller = userRepository.save(User.signup(
            "perf-seller@test.com", "encoded", "perfSeller", "01000000000"));
        List<Long> regionIds = new ArrayList<>();
        for (int i = 0; i < REGION_COUNT; i++) {
            Region region = regionRepository.save(Region.create(
                "perf region " + i, "PERF" + i, "Seoul", "Gu" + i, "Dong" + i));
            regionIds.add(region.getId());
        }
        targetRegionId = regionIds.get(0);
        Long sellerId = seller.getId();

        String sql = "INSERT INTO products "
            + "(seller_id, region_id, title, description, price, category, status, view_count, like_count) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        long start = System.currentTimeMillis();
        int inserted = 0;
        for (int offset = 0; offset < TOTAL; offset += BATCH) {
            final int base = offset;
            final int size = Math.min(BATCH, TOTAL - offset);
            jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int n = base + i;
                    ps.setLong(1, sellerId);
                    ps.setLong(2, regionIds.get(n % REGION_COUNT));
                    ps.setString(3, "성능테스트 상품 " + n + " 맥북 아이폰 노트북");
                    ps.setString(4, "성능 측정용 더미 상품 설명 " + n);
                    ps.setBigDecimal(5, java.math.BigDecimal.valueOf(10000 + (n % 900000)));
                    ps.setString(6, CATEGORIES[n % CATEGORIES.length]);
                    ps.setString(7, ProductStatus.SELLING.name());
                    ps.setInt(8, n % 1000);
                    ps.setInt(9, n % 50);
                }

                @Override
                public int getBatchSize() {
                    return size;
                }
            });
            inserted += size;
        }
        long elapsed = System.currentTimeMillis() - start;
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        System.out.println();
        System.out.println("===== [SEED] MySQL agora_perf =====");
        System.out.println("  JDBC batchUpdate(" + BATCH + "건 단위) 적재: " + inserted + "건");
        System.out.println("  적재 시간          : " + elapsed + " ms");
        System.out.println("  products 총 row수   : " + count);
        System.out.println("  타깃 region_id      : " + targetRegionId + " (약 " + (TOTAL / REGION_COUNT) + "건)");
        System.out.println("===================================");
    }

    @Test
    @Order(2)
    void step2_indexExplainBeforeAfter() {
        String category = CATEGORIES[0];
        // 실제 검색 필터를 모사. ORDER BY/LIMIT는 PK 역스캔 최적화를 피하려 제외.
        String query = "SELECT id FROM products "
            + "WHERE category = '" + category + "' AND status = 'SELLING' AND deleted_at IS NULL";

        // BEFORE: 이 쿼리를 탈 수 있는 인덱스를 모두 제거 → 풀스캔 유도
        dropIndexQuietly(INDEX);
        dropIndexQuietly("idx_products_status_deleted");
        jdbc.execute("ANALYZE TABLE products"); // 통계 갱신 → EXPLAIN rows 현실화
        Map<String, Object> before = explain(query);

        // AFTER: category 복합 인덱스만 생성
        jdbc.execute("CREATE INDEX " + INDEX + " ON products (category, status, deleted_at)");
        jdbc.execute("ANALYZE TABLE products");
        Map<String, Object> after = explain(query);

        System.out.println();
        System.out.println("===== [INDEX] EXPLAIN before / after (category+status+deleted_at) =====");
        System.out.println("  BEFORE (인덱스 없음) : type=" + before.get("type")
            + " | key=" + before.get("key") + " | rows=" + before.get("rows"));
        System.out.println("  AFTER  (복합 인덱스)  : type=" + after.get("type")
            + " | key=" + after.get("key") + " | rows=" + after.get("rows"));
        System.out.println("======================================================================");
    }

    @Test
    @Order(3)
    void step3_cachingV1V2() {
        ProductSearchCondition cond = new ProductSearchCondition(
            null, targetRegionId, null, PageRequest.of(0, 20));

        for (int i = 0; i < 5; i++) {
            productSearchService.searchV1(cond);
            productSearchService.searchV2(cond);
        }

        int iterations = 100;
        long v1 = measure(iterations, () -> productSearchService.searchV1(cond));
        long v2 = measure(iterations, () -> productSearchService.searchV2(cond));

        double v1Avg = v1 / 1_000_000.0 / iterations;
        double v2Avg = v2 / 1_000_000.0 / iterations;

        System.out.println();
        System.out.println("===== [CACHE] v1(DB) vs v2(Redis 캐시) 평균 응답시간 =====");
        System.out.printf("  v1 (캐시 미적용·DB 조회) : %.3f ms/req%n", v1Avg);
        System.out.printf("  v2 (Redis 캐시 적용)     : %.3f ms/req%n", v2Avg);
        System.out.printf("  개선 배수                : %.1fx 빠름%n", v1Avg / Math.max(v2Avg, 0.0001));
        System.out.println("  (반복 " + iterations + "회 · 동일 조건, v2는 첫 1회 미스 후 캐시 적중)");
        System.out.println("=========================================================");
    }

    private void dropIndexQuietly(String index) {
        try {
            jdbc.execute("ALTER TABLE products DROP INDEX " + index);
        } catch (Exception ignored) {
            // 인덱스가 없거나 FK가 필요로 하면 무시
        }
    }

    private Map<String, Object> explain(String query) {
        List<Map<String, Object>> rows = jdbc.queryForList("EXPLAIN " + query);
        return rows.get(0);
    }

    private long measure(int iterations, Runnable action) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            action.run();
        }
        return System.nanoTime() - start;
    }
}
