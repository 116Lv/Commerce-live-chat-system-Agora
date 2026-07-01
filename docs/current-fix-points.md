# Current Fix Points

Last reviewed: 2026-07-01

This document summarizes the current code areas that should be fixed or tightened before the project is treated as production-ready. The list is based on the current `src/main/java` implementation and the current Gradle configuration.

## Priority Summary

| Priority | Area | Why it matters |
| --- | --- | --- |
| ~~P0~~ | ~~Redisson distributed lock migration~~ | ✅ **완료 (2026-07-01)** — Redisson 적용 및 트랜잭션 경계 분리 완료. |
| P0 | Race conditions around trade, payment, likes, and coupons | Several flows use `exists -> save/update` checks that can pass concurrently unless backed by DB constraints, pessimistic locking, or distributed locks. |
| P1 | Payment integration robustness | PortOne response parsing, idempotency, admin verification, and settlement creation need stronger failure handling. |
| P1 | Encoding and response-message cleanup | Several Java files still contain mojibake text in comments or exception messages. Users may receive broken Korean messages. |
| P1 | Production configuration hardening | Redis connection, upload storage, CORS, JWT secret policy, and JPA DDL strategy need production-safe defaults. |
| P2 | Documentation quality | The JavaDoc pass added broad comments, but many comments are generic and should be refined around actual domain behavior. |

## P0: Locking and Concurrency

### 1. ✅ Redisson migration — 완료 (2026-07-01)

- `RedissonConfig.java` + `LockService.java`에 Redisson `RLock` 적용 완료.
- 트랜잭션 경계 분리(`CouponSlotService` / `CouponSlotTransactionExecutor`)도 완료.
- 설계 상세 → [`docs/redisson-distributed-lock-guide.md`](redisson-distributed-lock-guide.md)

### 2. Coupon issuance lock is inside a transactional method

Current file:

- `src/main/java/com/team7/agora/domain/coupon/service/CouponIssueService.java`

Risk:

- `issue()` is annotated with `@Transactional`, and the distributed lock is acquired inside that method.
- If transaction commit happens after the lock is released, a second request may acquire the lock before the first transaction is fully committed.
- This can expose stale reads or duplicate issuance paths under load.

Required action:

- Split lock acquisition and transactional work into separate beans or methods.
- Recommended flow: outer non-transactional method acquires Redisson lock, inner `@Transactional` method performs DB read/update/save, transaction commits before lock is released.

### 3. Coupon issue uniqueness should be enforced at DB level

Current files:

- `src/main/java/com/team7/agora/domain/coupon/entity/CouponIssue.java`
- `src/main/java/com/team7/agora/domain/coupon/repository/JpaCouponIssueDataRepository.java`
- `src/main/java/com/team7/agora/domain/coupon/service/CouponIssueService.java`

Risk:

- Duplicate issuance is prevented by `existsByCouponEventAndUser(...)` before `save(...)`.
- Without a unique constraint on `(coupon_event_id, user_id)`, concurrent requests can still duplicate rows if the lock fails, expires early, or is bypassed by another code path.

Required action:

- Add a unique constraint to `CouponIssue` for event/user.
- Keep the service-level validation for a clean error message, but rely on the DB constraint as the final guard.

### 4. Trade creation can race

Current files:

- `src/main/java/com/team7/agora/domain/trade/service/TradeService.java`
- `src/main/java/com/team7/agora/domain/trade/entity/Trade.java`
- `src/main/java/com/team7/agora/domain/trade/repository/TradeRepository.java`

Risk:

- `startTrade()` and `createTradeFromAcceptedOffer()` both check `existsByProductAndStatusNot(product, CANCELLED)` before creating a trade.
- Two requests for the same product can pass the check before either saves.
- Product status changes to `RESERVED`, but without a row lock or DB uniqueness, duplicate active trades are possible.

Required action:

- Add a product-level lock around trade creation, or use pessimistic locking on the product row.
- Add a DB-level uniqueness strategy for active trade per product if supported by the target DB design.
- Add a concurrency test for duplicate trade start.

### 5. Payment prepare and confirm can race

Current files:

- `src/main/java/com/team7/agora/domain/payment/service/PaymentService.java`
- `src/main/java/com/team7/agora/domain/payment/entity/Payment.java`
- `src/main/java/com/team7/agora/domain/payment/repository/PaymentRepository.java`

Risk:

- `prepare()` checks `paymentRepository.existsByTrade(trade)` before creating a payment.
- `confirm()` can create a `Settlement` after marking payment paid.
- Concurrent calls may create duplicate payment or settlement rows unless constrained.

Required action:

- Add unique constraints for `Payment.trade` and `Settlement.payment`.
- Add idempotent confirm handling based on `paymentKey` and `orderId`.
- Consider a trade/payment lock around prepare and confirm flows.

### 6. Product like count can drift

Current files:

- `src/main/java/com/team7/agora/domain/product/service/ProductLikeService.java`
- `src/main/java/com/team7/agora/domain/product/entity/ProductLike.java`

Current protection:

- `ProductLike` already has a unique constraint on `(product_id, user_id)`.

Remaining risk:

- `product.increaseLikeCount()` and `product.decreaseLikeCount()` update a counter on `Product`.
- Concurrent like/unlike calls can produce stale or drifted counters unless product row updates are locked or the counter is recalculated.

Required action:

- Use optimistic locking (`@Version`) or a DB-level atomic update for `likeCount`.
- Add tests for concurrent like and unlike operations.

## P1: Payment and External API Robustness

### 1. PortOne JSON parsing is manual string parsing

Current file:

- `src/main/java/com/team7/agora/domain/payment/client/PortOnePaymentClient.java`

Risk:

- `extractJsonString(...)` uses `indexOf` and substring parsing.
- Any response shape change, escaped value, error body, or nested field can break parsing.

Required action:

- Use Jackson `ObjectMapper` or Spring `RestClient`/`WebClient` DTO mapping.
- Parse both success and error response bodies explicitly.

### 2. PortOne request body is built with string formatting

Current file:

- `src/main/java/com/team7/agora/domain/payment/client/PortOnePaymentClient.java`

Risk:

- Manual JSON formatting can break if values contain special characters.
- It also makes request schema drift harder to catch.

Required action:

- Introduce request DTOs and serialize through Jackson.

### 3. Admin payment verification can mutate paid state without settlement synchronization

Current file:

- `src/main/java/com/team7/agora/domain/admin/service/AdminPaymentService.java`

Risk:

- `verifyPayment()` calls `payment.markPaid(...)` when PortOne confirms.
- It does not also mark the linked trade as paid or create a settlement in the same way as `PaymentService.confirm(...)`.

Required action:

- Reuse a single domain service path for payment confirmation.
- Ensure paid payment, paid trade, and pending settlement are always updated atomically.

### 4. Webhook validation is too simple for real PortOne signatures

Current files:

- `src/main/java/com/team7/agora/domain/payment/controller/PaymentWebhookController.java`
- `src/main/java/com/team7/agora/domain/payment/service/PaymentWebhookVerifier.java`

Risk:

- Validation currently compares a shared secret header.
- If PortOne requires HMAC signature validation, timestamp tolerance, replay protection, or event IDs, the current check is incomplete.

Required action:

- Confirm PortOne's current webhook signing contract.
- Validate signature, timestamp, and idempotency key/event ID.
- Store processed webhook IDs to avoid duplicate state transitions.

## P1: Authentication and Token Handling

### 1. Refresh token storage is plain UUID

Current files:

- `src/main/java/com/team7/agora/domain/auth/service/AuthService.java`
- `src/main/java/com/team7/agora/domain/auth/entity/RefreshToken.java`

Risk:

- Refresh tokens are stored directly in DB.
- DB exposure would allow token replay until expiration.

Required action:

- Store a hash of the refresh token.
- Compare incoming refresh tokens by hashing them before lookup.
- Add token family or rotation reuse detection if refresh-token theft is in scope.

### 2. Duplicate signup race

Current file:

- `src/main/java/com/team7/agora/domain/auth/service/AuthService.java`

Risk:

- `existsByEmail(...)` before `save(...)` can race.
- This is acceptable only if the `users.email` column has a unique DB constraint and duplicate-key errors are mapped cleanly.

Required action:

- Confirm `User.email` has `unique = true`.
- Add service-level handling for duplicate-key exceptions.

## P1: Configuration and Operations

### 1. Redis connection settings are not explicit

Current file:

- `src/main/resources/application.yaml`

Risk:

- Redis is used for popular keywords and current custom locking, but `spring.data.redis.host/port/password` are not defined in local or prod config.
- Prod may rely on external parameter store, but the expected keys are not documented in this repo.

Required action:

- Document required prod parameter names.
- Add local defaults for Redis or a dev profile that disables Redis-backed features cleanly.
- Add Redisson config after migration.

### 2. `ddl-auto: update` in prod

Current file:

- `src/main/resources/application.yaml`

Risk:

- `spring.jpa.hibernate.ddl-auto: update` can make uncontrolled schema changes in production.

Required action:

- Move production schema changes to Flyway or Liquibase.
- Set production `ddl-auto` to `validate`.

### 3. Local image storage is not production-safe

Current file:

- `src/main/java/com/team7/agora/global/storage/LocalImageStorageClient.java`

Risk:

- Uploads are stored on the application filesystem.
- In multi-instance deployment, files are not shared across instances.
- File type, size, content validation, and path/category validation are thin.

Required action:

- Introduce S3 or object storage for prod.
- Validate MIME type and extension.
- Enforce file size limits.
- Sanitize/whitelist upload categories.

### 4. CORS policy is absent

Current file:

- `src/main/java/com/team7/agora/global/config/SecurityConfig.java`

Risk:

- Browser clients may fail cross-origin requests, or future ad hoc CORS changes may become too permissive.

Required action:

- Add explicit CORS configuration per environment.

## P1: Error Messages and Encoding

Current examples:

- `src/main/java/com/team7/agora/domain/trade/service/TradeService.java`
- `src/main/java/com/team7/agora/domain/payment/service/PaymentService.java`
- `src/main/java/com/team7/agora/domain/product/service/ProductLikeService.java`
- `src/main/java/com/team7/agora/global/storage/LocalImageStorageClient.java`
- `src/main/resources/application.yaml`

Risk:

- Several comments and exception messages are mojibake.
- Some user-facing API responses may contain broken Korean.

Required action:

- Normalize source encoding to UTF-8.
- Re-enter broken Korean messages.
- Add Gradle/compiler/editor settings to enforce UTF-8.
- Add a quick test or script that scans for common mojibake patterns before release.

## P2: Documentation Cleanup

Current state:

- Public JavaDoc was added across main Java files.
- Many generated comments are syntactically valid but too generic, such as "Handles ... behavior."

Required action:

- Refine JavaDoc for controllers, services, and domain entities to explain business rules.
- Remove low-value JavaDoc where it only repeats the method name.
- Add package-level or docs-level architecture notes for domain flows instead of over-commenting simple methods.

## Suggested Next Work Order

1. Migrate `LockService` to Redisson and fix transaction boundary around coupon issuance.
2. Add DB constraints and concurrency tests for coupon issue, trade creation, payment prepare/confirm, and product likes.
3. Consolidate payment confirmation logic and settlement creation.
4. Replace manual PortOne JSON handling with typed DTO parsing.
5. Clean broken Korean messages and enforce UTF-8.
6. Harden prod config: Redis, Redisson, object storage, CORS, and schema migration.
7. Refine JavaDoc to meaningful domain documentation.
