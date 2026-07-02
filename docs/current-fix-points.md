# Current Fix Points

Last reviewed: 2026-07-02

This document summarizes the current code areas that should be fixed or tightened before the project is treated as production-ready. The list is based on the current `src/main/java` implementation and the current Gradle configuration.

## Priority Summary

| Priority | Area | Why it matters |
| --- | --- | --- |
| ~~P0~~ | ~~Redisson distributed lock migration~~ | ✅ **완료 (2026-07-01)** — Redisson 적용 및 트랜잭션 경계 분리 완료. |
| ~~P0~~ | ~~Race conditions around trade, payment, likes, and coupons~~ | ✅ **완료 (2026-07-02)** — 비관적 락(`FOR UPDATE`) + DB 유니크 제약 + `DataIntegrityViolationException` 변환, 좋아요 카운트는 원자적 UPDATE로 정리됨. |
| ~~P1~~ | ~~Payment integration robustness~~ | ✅ **완료 (2026-07-02)** — Jackson DTO 파싱, 결제 확정 경로 단일화, HMAC 웹훅 검증까지 반영됨. |
| ~~P1~~ | ~~Encoding cleanup~~ | ✅ **완료 (2026-07-02)** — 확인된 mojibake 4곳 모두 올바른 한국어 메시지로 수정됨. |
| ~~P1~~ | ~~Production configuration hardening~~ | ✅ **완료 (2026-07-02)** — Redis/스토리지/CORS/JWT/DDL 전략 모두 prod 안전 기본값으로 확인됨. |
| P2 | Documentation quality | JavaDoc 일괄 추가 문구가 여전히 대부분 일반적("'X' 메서드가 맡은 기능을 수행하고...")이라 리팩터링 필요. |

## ✅ Resolved: Locking and Concurrency

### 1. Redisson migration — 완료 (2026-07-01)
- `RedissonConfig.java` + `LockService.java`에 Redisson `RLock` 적용 완료.
- 트랜잭션 경계 분리(`CouponSlotService` / `CouponSlotTransactionExecutor`)도 완료.
- 설계 상세 → [`docs/redisson-distributed-lock-guide.md`](redisson-distributed-lock-guide.md)

### 2. 쿠폰 발급 락/트랜잭션 분리 — 완료 (2026-07-02)
- `CouponSlotService.assignSlot(...)`가 락 획득(외부, non-transactional)만 담당하고, 실제 조회/검증/저장은 `CouponSlotTransactionExecutor.assignSlotInTransaction(...)`(내부, `@Transactional`)에 위임하도록 분리되어 있음.
- 커밋 전에 락이 풀리는 문제 없음. (구 `CouponIssueService`/`CouponIssue` 구조는 슬롯 모델로 리팩터링되며 대체됨.)

### 3. 쿠폰 발급 유니크 제약 — 완료 (2026-07-02)
- `Coupon` 엔티티에 `uniqueConstraints`가 적용되어 있어, 락이 실패/만료되는 극단적 상황에서도 DB가 최종 가드 역할을 함.

### 4. 거래 생성 race — 완료 (2026-07-02)
- `TradeService.startTrade` / `createTradeFromAcceptedOffer` 모두 `productRepository.findByIdForUpdateAndDeletedAtIsNull(...)`로 상품 행에 비관적 락을 건 뒤 상태를 재검증.
- `save()`에서 `DataIntegrityViolationException` 발생 시 `CONFLICT`로 변환해 중복 거래 생성을 막음.

### 5. 결제 prepare/confirm race — 완료 (2026-07-02)
- `PaymentService.prepare`는 `findTradeForUpdate`(비관적 락) + 기존 결제 상태(READY/CONFIRMING) 재검증 + `DataIntegrityViolationException` 변환으로 중복 결제 생성을 방지.
- `confirm` 계열은 `Propagation.NOT_SUPPORTED` + 별도 트랜잭션 블록으로 "결제 승인 요청 중(CONFIRMING)" 상태를 명시적으로 관리해 동시 confirm을 직렬화.

### 6. 좋아요 카운트 drift — 완료 (2026-07-02)
- `ProductRepository.increaseLikeCount` / `decreaseLikeCount`가 엔티티 필드 증감이 아니라 `@Modifying` JPQL 원자적 UPDATE로 구현되어 있어 동시 좋아요/취소에도 카운트가 어긋나지 않음.

## ✅ Resolved: Payment and External API Robustness

- **PortOne JSON 파싱**: `PortOnePaymentClient`가 수동 문자열 파싱 대신 Jackson `ObjectMapper` + record 기반 요청 DTO(`TokenRequest` 등)로 직렬화/역직렬화함.
- **관리자 결제 검증 동기화**: `AdminPaymentService.verifyPayment`가 자체 로직 대신 `PaymentService.confirmByPaymentId(...)`를 그대로 호출해, 결제·거래·정산 상태가 한 경로로만 갱신됨.
- **웹훅 검증**: `PaymentWebhookVerifier`가 HMAC-SHA256 서명 검증 + 5분 타임스탬프 허용오차 + `MessageDigest.isEqual` 상수시간 비교로 구현되어 있음(단순 공유 시크릿 비교 아님).

## ✅ Resolved: Authentication and Token Handling

- **Refresh Token 저장**: `RefreshToken` 엔티티는 평문이 아니라 `tokenHash` 컬럼(해시)만 저장.
- **회원가입 중복 race**: `User.email`에 `unique = true` 제약이 있고, `AuthService`가 `DataIntegrityViolationException`을 `DUPLICATE_EMAIL`로 변환 처리함.

## ✅ Resolved: Configuration and Operations

- **prod `ddl-auto`**: `validate`로 설정되어 있고 스키마는 Flyway가 전담(`AGENTS.md`/`CLAUDE.md` §3 규칙과 일치).
- **이미지 스토리지**: `STORAGE_TYPE` 전환으로 로컬은 `LocalImageStorageClient`, 운영은 `S3ImageStorageClient` 사용.
- **CORS**: `CorsConfig`/`CorsProperties`가 `CORS_ALLOWED_ORIGINS` 환경변수 기반으로 명시적 구성되어 있음.
- **Redis 접속 정보**: local/prod/docker 프로파일 모두 `REDIS_HOST`/`REDIS_PORT`/`REDIS_PASSWORD`를 환경변수로 노출(prod는 AWS Parameter Store 연동).

## ✅ Resolved: Error Messages and Encoding

2026-07-02 기준 확인된 mojibake 4곳을 모두 올바른 한국어 메시지로 수정했습니다.

- `src/main/java/com/team7/agora/domain/payment/service/PaymentService.java:285` — `"결제 대기 중인 거래만 결제할 수 있습니다."`
- `src/main/java/com/team7/agora/global/storage/S3ImageStorageClient.java:44` — `"업로드할 이미지가 없습니다."`
- `src/main/java/com/team7/agora/global/storage/S3ImageStorageClient.java:62,70` — `"이미지 저장에 실패했습니다."`
- `src/main/java/com/team7/agora/global/storage/S3ImageStorageClient.java:76` — `"지원하지 않는 업로드 경로입니다."`
- `src/main/java/com/team7/agora/domain/nego/entity/NegoOffer.java:181` — `"응답 가능한 가격 제안 상태가 아닙니다."`

남은 후속 작업(선택):

- 위와 같은 mojibake 패턴을 스캔하는 간단한 스크립트/테스트를 릴리스 전 체크리스트에 추가하는 것을 고려.

## ⚠️ Open: Documentation Cleanup (P2)

- JavaDoc 일괄 추가 문구가 여전히 대부분 제네릭함(예: "'like' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다").
- 컨트롤러/서비스/도메인 엔티티의 JavaDoc을 실제 비즈니스 규칙 설명으로 다듬는 작업 필요.
- 메서드 이름만 반복하는 저가치 JavaDoc은 제거 고려.

## Suggested Next Work Order

1. (선택) Trade/Payment에 대해 `CouponSlotConcurrencyIntegrationTest`와 같은 명시적 동시성 테스트 추가.
2. JavaDoc을 실제 도메인 규칙 설명으로 리팩터링.
