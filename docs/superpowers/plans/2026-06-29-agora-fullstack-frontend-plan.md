# Agora Fullstack Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a usable React + Bootstrap Agora frontend with separate user/admin apps, and add the missing backend read APIs required by the my-trades and my-reviews screens.

**Architecture:** Implement backend read APIs first with the existing Spring Boot, DTO, Service, Controller, QueryDSL, `ApiResponse`, and `PageResponse` patterns. Then create a Vite React frontend in `frontend/` with separate layouts, route guards, API modules, loading/empty/error states, and Bootstrap UI.

**Tech Stack:** Spring Boot, QueryDSL, JUnit, MockMvc, Vite, React, React Router, Bootstrap, React Bootstrap, Axios, STOMP, SockJS.

---

## File Structure

Backend create:
- `src/main/java/com/team7/agora/domain/trade/dto/request/MyTradeRole.java`
- `src/main/java/com/team7/agora/domain/trade/dto/response/MyTradeResponse.java`
- `src/main/java/com/team7/agora/domain/trade/repository/TradeQueryRepository.java`
- `src/main/java/com/team7/agora/domain/trade/repository/TradeQueryRepositoryImpl.java`
- `src/main/java/com/team7/agora/domain/review/dto/request/MyReviewType.java`
- `src/main/java/com/team7/agora/domain/review/dto/response/MyReviewResponse.java`
- `src/main/java/com/team7/agora/domain/review/repository/ReviewQueryRepository.java`
- `src/main/java/com/team7/agora/domain/review/repository/ReviewQueryRepositoryImpl.java`
- `src/main/java/com/team7/agora/domain/user/controller/MyActivityController.java`
- `src/test/java/com/team7/agora/domain/trade/repository/TradeQueryRepositoryImplTest.java`
- `src/test/java/com/team7/agora/domain/review/repository/ReviewQueryRepositoryImplTest.java`
- `src/test/java/com/team7/agora/domain/user/controller/MyActivityControllerTest.java`

Backend modify:
- `src/main/java/com/team7/agora/domain/trade/service/TradeService.java`
- `src/main/java/com/team7/agora/domain/review/service/ReviewService.java`

Frontend create:
- `frontend/package.json`, `frontend/index.html`, `frontend/src/main.jsx`
- `frontend/src/app/App.jsx`, `frontend/src/app/routes.jsx`
- `frontend/src/api/client.js` and domain API modules
- `frontend/src/auth/tokenStorage.js`, `frontend/src/auth/AuthContext.jsx`
- `frontend/src/layouts/UserLayout.jsx`, `frontend/src/layouts/AdminLayout.jsx`
- `frontend/src/components/LoadingState.jsx`, `EmptyState.jsx`, `ErrorState.jsx`, `ProductCard.jsx`, `StatusBadge.jsx`, `MoneyText.jsx`
- `frontend/src/features/auth`, `products`, `coupons`, `mypage`, `chat`, `nego`, `trade`, `payment`, `reviews`, `reports`, `admin`
- `frontend/src/styles/theme.css`

---

### Task 1: Add My Trades Backend API

**Files:** create and modify the trade DTO, QueryDSL repository, service, `MyActivityController`, and tests listed above.

- [ ] **Step 1: Add failing repository tests**

Create `TradeQueryRepositoryImplTest` with three tests:

```java
@Test
void findMyTrades_returnsBuyerTradesOnly() {
    Page<MyTradeResponse> page = tradeQueryRepository.findMyTrades(buyer.getId(), MyTradeRole.BUYER, PageRequest.of(0, 20));
    assertThat(page.getContent()).extracting(MyTradeResponse::role).containsOnly("BUYER");
}

@Test
void findMyTrades_returnsSellerTradesOnly() {
    Page<MyTradeResponse> page = tradeQueryRepository.findMyTrades(seller.getId(), MyTradeRole.SELLER, PageRequest.of(0, 20));
    assertThat(page.getContent()).extracting(MyTradeResponse::role).containsOnly("SELLER");
}

@Test
void findMyTrades_returnsAllParticipantTrades() {
    Page<MyTradeResponse> page = tradeQueryRepository.findMyTrades(user.getId(), MyTradeRole.ALL, PageRequest.of(0, 20));
    assertThat(page.getContent()).extracting(MyTradeResponse::role).contains("BUYER", "SELLER");
}
```

- [ ] **Step 2: Run failing test**

Run: `./gradlew.bat test --tests "com.team7.agora.domain.trade.repository.TradeQueryRepositoryImplTest"`

Expected: compile fails because the new trade query types do not exist.

- [ ] **Step 3: Add trade request/response types**

Create `MyTradeRole`:

```java
package com.team7.agora.domain.trade.dto.request;

import java.util.Locale;

public enum MyTradeRole {
    BUYER, SELLER, ALL;

    public static MyTradeRole from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return MyTradeRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
```

Create `MyTradeResponse`:

```java
package com.team7.agora.domain.trade.dto.response;

import java.time.LocalDateTime;

public record MyTradeResponse(
    Long tradeId,
    Long productId,
    String productTitle,
    Long productPrice,
    Long tradePrice,
    String status,
    String role,
    String counterpartNickname,
    String paymentStatus,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {
}
```

- [ ] **Step 4: Add trade QueryDSL repository**

Create `TradeQueryRepository`:

```java
public interface TradeQueryRepository {
    Page<MyTradeResponse> findMyTrades(Long userId, MyTradeRole role, Pageable pageable);
}
```

Create `TradeQueryRepositoryImpl` using `JPAQueryFactory`, `QTrade`, `QProduct`, `QUser`, and `QPayment`. Filter by buyer, seller, or both. Select with `Projections.constructor(MyTradeResponse.class, ...)`. Use `PageImpl` with a separate count query. Order by `trade.id.desc()`.

- [ ] **Step 5: Wire service and endpoint**

Add `TradeQueryRepository` to `TradeService` constructor. Add:

```java
@Transactional(readOnly = true)
public Page<MyTradeResponse> getMyTrades(Long userId, MyTradeRole role, Pageable pageable) {
    return tradeQueryRepository.findMyTrades(userId, role, pageable);
}
```

Create `MyActivityController` and add:

```java
@GetMapping("/api/users/me/trades")
public ApiResponse<PageResponse<MyTradeResponse>> getMyTrades(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam(defaultValue = "all") String role,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Page<MyTradeResponse> responses = tradeService.getMyTrades(userDetails.getUserId(), MyTradeRole.from(role), PageRequest.of(page, size));
    return ApiResponse.success("내 거래 목록을 조회했습니다.", PageResponse.from(responses));
}
```

- [ ] **Step 6: Run and commit**

Run: `./gradlew.bat test --tests "com.team7.agora.domain.trade.repository.TradeQueryRepositoryImplTest"`

Commit:

```bash
git add src/main/java/com/team7/agora/domain/trade src/main/java/com/team7/agora/domain/user/controller/MyActivityController.java src/test/java/com/team7/agora/domain/trade
git commit -m "feat: add my trade list api"
```

---

### Task 2: Add My Reviews Backend API

**Files:** create and modify the review DTO, QueryDSL repository, service, `MyActivityController`, and tests listed above.

- [ ] **Step 1: Add failing review query tests**

Create `ReviewQueryRepositoryImplTest` with written/received cases:

```java
@Test
void findMyReviews_returnsWrittenReviews() {
    Page<MyReviewResponse> page = reviewQueryRepository.findMyReviews(reviewer.getId(), MyReviewType.WRITTEN, PageRequest.of(0, 20));
    assertThat(page.getContent()).extracting(MyReviewResponse::reviewerNickname).containsOnly(reviewer.getNickname());
}

@Test
void findMyReviews_returnsReceivedReviews() {
    Page<MyReviewResponse> page = reviewQueryRepository.findMyReviews(target.getId(), MyReviewType.RECEIVED, PageRequest.of(0, 20));
    assertThat(page.getContent()).extracting(MyReviewResponse::targetNickname).containsOnly(target.getNickname());
}
```

- [ ] **Step 2: Run failing test**

Run: `./gradlew.bat test --tests "com.team7.agora.domain.review.repository.ReviewQueryRepositoryImplTest"`

Expected: compile fails because the new review query types do not exist.

- [ ] **Step 3: Add review request/response types**

Create `MyReviewType`:

```java
package com.team7.agora.domain.review.dto.request;

import java.util.Locale;

public enum MyReviewType {
    WRITTEN, RECEIVED;

    public static MyReviewType from(String value) {
        if (value == null || value.isBlank()) {
            return WRITTEN;
        }
        return MyReviewType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
```

Create `MyReviewResponse`:

```java
package com.team7.agora.domain.review.dto.response;

import java.time.LocalDateTime;

public record MyReviewResponse(
    Long reviewId,
    Long tradeId,
    Long productId,
    String productTitle,
    String reviewerNickname,
    String targetNickname,
    int rating,
    String content,
    LocalDateTime createdAt
) {
}
```

- [ ] **Step 4: Add review QueryDSL repository**

Create `ReviewQueryRepository` with `Page<MyReviewResponse> findMyReviews(Long userId, MyReviewType type, Pageable pageable)`. Implement it with `JPAQueryFactory`, joining review, trade, product, reviewer, and target user. Filter reviewer for `WRITTEN`, target user for `RECEIVED`, order by `review.id.desc()`, and return `PageImpl`.

- [ ] **Step 5: Wire service and endpoint**

Add `ReviewQueryRepository` to `ReviewService` constructor. Add:

```java
@Transactional(readOnly = true)
public Page<MyReviewResponse> getMyReviews(Long userId, MyReviewType type, Pageable pageable) {
    return reviewQueryRepository.findMyReviews(userId, type, pageable);
}
```

Add endpoint to `MyActivityController`:

```java
@GetMapping("/api/users/me/reviews")
public ApiResponse<PageResponse<MyReviewResponse>> getMyReviews(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam(defaultValue = "written") String type,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Page<MyReviewResponse> responses = reviewService.getMyReviews(userDetails.getUserId(), MyReviewType.from(type), PageRequest.of(page, size));
    return ApiResponse.success("내 후기 목록을 조회했습니다.", PageResponse.from(responses));
}
```

- [ ] **Step 6: Run and commit**

Run: `./gradlew.bat test --tests "com.team7.agora.domain.review.repository.ReviewQueryRepositoryImplTest"`

Commit:

```bash
git add src/main/java/com/team7/agora/domain/review src/main/java/com/team7/agora/domain/user/controller/MyActivityController.java src/test/java/com/team7/agora/domain/review
git commit -m "feat: add my review list api"
```

---

### Task 3: Scaffold React Frontend Foundation

**Files:** create `frontend/` app, app router, layouts, shared state components, and theme.

- [ ] **Step 1: Create Vite files and dependencies**

Create `frontend/package.json`:

```json
{
  "scripts": {"dev":"vite --host 127.0.0.1","build":"vite build","preview":"vite preview --host 127.0.0.1"},
  "dependencies": {"@stomp/stompjs":"latest","axios":"latest","bootstrap":"latest","lucide-react":"latest","react":"latest","react-bootstrap":"latest","react-dom":"latest","react-router-dom":"latest","sockjs-client":"latest"},
  "devDependencies": {"@vitejs/plugin-react":"latest","vite":"latest"}
}
```

- [ ] **Step 2: Add routing and layouts**

Create `UserLayout` with marketplace nav and `AdminLayout` with sidebar. Create routes for all user paths and `/admin/*` paths. Admin routes must never render `UserLayout`.

- [ ] **Step 3: Add shared UI and theme**

Create `LoadingState`, `EmptyState`, `ErrorState`, `StatusBadge`, `MoneyText`, `ProductCard`, and `theme.css` using Bootstrap variables plus orange action color and green Agora brand accents.

- [ ] **Step 4: Install, build, commit**

Run in `frontend`: `npm install`, then `npm run build`.

Commit:

```bash
git add frontend
git commit -m "feat: scaffold react frontend"
```

---

### Task 4: Implement API Client And Auth

**Files:** create `frontend/src/api/client.js`, all endpoint modules, `frontend/src/auth/tokenStorage.js`, `frontend/src/auth/AuthContext.jsx`, and auth pages.

- [ ] **Step 1: Add token isolation**

Use separate storage keys: `agora.user.accessToken` and `agora.admin.accessToken`.

- [ ] **Step 2: Add API client**

Axios base URL is `import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'`. Request interceptor uses admin token for `/api/admin` and user token otherwise. Response interceptor unwraps backend `ApiResponse.data` and throws readable errors.

- [ ] **Step 3: Add login and guards**

User protected routes redirect to `/login`; admin protected routes redirect to `/admin/login`.

- [ ] **Step 4: Build and commit**

Run in `frontend`: `npm run build`.

Commit:

```bash
git add frontend/src/api frontend/src/auth frontend/src/features/auth frontend/src/app/routes.jsx
git commit -m "feat: add frontend auth and api client"
```

---

### Task 5: Implement User Marketplace And My Page

**Files:** create product, coupon, region, mypage API modules and pages.

- [ ] **Step 1: Add user API modules**

Map product, product image, like, search, region, coupon, my products, my likes, my trades, and my reviews endpoints.

- [ ] **Step 2: Add product and coupon pages**

Implement product list/search, detail, sell form, edit form, coupon event list, and my coupon list. Every page renders loading, empty, and error states.

- [ ] **Step 3: Add my page routes**

Implement `/me`, `/me/likes`, `/me/products`, `/me/trades`, `/me/reviews`, `/me/coupons`. `/me/trades` has buyer/seller/all controls. `/me/reviews` has written/received controls. There is no user report list.

- [ ] **Step 4: Build and commit**

Run in `frontend`: `npm run build`.

Commit:

```bash
git add frontend/src/api frontend/src/features/products frontend/src/features/coupons frontend/src/features/mypage
git commit -m "feat: add user marketplace and my page screens"
```

---

### Task 6: Implement Chat, Negotiation, Trade, Payment, Review, And Report Actions

**Files:** create chat, nego, trade, payment, review, and report API modules and feature pages.

- [ ] **Step 1: Add domain API modules**

Map chat room open/list/messages/images, negotiation create/actions, trade start/detail/complete, payment prepare/confirm/refund/status, review create, trade reviews, product report, and user report endpoints.

- [ ] **Step 2: Add chat socket**

Use STOMP and SockJS. Load message history over HTTP first, then append realtime messages from the room subscription.

- [ ] **Step 3: Add transaction actions**

Inside chat/product/trade screens, wire negotiation request, seller accept/reject, trade start, checkout, payment confirm, trade complete, rating request, and review create.

- [ ] **Step 4: Add report modal only**

Add report modal to product detail and chat context. Do not create a user report history route.

- [ ] **Step 5: Build and commit**

Run in `frontend`: `npm run build`.

Commit:

```bash
git add frontend/src/api frontend/src/features/chat frontend/src/features/nego frontend/src/features/trade frontend/src/features/reviews frontend/src/features/reports
git commit -m "feat: add chat trade payment and review flows"
```

---

### Task 7: Implement Admin Screens

**Files:** create admin API module and admin dashboard/table pages.

- [ ] **Step 1: Add admin API module**

Map `/api/admin/auth`, `/api/admin/me`, `/api/admin/dashboard`, products, users, reports, payments, refunds, coupon events, and accounts endpoints.

- [ ] **Step 2: Add admin pages**

Implement dashboard cards, product table, user table, report table, payment/refund table, and coupon/event table. Wire hide product, update user status, resolve report, verify payment, create coupon event, issue coupons, and update admin account role.

- [ ] **Step 3: Verify separation**

Open `/` and confirm no admin sidebar. Open `/admin` and confirm no user marketplace nav.

- [ ] **Step 4: Build and commit**

Run in `frontend`: `npm run build`.

Commit:

```bash
git add frontend/src/api/adminApi.js frontend/src/features/admin frontend/src/layouts/AdminLayout.jsx
git commit -m "feat: add admin frontend screens"
```

---

### Task 8: Full Verification

**Files:** modify only files required to fix failing checks.

- [ ] **Step 1: Run backend tests**

Run: `./gradlew.bat test`

Expected: all tests pass.

- [ ] **Step 2: Run frontend build**

Run in `frontend`: `npm run build`

Expected: build passes.

- [ ] **Step 3: Run local smoke test**

Run backend: `./gradlew.bat bootRun`

Run frontend: `npm run dev -- --port 5173`

Browser-check user routes: `/`, `/login`, `/products`, `/events`, `/chat`, `/me`, `/me/trades`, `/me/reviews`.

Browser-check admin routes: `/admin/login`, `/admin`, `/admin/products`, `/admin/users`, `/admin/reports`, `/admin/payments`, `/admin/coupons`.

- [ ] **Step 4: Commit verification fixes**

Commit any verification fixes:

```bash
git add frontend src/main/java src/test/java
git commit -m "test: verify agora frontend integration"
```
