# Full UX Remaining Requirements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the remaining requirements from `docs/frontend-product-seller-ux-improvement.md` beyond the first product/seller UX pass.

**Architecture:** Preserve existing Spring Boot and React/Vite boundaries. Implement additive DTO/API changes where possible, keep backend concurrency primitives intact, and split work by domain so coupon, product, chat, and admin changes can be reviewed independently.

**Tech Stack:** Spring Boot, JPA, Querydsl, Redis/Redisson where already present, React 19, React Bootstrap, Vite, node:test, JUnit/Mockito.

---

### Task 1: Coupon User UX and Shared Formatting

**Files:**
- Create/modify: `frontend/src/pages/couponUtils.js`
- Modify: `frontend/src/pages/CouponEventsPage.jsx`
- Modify: `frontend/src/pages/MyCouponsPage.jsx`
- Modify: `frontend/src/styles/theme.css`
- Test: `frontend/src/pages/couponUx.test.js`

- [x] Add coupon amount, status, D-day, remaining quantity, and issue-rate helper functions.
- [x] Convert coupon event cards to ticket-style cards with status badge, friendly conditions, period wording, remaining progress bar, and button state.
- [x] Sort and filter my coupons by usable/used/expired state, add product-list CTA for usable coupons, and highlight soon-to-expire coupons.
- [x] Run `npm test` and `npm run build`.

### Task 2: Coupon Backend Transactions, DTOs, and Validation

**Files:**
- Modify: `src/main/java/com/team7/agora/domain/coupon/service/CouponParticipationService.java`
- Modify: `src/main/java/com/team7/agora/domain/coupon/service/AdminCouponEventService.java`
- Modify: `src/main/java/com/team7/agora/domain/coupon/dto/request/AdminCouponEventCreateRequest.java`
- Modify: `src/main/java/com/team7/agora/domain/coupon/dto/request/AdminCouponEventIssueRequest.java`
- Modify response DTOs under `src/main/java/com/team7/agora/domain/coupon/dto/response`
- Test coupon service/controller tests under `src/test/java/com/team7/agora/domain/coupon`

- [x] Remove fragile read-only transaction behavior from write paths.
- [x] Add defensive service validation for event date, quantity, discount, minimum order amount, individual issue type, duplicate IDs, and nonexistent users.
- [x] Add additive response fields such as remaining quantity, issue rate, canIssue, ended, labels, event name, user email, discount amount, min order amount.
- [x] Add regression tests for user issue, duplicate issue, admin create, admin issue, and issue history DTOs.
- [x] Run targeted coupon tests and then full backend tests.

### Task 3: Admin Coupon UI

**Files:**
- Modify: `frontend/src/pages/AdminPlaceholderPages.jsx`
- Modify: `frontend/src/pages/adminPageUtils.js`
- Modify: `frontend/src/api/adminApi.js`
- Modify: `frontend/src/styles/theme.css`
- Test: `frontend/src/api/adminApi.test.js`, `frontend/src/pages/emptyStates.test.js` or a new admin UX source test

- [x] Display admin coupon enums as Korean labels while preserving raw API values.
- [x] Format coupon money inputs with commas and won suffix.
- [x] Strengthen client validation for event dates, amounts, quantity, and issue targets.
- [x] Show target user IDs as chips with invalid tokens visible.
- [x] Add richer event detail/issue panels using new DTO fields.
- [x] Run frontend tests and build.

### Task 4: Product Search, Favorites, and Seller Follow-up

**Files:**
- Modify: product DTO/service/query classes as needed for like count/liked/image/region/seller metadata.
- Modify: `frontend/src/pages/ProductDetailPage.jsx`
- Modify: `frontend/src/pages/ProductListPage.jsx`
- Modify: `frontend/src/pages/UserPlaceholderPages.jsx`
- Modify: `frontend/src/pages/LikedProductsPage.jsx`
- Modify: `frontend/src/pages/MyProductsPage.jsx`
- Modify: `frontend/src/pages/SellProductPage.jsx`
- Modify: `frontend/src/pages/EditProductPage.jsx`
- Modify: `frontend/src/components/ProductCard.jsx`
- Test: product backend tests and frontend product UX tests

- [x] Add staged region selector and category label mapping across list/home/forms/cards/detail.
- [x] Add product form preview card, two-column image/basic layout, and remove awkward top eyebrow text.
- [x] Add like toggle on detail and favorite cards, including immediate removal from favorites and empty-state CTA.
- [x] Add seller product status/actions where backend owner APIs already exist or add small owner APIs if missing.
- [x] Run frontend and backend product tests.

### Task 5: Chat Stability and Message UI

**Files:**
- Modify: `frontend/src/pages/ChatRoomPage.jsx`
- Modify: `frontend/src/features/chat/useChatSocket.js`
- Modify: `frontend/src/api/client.js`
- Modify: `frontend/src/api/chatApi.js`
- Test: chat API/socket/page source tests

- [x] Add optimistic/pending/failed message UI and retry affordance.
- [x] Render SYSTEM messages separately and IMAGE messages inline with validation and preview.
- [x] Fix STOMP connection/subscription readiness, publish failure handling, error categories, token bearer commonization, and duplicate dedupe calls.
- [x] Add left/right message alignment where current-user information is available.
- [x] Run frontend tests and build.

### Task 6: Nego and Checkout UX

**Files:**
- Modify: `frontend/src/features/nego/NegoPanel.jsx`
- Modify: `frontend/src/api/negoApi.js`
- Modify: `frontend/src/pages/CheckoutPage.jsx`
- Modify backend nego DTO/API only if current-offer/list support is required.
- Test: action API tests, source tests, backend nego tests if APIs change

- [x] Remove manual offer ID input and render per-offer action buttons by role/status.
- [x] Show accepted-offer payment CTA with remaining time when `tradeId` is available.
- [x] Add offer price validation against product price where available.
- [x] Remove manual technical payment key UX as far as current payment API allows.
- [x] Run frontend tests/build and backend nego tests if changed.

### Task 7: Chat Room List/Product Metadata Backend + Frontend

**Files:**
- Modify: `ChatRoomResponse`, `ChatService`, chat repositories/tests
- Modify: `frontend/src/pages/ChatRoomsPage.jsx`
- Modify: `frontend/src/pages/ChatRoomPage.jsx`
- Modify: `frontend/src/api/chatApi.js`

- [x] Add product title/price/status/thumbnail, participant nicknames, last message, and unread count to chat room responses.
- [x] Show product card at chat-room top and richer room list rows.
- [x] Remove manual product ID room opening from chat list.
- [x] Run backend chat tests and frontend tests/build.

### Task 8: Admin Shell, Role Menu, Accounts, and Reports

**Files:**
- Modify: `frontend/src/layouts/AdminLayout.jsx`
- Modify: `frontend/src/pages/AdminPlaceholderPages.jsx`
- Modify: `frontend/src/app/routes.jsx`
- Modify: `frontend/src/api/adminApi.js`
- Modify backend admin account/report DTO/service/controller tests

- [x] Add admin topbar/avatar/dropdown and role-filtered sidebar.
- [x] Move admin account role management out of user rows into root-admin account management.
- [x] Add root admin account list/protections such as no self-demotion and last-root protection.
- [x] Improve report DTOs/API filters/search and frontend pending/completed tabs.
- [x] Improve payment verification result display.
- [x] Run admin backend tests and frontend tests/build.

### Task 9: Admin Approval and Product Approval Workflow

**Files:**
- Add/modify backend admin approval domain classes, DTOs, services, controllers, repositories, tests.
- Modify product admin DTO/API/service/controller/tests.
- Modify frontend admin routes/pages/API.

- [x] Add root approval management page/API and domain-admin my-request page/API.
- [x] Define approval-required operations and route high-risk admin actions through approval requests.
- [x] Add admin product approval status/filter/detail modal/approve API and UI.
- [x] Add seller nickname/product title display fields where needed.
- [x] Run full backend and frontend verification.

### Task 10: Final Full Verification

**Files:**
- Review all changed files.

- [x] Run `npm test` and `npm run build` in `frontend`.
- [x] Run `.\\gradlew.bat test` and `.\\gradlew.bat build` from repo root.
- [x] Perform final spec compliance review against the original markdown.
- [x] Perform final code quality review over the whole diff.
