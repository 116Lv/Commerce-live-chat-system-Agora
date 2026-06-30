# 쿠폰 도메인 리팩터링 가이드

Last reviewed: 2026-06-26

이 문서는 **지금 쿠폰 코드가 어떻게 동작하는지**, **왜 바꿔야 하는지**, **어떤 파일을 어떻게 고치면 되는지**를 팀 전체가 같은 그림으로 이해할 수 있게 정리한 가이드입니다.

> 읽는 사람이 쿠폰 코드를 처음 본다고 가정하고, 최대한 쉬운 말로 썼습니다.

---

## 목차

1. [한 줄 요약](#1-한-줄-요약)
2. [Part A — 정리: 기존 방식 vs 바뀔 방식](#part-a--정리-기존-방식-vs-바뀔-방식)
3. [Part B — 수정 가이드: 파일별 변경 안내](#part-b--수정-가이드-파일별-변경-안내)
4. [Part C — Redisson 분산 락 적용 방법](#part-c--redisson-분산-락-적용-방법)
5. [Part D — 작업 순서 체크리스트](#part-d--작업-순서-체크리스트)

---

## 1. 한 줄 요약

| | 기존 | 바뀔 방식 |
|---|---|---|
| 규칙(할인금액, 기간 등) | `Coupon` 테이블 | **`CouponEvent` 테이블** |
| 유저가 실제로 가진 쿠폰 1장 | `CouponIssue` 테이블 | **`Coupon` 테이블 (슬롯)** |
| 중간 테이블 | `Coupon` + `CouponEvent` + `CouponIssue` (3개) | **`CouponEvent` + `Coupon` (2개)** |
| 발급 방식 | 발급할 때마다 Issue row 생성 | **이벤트 생성 시 슬롯 N개 미리 INSERT → user_id 채우기** |
| 동시성 | Redisson `LockService` (이미 적용됨) | **같은 방식 유지, 락 키는 `lock:coupon-event:{eventId}`** |

---

# Part A — 정리: 기존 방식 vs 바뀔 방식

## A-1. 비유로 이해하기

쿠폰을 **「행사 키트」** 에 비유해 봅니다.

### 기존 방식 (지금 코드)

```text
Coupon        = 할인 규칙 설명서 (「5,000원 할인, 3만원 이상 주문」)
CouponEvent   = 행사 포스터 (이름, 기간, 몇 장 남았는지만 적혀 있음)
CouponIssue   = 실제로 손에 쥔 티켓 1장 (누구 것인지, 언제 받았는지)
```

문제는 **설명서(Coupon)와 포스터(CouponEvent)가 서로 연결되어 있지 않다**는 것입니다.

- 유저가 이벤트 페이지에서 「받기」를 누르면, 코드는 포스터 ID만 보고 **아무 FIRST_COME 설명서나** 집어 옵니다 (`findFirstComeCoupon()`).
- 관리자가 개별 발급하면, 그때 **포스터를 즉석에서 새로 인쇄**하고 티켓을 줍니다.
- 그래서 「선착순 이벤트로 받은 쿠폰」과 「관리자가 준 쿠폰」이 **같은 규칙인지, 다른 이벤트인지 구분이 어렵습니다.**

### 바뀔 방식 (목표)

```text
CouponEvent   = 행사 키트 전체 (종류, 제목, 기간, 수량, 할인 규칙 전부)
Coupon        = 미리 만들어 둔 빈 티켓 1장 (user_id가 비어 있음 → 발급 시 채움)
```

- 행사를 만들 때 **티켓을 수량만큼 미리 깔아 둡니다.**
- 유저가 참여하거나 관리자가 지정 발급하면 **빈 티켓 하나에 user_id + 발급일 + 만료일**을 채웁니다.
- 행사가 끝나면 **그 이벤트 티켓들을 통째로 정리(삭제)** 합니다.

`CouponIssue` 테이블은 **더 이상 필요 없습니다.** `Coupon` 한 줄이 「티켓 1장」 역할을 합니다.

---

## A-2. 기존 방식 — 테이블 역할

### `coupons` (Coupon 엔티티)

**역할:** 할인 **정책(템플릿)**. 유저 정보 없음.

| 필드 | 의미 |
|---|---|
| name | 쿠폰 이름 |
| discountAmount | 할인 금액 |
| minOrderAmount | 최소 주문 금액 |
| type | FIRST_COME, SMILE_REWARD |
| validDays | 유효 일수 |
| status | ACTIVE 등 |

### `coupon_events` (CouponEvent 엔티티)

**역할:** 이벤트 **캠페인 메타데이터만**. 할인 규칙 없음, Coupon과 FK 없음.

| 필드 | 의미 |
|---|---|
| name | 이벤트 이름 |
| totalQuantity | 전체 수량 |
| issuedQuantity | 발급된 수량 |
| startAt, endAt | 진행 기간 |
| status | ACTIVE, ENDED |

### `coupon_issues` (CouponIssue 엔티티)

**역할:** 「누가 실제로 쿠폰을 받았는지」 기록.

| 필드 | 의미 |
|---|---|
| coupon_id | 어떤 정책인지 |
| coupon_event_id | 어떤 이벤트 경로인지 |
| user_id | 누구 것인지 |
| status | ISSUED, USED, EXPIRED |
| issuedAt | 발급 시각 |

---

## A-3. 기존 방식 — API 흐름

### 흐름 1: 유저 선착순 이벤트 참여

```text
POST /api/coupon-events/{eventId}/issue
  → CouponIssueService.issue(userId, eventId)
  → Redisson 락: lock:coupon-event:{eventId}
  → CouponIssueTransactionExecutor.issue()
       1. CouponEvent 조회 + 기간/수량 검증
       2. findFirstComeCoupon() ← ⚠️ 이벤트와 무관한 쿠폰 정책 선택
       3. CouponIssue INSERT
```

### 흐름 2: 관리자 쿠폰 정책 생성

```text
POST /api/admin/coupons
  → Coupon(정책) INSERT만 함
  → CouponEvent는 만들지 않음
```

### 흐름 3: 관리자 개별/전체 발급

```text
POST /api/admin/coupons/{couponId}/issue
POST /api/admin/coupons/{couponId}/broadcast
  → AdminCouponService
       1. Coupon(정책) 조회
       2. ⚠️ 그 자리에서 CouponEvent 새로 생성 ("OOO 지정 발급")
       3. CouponIssue INSERT
  → Redisson 락: lock:admin-coupon:{couponId}
```

### 흐름 4: 내 쿠폰 조회

```text
GET /api/users/me/coupons
  → CouponIssueRepository.findAllByUser()
```

---

## A-4. 기존 방식의 문제점 (왜 바꾸나)

| # | 문제 | 왜 심각한가 |
|---|---|---|
| 1 | CouponEvent에 할인 규칙이 없음 | 이벤트마다 다른 할인을 줄 수 없음 |
| 2 | `findFirstComeCoupon()` | 이벤트 A인데 쿠폰 B가 나갈 수 있음 |
| 3 | 관리자 발급 시 Event 즉석 생성 | 공개 이벤트 목록(`GET /api/coupon-events`)에 섞임 |
| 4 | Coupon / CouponIssue / CouponEvent 역할 중복 | 「정책」「캠페인」「소유」가 팀마다 다르게 이해됨 |
| 5 | 테이블 3개 | 조회·정리·만료 배치가 복잡 |

---

## A-5. 바뀔 방식 — 테이블 역할

### `coupon_events` (규칙 + 캠페인의 중심)

| 필드 | 의미 |
|---|---|
| **type** | FIRST_COME, NEW_SIGNUP, ADMIN_INDIVIDUAL |
| name | 이벤트 페이지 제목 |
| startAt, endAt | 진행 기간 |
| totalQuantity | 발행할 쿠폰 장수 |
| issuedQuantity | 이미 user_id가 채워진 장수 |
| **discountAmount** | 할인 금액 |
| **minOrderAmount** | 최소 주문 금액 |
| **validDays** | 발급일 + N일 = 만료일 |
| status | ACTIVE, ENDED, ARCHIVED |

### `coupons` (슬롯 = 쿠폰 1장)

| 필드 | 의미 |
|---|---|
| coupon_event_id | 어느 이벤트 소속인지 |
| user_id | **NULL = 아직 안 준 빈 슬롯** |
| issued_at | 발급 시각 (NULL until assigned) |
| expires_at | 만료 시각 (issued_at + event.validDays) |
| status | AVAILABLE, ISSUED, USED, EXPIRED |

> **`coupon_issues` 테이블 삭제.** Issue가 하던 일은 `coupons` row가 합니다.

---

## A-6. 바뀔 방식 — API 흐름

### 흐름 1: 관리자 이벤트 생성 (새 API)

```text
POST /api/admin/coupon-events
  body: { type, name, startAt, endAt, totalQuantity,
          discountAmount, minOrderAmount, validDays }

  1. CouponEvent INSERT
  2. Coupon 슬롯 totalQuantity개 bulk INSERT
     (user_id=null, status=AVAILABLE)
```

### 흐름 2: 유저 선착순 참여

```text
POST /api/coupon-events/{eventId}/issue

  Redisson 락: lock:coupon-event:{eventId}
  1. CouponEvent.validateIssueable()
  2. 빈 슬롯 1개 SELECT ... FOR UPDATE (user_id IS NULL)
  3. 슬롯 UPDATE: user_id, issued_at, expires_at, status=ISSUED
  4. event.issuedQuantity++
```

### 흐름 3: 관리자 개별 발급

```text
POST /api/admin/coupon-events/{eventId}/issue
  body: { userIds: [1, 2, 3] }

  Redisson 락: lock:coupon-event:{eventId}
  → 유저 참여와 **완전히 같은 슬롯 채우기 로직**
  (type=ADMIN_INDIVIDUAL 이벤트는 공개 목록에서 제외)
```

### 흐름 4: 내 쿠폰 조회

```text
GET /api/users/me/coupons
  → CouponRepository.findAllByUserIdAndStatusIn(ISSUED, ...)
  → 할인 정보는 JOIN coupon_events에서 가져옴
```

### 흐름 5: 만료 정리 (배치, 새로 추가)

```text
@Scheduled 또는 Admin 배치
  1. endAt 지난 CouponEvent → status ENDED
  2. 해당 event_id의 Coupon 중
     - user_id IS NULL (미발급 슬롯) → DELETE
     - expires_at < now AND status != USED → DELETE 또는 EXPIRED 처리
```

---

## A-7. Before / After 그림

```text
[ BEFORE — 3테이블 ]

  Coupon(정책)          CouponEvent(기간/수량)
       \                    /
        \                  /
         → CouponIssue ← User


[ AFTER — 2테이블 ]

  CouponEvent(규칙+캠페인)
       |
       | 1:N
       v
  Coupon(슬롯 1장 = user_id 있으면 「내 쿠폰」)
```

---

# Part B — 수정 가이드: 파일별 변경 안내

아래는 **궁극적으로 바꿀 최종 모습**입니다. 한 번에 다 하지 말고 [Part D 체크리스트](#part-d--작업-순서-체크리스트) 순서대로 진행하세요.

---

## B-1. 삭제할 파일

이 파일들은 **`CouponIssue` 개념이 사라지면** 더 이상 필요 없습니다.

| 파일 | 왜 삭제하나 |
|---|---|
| `entity/CouponIssue.java` | `Coupon` 슬롯이 그 역할을 대신함 |
| `enums/CouponIssueStatus.java` | `CouponStatus`로 통합 |
| `repository/CouponIssueRepository.java` | |
| `repository/CouponIssueRepositoryAdapter.java` | |
| `repository/JpaCouponIssueDataRepository.java` | |
| `dto/response/CouponIssueHistoryResponse.java` | 이벤트 기준 발급 이력 DTO로 교체 |
| `test/.../InMemoryCouponIssueRepository.java` | |
| `test/.../CouponIssueMappingTest.java` | |
| `test/.../CouponIssueTransactionExecutorTest.java` | 새 Executor 테스트로 교체 |

---

## B-2. 새로 만들 파일

### `enums/CouponEventType.java`

**왜 필요한가:** 이벤트 종류(선착순 / 신규가입 / 관리자 개별)에 따라 API 노출·발급 주체가 달라집니다.

```java
public enum CouponEventType {
    FIRST_COME,        // 유저가 이벤트 페이지에서 직접 받기
    NEW_SIGNUP,        // 가입 시 시스템이 자동 발급
    ADMIN_INDIVIDUAL   // 관리자만 user_id 지정 발급, 공개 목록 제외
}
```

---

### `enums/CouponStatus.java` (기존 파일 **대폭 수정**)

**왜 바꾸나:** 예전 `CouponStatus`는 「정책 ACTIVE/INACTIVE」였는데, 이제 「슬롯 1장의 상태」입니다.

```java
public enum CouponStatus {
    AVAILABLE,  // 빈 슬롯 (user_id == null)
    ISSUED,     // 발급됨, 사용 가능
    USED,       // 결제 등에서 사용 완료
    EXPIRED     // 만료
}
```

---

### `dto/request/AdminCouponEventCreateRequest.java`

**왜 필요한가:** 관리자가 **이벤트 + 슬롯 N개**를 한 번에 만듭니다. 예전 `AdminCouponCreateRequest`를 대체합니다.

```java
public record AdminCouponEventCreateRequest(
    @NotNull CouponEventType type,
    @NotBlank String name,
    @NotNull LocalDateTime startAt,
    @NotNull LocalDateTime endAt,
    @Min(1) int totalQuantity,
    @Min(0) int discountAmount,
    @Min(0) int minOrderAmount,
    @Min(1) int validDays
) {}
```

---

### `dto/request/AdminCouponEventIssueRequest.java`

**왜 필요한가:** 관리자 개별 발급 시 `eventId` + `userIds`만 보냅니다.

```java
public record AdminCouponEventIssueRequest(
    @NotEmpty List<Long> userIds
) {}
```

---

### `dto/response/AdminCouponEventResponse.java`

**왜 필요한가:** 이벤트 생성/상세 조회 응답. 할인 규칙 필드 포함.

```java
public record AdminCouponEventResponse(
    Long eventId,
    String type,
    String name,
    LocalDateTime startAt,
    LocalDateTime endAt,
    int totalQuantity,
    int issuedQuantity,
    int discountAmount,
    int minOrderAmount,
    int validDays,
    String status
) {
    public static AdminCouponEventResponse from(CouponEvent event) { ... }
}
```

---

### `dto/response/CouponEventIssueResponse.java`

**왜 필요한가:** 발급 API 결과 (몇 장 발급, 몇 명 스킵).

```java
public record CouponEventIssueResponse(
    Long eventId,
    int issuedCount,
    int skippedCount
) {}
```

---

### `controller/AdminCouponEventController.java`

**왜 필요한가:** 쿠폰 **정책** API(`/api/admin/coupons`) 대신 **이벤트** API로 통합.

```java
@RestController
@RequestMapping("/api/admin/coupon-events")
public class AdminCouponEventController {

    // POST   /              → 이벤트 생성 + 슬롯 bulk INSERT
    // GET    /              → 이벤트 목록
    // GET    /{eventId}     → 이벤트 상세
    // POST   /{eventId}/issue → 관리자 개별 발급 (ADMIN_INDIVIDUAL 타입)
    // GET    /{eventId}/coupons → 발급 현황 (선택)
}
```

---

### `service/CouponSlotService.java`

**왜 필요한가:** 「빈 슬롯 찾기 → user_id 채우기」 로직을 **한 곳**에 모읍니다. 유저 참여와 관리자 발급이 **같은 코드**를 씁니다.

```java
@Service
public class CouponSlotService {

    private final LockService lockService;
    private final CouponSlotTransactionExecutor executor;

    /** 유저 선착순 / 관리자 개별 발급 공통 진입점 */
    public CouponEventIssueResponse assignSlot(Long eventId, Long userId) {
        return lockService.withLock(
            "lock:coupon-event:" + eventId,
            () -> executor.assignSlotInTransaction(eventId, userId)
        );
    }

    /** 관리자: 여러 userId 일괄 발급 */
    public CouponEventIssueResponse assignSlots(Long eventId, List<Long> userIds) {
        return lockService.withLock(
            "lock:coupon-event:" + eventId,
            () -> executor.assignSlotsInTransaction(eventId, userIds)
        );
    }
}
```

---

### `service/CouponSlotTransactionExecutor.java`

**왜 필요한가:** Redisson 락 **안에서** DB 트랜잭션을 실행하는 전용 빈. [Part C](#part-c--redisson-분산-락-적용-방법) 참고.

```java
@Component
public class CouponSlotTransactionExecutor {

    @Transactional
    public CouponEventIssueResponse assignSlotInTransaction(Long eventId, Long userId) {
        // 1. event 조회 + validateIssueable
        // 2. 이미 이 event에 발급받았는지 확인 (event_id + user_id unique)
        // 3. 빈 슬롯 1개 FOR UPDATE
        // 4. slot.assign(user) → user_id, issuedAt, expiresAt, status=ISSUED
        // 5. event.issue() → issuedQuantity++
        // 6. save
    }
}
```

---

### `service/CouponCleanupService.java` (선택, 권장)

**왜 필요한가:** 말씀하신 「기간 지나면 치운다」 운영을 코드로 자동화.

```java
@Service
public class CouponCleanupService {

    /** endAt 지난 이벤트의 미발급 슬롯 + 만료 쿠폰 DELETE */
    @Transactional
    public int cleanupExpiredEvents() { ... }
}
```

---

## B-3. 수정할 파일

### `entity/CouponEvent.java`

**지금:** 이름, 수량, 기간만 있음.  
**바꿀 것:** 할인 규칙 + 이벤트 타입 추가. **슬롯 bulk 생성** 메서드 추가.

```java
@Entity
@Table(name = "coupon_events")
public class CouponEvent extends BaseTimeEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponEventType type;          // ← 추가

    private int totalQuantity;
    private int issuedQuantity;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private int discountAmount;            // ← 추가
    private int minOrderAmount;            // ← 추가
    private int validDays;                 // ← 추가

    @Enumerated(EnumType.STRING)
    private CouponEventStatus status;

    /** 이벤트 생성 팩토리 — 규칙 전부 여기서 받음 */
    public static CouponEvent create(
        CouponEventType type,
        String name,
        int totalQuantity,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int discountAmount,
        int minOrderAmount,
        int validDays
    ) { ... }

    public void issue() { ... }  // issuedQuantity++, 수량 검증
    public void validateIssueable(LocalDateTime now) { ... }
}
```

**왜 이렇게:** 규칙의 「단일 진실 원천」이 Event가 됩니다.

---

### `entity/Coupon.java`

**지금:** 할인 정책 템플릿 (user_id 없음).  
**바꿀 것:** **슬롯 1장**.

```java
@Entity
@Table(
    name = "coupons",
    uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_event_id", "user_id"})
    // user_id가 NULL인 row는 DB마다 unique 처리가 다를 수 있음 → 애플리케이션에서 중복 발급 방지 + 락
)
public class Coupon {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")          // nullable — 빈 슬롯
    private User user;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;           // AVAILABLE, ISSUED, USED, EXPIRED

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    /** 이벤트 생성 시 미리 깔아 두는 빈 슬롯 */
    public static Coupon createAvailableSlot(CouponEvent event) {
        Coupon slot = new Coupon();
        slot.couponEvent = event;
        slot.status = CouponStatus.AVAILABLE;
        return slot;
    }

    /** 발급: user_id + 날짜 채우기 */
    public void assign(User user, LocalDateTime now, int validDays) {
        if (this.user != null) {
            throw new BusinessException(CONFLICT, "이미 발급된 슬롯입니다.");
        }
        this.user = user;
        this.issuedAt = now;
        this.expiresAt = now.plusDays(validDays);
        this.status = CouponStatus.ISSUED;
    }
}
```

**왜 이렇게:** 「미리 INSERT → 나중에 user_id 채우기」 모델 그대로 코드에 반영.

---

### `repository/CouponRepository.java`

**지금:** `findFirstComeCoupon()`, `findAll()` (정책 조회).  
**바꿀 것:**

```java
public interface CouponRepository {

    Coupon save(Coupon coupon);
    List<Coupon> saveAll(List<Coupon> coupons);

    /** 선착순: 빈 슬롯 1개 (FOR UPDATE는 JPA @Lock 또는 Query) */
    Optional<Coupon> findFirstAvailableSlotForUpdate(Long eventId);

    /** 내 쿠폰 */
    List<Coupon> findAllByUserIdAndStatusIn(Long userId, List<CouponStatus> statuses);

    /** 이벤트 발급 현황 */
    List<Coupon> findAllByCouponEventIdAndUserIsNotNull(Long eventId);

    /** 중복 발급 확인 */
    boolean existsByCouponEventIdAndUserId(Long eventId, Long userId);

    /** 만료 정리 */
    void deleteByCouponEventIdAndUserIsNull(Long eventId);
    void deleteByCouponEventIdAndExpiresAtBefore(Long eventId, LocalDateTime cutoff);
}
```

---

### `repository/JpaCouponDataRepository.java`

```java
interface JpaCouponDataRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.couponEvent.id = :eventId AND c.user IS NULL ORDER BY c.id ASC")
    Optional<Coupon> findFirstAvailableSlotForUpdate(@Param("eventId") Long eventId);

    List<Coupon> findAllByUserIdAndStatusIn(Long userId, Collection<CouponStatus> statuses);

    boolean existsByCouponEventIdAndUserId(Long eventId, Long userId);
}
```

**왜 FOR UPDATE:** 선착순 100명이 동시에 눌러도 **슬롯 1개만** 가져가게.

---

### `service/AdminCouponService.java` → **`AdminCouponEventService.java`로 교체**

**지금 하는 일:** Coupon 정책 CRUD + 발급 시 Event 즉석 생성.  
**바꿀 일:**

```java
@Service
public class AdminCouponEventService {

    /** 이벤트 + 슬롯 N개 생성 */
    @Transactional
    public AdminCouponEventResponse createEvent(...) {
        CouponEvent event = couponEventRepository.save(CouponEvent.create(...));
        List<Coupon> slots = IntStream.range(0, totalQuantity)
            .mapToObj(i -> Coupon.createAvailableSlot(event))
            .toList();
        couponRepository.saveAll(slots);   // batch insert
        return AdminCouponEventResponse.from(event);
    }

    /** ADMIN_INDIVIDUAL 타입 이벤트에 userIds 발급 */
    public CouponEventIssueResponse issueToUsers(Long eventId, List<Long> userIds) {
        // CouponSlotService.assignSlots() 호출
    }
}
```

**삭제할 메서드:** `create()`, `broadcast()`, `issueTargets()` (즉석 Event 생성 로직).

---

### `service/CouponIssueService.java` → **`CouponParticipationService.java`로 교체**

**지금:** `issue(userId, eventId)` → Issue row 생성.  
**바꿀 것:**

```java
@Service
public class CouponParticipationService {

    public void participate(Long userId, Long eventId) {
        // FIRST_COME / NEW_SIGNUP 타입만 허용
        CouponEvent event = findEvent(eventId);
        if (event.getType() == ADMIN_INDIVIDUAL) {
            throw forbidden("관리자 전용 이벤트입니다.");
        }
        couponSlotService.assignSlot(eventId, userId);
    }
}
```

---

### `service/CouponQueryService.java`

**바꿀 것:**

```java
public List<CouponEventResponse> listPublicEvents() {
    // ACTIVE 이면서 type != ADMIN_INDIVIDUAL 만
}

public List<MyCouponResponse> getMyCoupons(Long userId) {
    return couponRepository.findAllByUserIdAndStatusIn(userId, List.of(ISSUED))
        .stream()
        .map(MyCouponResponse::from)  // event JOIN해서 할인 정보 포함
        .toList();
}
```

---

### `controller/CouponEventController.java`

**바꿀 것:**

```java
@GetMapping
public ApiResponse<List<CouponEventResponse>> list() {
    return success(couponQueryService.listPublicEvents());  // ADMIN_INDIVIDUAL 제외
}

@PostMapping("/{eventId}/issue")
public ResponseEntity<ApiResponse<Void>> issue(...) {
    couponParticipationService.participate(userId, eventId);  // 이름만 변경
}
```

---

### `controller/AdminCouponController.java`

**→ 삭제하거나 Deprecated.**  
`/api/admin/coupons` 전체를 `/api/admin/coupon-events`로 이전.

---

### `dto/response/MyCouponResponse.java`

**바꿀 것:** `issueId` → `couponId`(슬롯 id), 할인 정보는 event에서.

```java
public record MyCouponResponse(
    Long couponId,          // 슬롯 ID (결제 시 이 ID 참조)
    Long eventId,
    String eventName,
    int discountAmount,
    int minOrderAmount,
    String status,
    LocalDateTime issuedAt,
    LocalDateTime expiresAt
) {
    public static MyCouponResponse from(Coupon coupon) {
        CouponEvent event = coupon.getCouponEvent();
        return new MyCouponResponse(
            coupon.getId(),
            event.getId(),
            event.getName(),
            event.getDiscountAmount(),
            event.getMinOrderAmount(),
            coupon.getStatus().name(),
            coupon.getIssuedAt(),
            coupon.getExpiresAt()
        );
    }
}
```

---

### `dto/response/CouponEventResponse.java`

**추가 필드:** type, discountAmount, minOrderAmount, validDays (이벤트 페이지 표시용).

---

### `enums/CouponType.java`

**→ 삭제 가능.** `CouponEventType`으로 대체.

---

### `resources/data.sql`

**바꿀 seed 예시:**

```sql
-- coupon_events: 규칙 + 캠페인
INSERT INTO coupon_events (id, type, name, total_quantity, issued_quantity,
    start_at, end_at, discount_amount, min_order_amount, valid_days, status, created_at)
VALUES
    (1, 'FIRST_COME', '동네 첫 거래 선착순 쿠폰', 100, 3,
     '2026-01-01', '2026-12-31', 5000, 10000, 30, 'ACTIVE', ...),
    (2, 'ADMIN_INDIVIDUAL', 'VIP 개별 발급', 10, 0,
     '2026-01-01', '2026-12-31', 7000, 30000, 14, 'ACTIVE', ...);

-- coupons: 슬롯 (발급된 것 + 빈 슬롯)
INSERT INTO coupons (id, coupon_event_id, user_id, status, issued_at, expires_at)
VALUES
    (1, 1, 1, 'ISSUED', '2026-06-23 09:00:00', '2026-07-23 09:00:00'),
    (2, 1, 2, 'USED',   '2026-06-23 09:05:00', '2026-07-23 09:05:00'),
    (3, 1, 3, 'ISSUED', '2026-06-23 09:10:00', '2026-07-23 09:10:00'),
    (4, 1, NULL, 'AVAILABLE', NULL, NULL),   -- 빈 슬롯
    ... ;

-- coupon_issues 테이블 seed 삭제
```

---

### 테스트 파일

| 파일 | 바꿀 내용 |
|---|---|
| `AdminCouponServiceTest.java` | → `AdminCouponEventServiceTest.java` |
| `CouponIssueServiceTest.java` | → `CouponSlotServiceTest.java` |
| `CouponIssueConcurrencyTest.java` | 슬롯 선점 + Redisson 락 검증 |
| `CouponQueryServiceTest.java` | `findAllByUserId` 기준으로 수정 |
| `MyCouponControllerTest.java` | 응답 필드 변경 반영 |
| `CouponEventControllerTest.java` | ADMIN_INDIVIDUAL 목록 제외 검증 |

**꼭 넣을 동시성 테스트:**

```text
이벤트 슬롯 5개 / 동시 요청 30개
→ ISSUED 슬롯 정확히 5개
→ issuedQuantity == 5
→ user_id 중복 없음
```

---

## B-4. API 변경 요약 (프론트/기획 공유용)

| 기존 | 변경 후 | 비고 |
|---|---|---|
| `POST /api/admin/coupons` | `POST /api/admin/coupon-events` | 이벤트+슬롯 동시 생성 |
| `GET /api/admin/coupons` | `GET /api/admin/coupon-events` | |
| `POST /api/admin/coupons/{id}/issue` | `POST /api/admin/coupon-events/{eventId}/issue` | couponId → eventId |
| `POST /api/admin/coupons/{id}/broadcast` | **삭제 또는 별도 기획** | 전체 발급은 ADMIN_INDIVIDUAL 이벤트 대량 issue로 대체 |
| `GET /api/admin/coupons/{id}/issues` | `GET /api/admin/coupon-events/{eventId}/coupons` | |
| `POST /api/coupon-events/{id}/issue` | **동일 URL, 내부 로직만 변경** | |
| `GET /api/users/me/coupons` | **동일 URL, 응답 필드 변경** | |

---

# Part C — Redisson 분산 락 적용 방법

## C-1. 우리 프로젝트 현재 상태

Redisson은 **이미 적용되어 있습니다.**

| 파일 | 역할 |
|---|---|
| `global/config/RedissonConfig.java` | RedissonClient 빈 생성 |
| `global/lock/LockService.java` | `withLock(key, supplier)` — Redisson `RLock.tryLock()` 사용 |
| 테스트 | `LockService.local()` — Redis 없을 때 인메모리 락 |

자세한 배경은 `docs/redisson-distributed-lock-guide.md` 참고.

---

## C-2. 쿠폰 발급에서 락을 쓰는 이유 (쉬운 설명)

선착순 쿠폰은 **100명이 동시에 「받기」** 를 누를 수 있습니다.

락이 없으면:

```text
A와 B가 동시에 「남은 슬롯 1개」를 봄
→ 둘 다 같은 슬롯에 user_id를 씀
→ 데이터 꼬임
```

Redisson 락:

```text
같은 eventId 요청은 줄을 서서 하나씩 처리
→ 슬롯 1개씩 정확히 할당
```

---

## C-3. 락 키 규칙

```java
private static final String COUPON_EVENT_LOCK_PREFIX = "lock:coupon-event:";

// 사용
lockService.withLock(COUPON_EVENT_LOCK_PREFIX + eventId, () -> { ... });
```

| 키 | 언제 |
|---|---|
| `lock:coupon-event:{eventId}` | 유저 참여, 관리자 개별 발급 **공통** |
| ~~`lock:admin-coupon:{couponId}`~~ | **삭제** — eventId 기준으로 통일 |

**왜 eventId 하나로 통일하나:** 슬롯이 Event에 속하므로, 같은 이벤트에 대한 모든 발급은 **한 줄**로 처리해야 합니다.

---

## C-4. 트랜잭션 경계 (매우 중요)

### 하면 안 되는 패턴

```java
@Transactional
public void assignSlot(...) {
    lockService.withLock(key, () -> {
        // DB work
    });
}  // ← 락이 풀린 뒤에 트랜잭션이 커밋될 수 있음
```

### 올바른 패턴

```java
// CouponSlotService — 락만 잡음 (@Transactional 없음)
public CouponEventIssueResponse assignSlot(Long eventId, Long userId) {
    return lockService.withLock(
        "lock:coupon-event:" + eventId,
        () -> executor.assignSlotInTransaction(eventId, userId)
    );
}

// CouponSlotTransactionExecutor — DB 작업 (@Transactional)
@Component
public class CouponSlotTransactionExecutor {
    @Transactional
    public CouponEventIssueResponse assignSlotInTransaction(Long eventId, Long userId) {
        // ...
    }
}
```

**기억할 것:** 락 바깥 → 트랜잭션 안쪽. 서로 다른 Spring 빈으로 분리.

---

## C-5. DB 제약 (락만으로는 부족)

Redisson이 있어도 **DB unique constraint**는 반드시 둡니다.

```sql
-- coupons: 같은 이벤트에서 같은 유저 중복 발급 방지
ALTER TABLE coupons ADD CONSTRAINT uk_coupons_event_user
    UNIQUE (coupon_event_id, user_id);
```

> MySQL 등에서 `user_id NULL` row는 unique에 여러 개 허용되는 경우가 많아, **빈 슬롯 중복**은 락 + `FOR UPDATE`로 막습니다.

---

## C-6. LockService leaseTime (선택 개선)

현재 `LockService`는 `tryLock(waitTime)` 만 사용 (watchdog 모드).  
bulk INSERT가 큰 이벤트 생성 시 오래 걸리면 leaseTime을 명시할 수 있습니다.

```java
acquired = lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS);
```

`leaseTime`은 **가장 긴 DB 트랜잭션 시간보다 길게** 잡습니다.  
상세: `docs/redisson-distributed-lock-guide.md` § LockService Replacement.

---

# Part D — 작업 순서 체크리스트

한 번에 다 바꾸지 말고 **아래 순서**를 권장합니다.

## Phase 1 — 스키마·엔티티

- [ ] `CouponEventType` enum 추가
- [ ] `CouponStatus` enum 의미 변경 (AVAILABLE, ISSUED, USED, EXPIRED)
- [ ] `CouponEvent`에 type, discountAmount, minOrderAmount, validDays 추가
- [ ] `Coupon`을 슬롯 모델로 재작성 (coupon_event_id, nullable user_id)
- [ ] `CouponIssue` 및 관련 repository **아직 삭제하지 말고** 병행 가능하면 Phase 3에서 삭제

## Phase 2 — 핵심 발급 로직

- [ ] `CouponSlotTransactionExecutor` 작성 (FOR UPDATE + assign)
- [ ] `CouponSlotService` 작성 (Redisson 락 wrapper)
- [ ] `CouponParticipationService` (유저 참여)
- [ ] DB unique `(coupon_event_id, user_id)` 추가

## Phase 3 — API 교체

- [ ] `AdminCouponEventController` + `AdminCouponEventService` (이벤트 생성 + bulk slot)
- [ ] `CouponEventController` → 새 서비스 연결
- [ ] `CouponQueryService` → 슬롯 기준 조회
- [ ] `AdminCouponController` deprecated / 삭제
- [ ] `CouponIssue*` 파일 삭제

## Phase 4 — 정리·운영

- [ ] `CouponCleanupService` + 스케줄러
- [ ] `data.sql` seed 갱신
- [ ] 동시성 테스트 (슬롯 5 / 요청 30)
- [ ] API 문서·프론트 팀 공유

## Phase 5 — 검증

```powershell
.\gradlew.bat test
.\gradlew.bat javadoc
```

---

## 부록: 자주 묻는 질문

### Q. Coupon 테이블 이름 그대로 써도 되나?

네. DB 테이블명 `coupons` 유지 가능. 다만 코드 주석·JavaDoc에 **「슬롯 = 유저 쿠폰 1장」** 이라고 명시해 팀 혼동을 줄이세요.

### Q. broadcast(전체 발급)는 어떻게 하나?

`ADMIN_INDIVIDUAL` 이벤트를 만들고, ACTIVE 유저 목록을 돌면서 `assignSlots(eventId, userIds)` 호출하면 됩니다. 슬롯 수 = 유저 수로 이벤트를 미리 만들거나, 유저 수만큼 슬롯이 있는 이벤트에만 broadcast 가능.

### Q. NEW_SIGNUP은 어디서 호출하나?

`AuthService.signup()` 성공 후 `couponParticipationService.participate(newUserId, signupEventId)` 또는 별도 `assignSlot` 호출. ACTIVE 상태의 NEW_SIGNUP 타입 이벤트가 있을 때만.

### Q. 결제에서 쿠폰 사용은?

`Coupon.id`(슬롯 ID)를 payment/trade에 참조. `status ISSUED` + `expiresAt > now` 검증 후 `USED`로 변경.

---

## 관련 문서

- `docs/redisson-distributed-lock-guide.md` — Redisson 락 상세
- `docs/current-fix-points.md` — 프로젝트 전체 수정 포인트

---

**작성 목적:** 쿠폰 도메인을 「Event = 규칙 + 캠페인, Coupon = 슬롯 1장」 모델로 통일하고, `CouponIssue`를 제거하며, Redisson 분산 락으로 선착순 발급 정합성을 지키기 위함.
