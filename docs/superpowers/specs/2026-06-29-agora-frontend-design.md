# Agora Frontend And Missing API Design

## Goal

Build a usable React frontend for Agora, styled with Bootstrap, in `frontend/` inside the existing backend repository. The frontend must connect to the real Spring Boot APIs rather than remain a static demo. Where the user-facing screens require list data that the backend does not currently expose, add small backend read APIs using the existing project style.

## Source Screens

The PDF `사용자.pdf` is the visual reference. It is not a strict contract. The implementation should keep the product direction and user flow, but it must follow the actual backend domain model and API contracts.

## App Separation

The user and admin experiences must be accessed as clearly separate screens.

User routes:

- `/`
- `/login`
- `/signup`
- `/regions/setup`
- `/products`
- `/products/:productId`
- `/sell`
- `/events`
- `/chat`
- `/chat/:chatRoomId`
- `/me`
- `/me/likes`
- `/me/products`
- `/me/trades`
- `/me/reviews`
- `/me/coupons`

Admin routes:

- `/admin/login`
- `/admin`
- `/admin/products`
- `/admin/users`
- `/admin/reports`
- `/admin/payments`
- `/admin/coupons`

The user app uses a marketplace layout with a top navigation bar. The admin app uses a dashboard layout with a sidebar. `/admin/*` must not share the normal user navigation, and admin auth state must be handled separately from normal user auth state.

## User Features

Implement these user features against existing APIs. For features that need the new my-trades or my-reviews list data, implement the backend APIs described below before wiring the frontend page:

- Signup, login, logout, token reissue.
- Region selection and preferred region update.
- Product list, product search, product detail, product create, product update, product delete.
- Product image upload.
- Product like and unlike.
- My liked products.
- My products for sale.
- Coupon event list, coupon issue, my coupons.
- Chat room open, my chat rooms, message history, image message upload, realtime STOMP text messaging.
- Negotiation offer create, accept, reject, extension request, extension approve, extension reject, expire.
- Trade start, trade detail, trade complete, reservation expiration, rating request message.
- Payment prepare, confirm, refund, refund status.
- Review create and trade review lookup.
- Product and user report creation.

Do not add a user-facing report history screen. Reporting is a user action from product/detail/chat contexts; report listing and processing belongs to admin.

## Missing User APIs To Add

### My Trades

Add:

`GET /api/users/me/trades?role=buyer|seller|all&page=0&size=20`

Purpose:

Return products/trades where the current user participated as buyer or seller.

Response DTO fields:

- `tradeId`
- `productId`
- `productTitle`
- `productPrice`
- `tradePrice`
- `status`
- `role`
- `counterpartNickname`
- `paymentStatus`
- `completedAt`
- `createdAt`

Implementation style:

- Add response record under `domain/trade/dto/response`.
- Add query repository interface and implementation named `TradeQueryRepository` and `TradeQueryRepositoryImpl`.
- Use `JPAQueryFactory`, QueryDSL joins, `Projections.constructor`, `PageImpl`, and pageable offset/limit, matching the existing `ProductSearchRepositoryImpl` style.
- Add a read-only service method in `TradeService` unless implementation shows a separate query service is already established in this package.
- Add controller endpoint returning `ApiResponse<PageResponse<MyTradeResponse>>`.

### My Reviews

Add:

`GET /api/users/me/reviews?type=written|received&page=0&size=20`

Purpose:

Return reviews written by the current user or received by the current user.

Response DTO fields:

- `reviewId`
- `tradeId`
- `productId`
- `productTitle`
- `reviewerNickname`
- `targetNickname`
- `rating`
- `content`
- `createdAt`

Implementation style:

- Add response record under `domain/review/dto/response`.
- Add `ReviewQueryRepository` and `ReviewQueryRepositoryImpl` using QueryDSL.
- Use the same `ApiResponse<PageResponse<...>>` response shape.
- Keep `GET /api/trades/{tradeId}/reviews` for trade-specific review lookup.

## Admin Features

Implement the admin frontend against existing admin APIs:

- Admin login/logout.
- Admin me and dashboard.
- Product list and product hide.
- User list and user status update.
- Report list and resolve for user/product reports.
- Payment list and payment verification.
- Refund list.
- Coupon event create/list/detail/issue/coupon list.
- Admin account role update through the existing admin account endpoint.

Admin report lists are where reported products/users are reviewed. The user app should only submit reports.

## Frontend Architecture

Create `frontend/` as a Vite React app.

Use:

- React Router for route separation.
- Bootstrap and React Bootstrap for styling.
- Axios or a small fetch wrapper for API calls.
- A central API client that unwraps the backend `ApiResponse` shape and throws useful errors.
- Separate auth modules for user and admin tokens.
- STOMP client for realtime chat, connected to the backend WebSocket endpoint.

Recommended structure:

- `src/app` for router and providers.
- `src/api` for API client and endpoint modules.
- `src/layouts/UserLayout.jsx` and `src/layouts/AdminLayout.jsx`.
- `src/features/auth`, `products`, `chat`, `nego`, `trade`, `payment`, `reviews`, `coupons`, `mypage`, `admin`.
- `src/components` for shared UI pieces.

## Error Handling And Empty States

Every API-backed page should show loading, empty, and error states. Missing optional data should not break rendering. Auth failures should redirect to the correct login page: user auth failures to `/login`, admin auth failures to `/admin/login`.

## Testing And Verification

Backend:

- Add repository/service tests for my-trades and my-reviews filters.
- Add controller tests for endpoint shape and auth handling if existing test style makes this practical.

Frontend:

- Run the Vite build.
- Smoke test key user routes and admin routes in the browser.
- Verify user/admin navigation separation.
- Verify API errors render readable messages.

## Non-Goals

- Do not implement a user report history/list screen.
- Do not fake completed trade/review data when the backend cannot provide it.
- Do not merge user and admin layouts into one shared navigation surface.
- Do not replace existing backend conventions with a new API style.

