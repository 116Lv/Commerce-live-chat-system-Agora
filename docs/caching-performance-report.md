# 검색 API 캐싱 성능 테스트 보고서

> 캐싱 도전과제 산출물. 상품 검색 API v1(캐시 미적용·DB 조회) vs v2(Redis Remote Cache) 성능 비교.

## 테스트 환경

| 항목 | 값 |
| --- | --- |
| DB | MySQL 8.4 (`agora_perf`), 상품 **50,000건** 적재 |
| 캐시 | Redis 7 (Docker), Spring `@Cacheable` Remote Cache, TTL 60s |
| 적재 방식 | JDBC `batchUpdate` (1,000건 단위), 5만건 약 5초 |
| 부하 도구 | k6 v2.0.0 |
| 대상 API | `GET /api/v1/products/search`, `GET /api/v2/products/search` (`regionId=1`, 약 1만건 매칭) |

## 부하 시나리오 (Ramp-up)

vUser를 점진적으로 늘려 포화점을 관찰한다.

| 단계 | 지속 | 목표 vUser |
| --- | --- | --- |
| 1 | 15s | 50 |
| 2 | 15s | 100 |
| 3 | 15s | 200 |
| 4 | 15s | 300 |
| 5 | 10s | 0 (감소) |

> Ramp-up을 쓰는 이유: 부하를 한 번에 주면 워밍업/커넥션 풀 초기화 때문에 초기 구간이 왜곡된다. 점진 증가로 **부하 대비 처리량/지연의 변화 곡선**과 포화점을 본다.

## 결과 (k6 실측)

| 지표 | v1 (캐시 미적용) | v2 (Redis 캐시) | 개선 |
| --- | --- | --- | --- |
| **처리량 TPS** | 558.75 req/s | **975.64 req/s** | **약 1.75배** |
| 평균 응답 | 230.21 ms | **131.55 ms** | 1.75배 빠름 |
| 중앙값(med) | 170.49 ms | **83.39 ms** | 2.0배 빠름 |
| p95 | 678.56 ms | **381.19 ms** | 1.78배 빠름 |
| 최대 | 2.86 s | 4.18 s | — |
| 총 요청 | 39,123 | 68,295 | — |
| 실패율 | 0.00% | 0.00% | — |

## 포화점(Saturation) 해석

- **v1**: vUser가 200→300으로 갈수록 p95가 678ms까지 상승하고 TPS는 약 **560 부근에서 정체**. DB 조회가 병목이 되어 vUser를 늘려도 처리량이 더 늘지 않는다 = 포화 진입.
- **v2**: 같은 부하에서 TPS **약 975까지 상승**, p95 381ms로 v1의 절반 수준. 캐시가 DB 부하를 흡수해 **포화점이 더 높은 부하 쪽으로 이동**.
- 결론: 캐시는 단순히 평균 응답만 줄이는 게 아니라, **같은 인프라로 감당 가능한 동시 사용자 수(처리량 상한)를 끌어올린다.**

## 참고: 단일 요청 기준(반복 100회 평균)

| | v1 | v2 |
| --- | --- | --- |
| 평균 응답 | ~15–37 ms | ~7–9 ms (2–4배) |

부하가 낮을 땐 차이가 작지만(2–4배), **동시 부하가 커질수록 캐시 이득이 커진다**(TPS 1.75배).

## 재현 방법

```bash
# 1) 5만건 적재 (MySQL agora_perf)
PERF_ENABLED=true ./gradlew.bat test --tests "*PerfCachingIndexTest*"

# 2) 앱을 agora_perf로 기동 (ddl-auto=none, 데이터 유지)
#    SPRING_APPLICATION_JSON으로 datasource를 MySQL agora_perf로 오버라이드 후 bootRun

# 3) k6 부하 테스트
k6 run -e TARGET=v1 k6/search_load.js
k6 run -e TARGET=v2 k6/search_load.js
```

## 한계 / 후속

- 본 측정은 단일 머신(앱·MySQL·Redis 동일 호스트) 기준이라 네트워크 비용이 최소화돼 있다. 분리 환경에선 캐시 이득이 더 커질 수 있다.
- 단계별(stage별) TPS 곡선은 k6 web dashboard나 `--out` 시계열 출력으로 더 정밀하게 볼 수 있다.
