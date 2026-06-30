# Frontend Product Seller UX Improvement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve the public product and seller flows from the UX specification: header account menu, product filters/cards, report modal, and sell/edit product forms.

**Architecture:** Keep changes inside the existing React/Vite frontend. Add small product UX helper modules for shared categories, price formatting/parsing, image preview, and region option helpers; then wire those helpers into existing pages without changing backend contracts.

**Tech Stack:** React 19, React Router, React Bootstrap, lucide-react, Vite, node:test source-level tests.

---

### Task 1: Shared Product UX Helpers

**Files:**
- Create: `frontend/src/pages/productFormUtils.js`
- Test: `frontend/src/pages/productFormUtils.test.js`

- [ ] Add fixed product category options and helpers to format/parse price input values.
- [ ] Add validation helpers for title, price, category, region, description.
- [ ] Add helpers to normalize region display and derive district/neighborhood select options from either flat or parent-shaped API region rows.
- [ ] Add tests for comma price display, numeric payload parsing, minimum price validation, category validation, and region option derivation.
- [ ] Run `npm test`.

### Task 2: Product List and Product Card UX

**Files:**
- Modify: `frontend/src/pages/ProductListPage.jsx`
- Modify: `frontend/src/components/ProductCard.jsx`
- Modify: `frontend/src/styles/theme.css`
- Test: `frontend/src/pages/productUx.test.js`

- [ ] Replace free-text category search with a fixed select.
- [ ] Keep region search as a select, using helper-normalized labels.
- [ ] Make the entire product card navigate to detail.
- [ ] Move like/heart metadata to the image top-right and stop click propagation for card actions.
- [ ] Remove chat count display from product cards.
- [ ] Keep status, region/category tags, price, and detail affordance visible.
- [ ] Add source-level tests for select category, card navigation, no chat count, and heart overlay.
- [ ] Run `npm test` and `npm run build`.

### Task 3: Sell/Edit Product Form UX

**Files:**
- Modify: `frontend/src/pages/SellProductPage.jsx`
- Modify: `frontend/src/pages/EditProductPage.jsx`
- Modify: `frontend/src/styles/theme.css`
- Test: `frontend/src/pages/productUx.test.js`

- [ ] Replace price number inputs with text input display using commas and fixed won suffix while submitting numeric values.
- [ ] Replace free-text category input with fixed category select.
- [ ] Add placeholders and field-level validation messages.
- [ ] Add custom image upload box, preview, main-image badge, and remove action.
- [ ] Add description guide, realtime character counter, and 2000 character max.
- [ ] Prevent duplicate submission while submitting.
- [ ] Add tests for utility usage and source-level UI markers.
- [ ] Run `npm test` and `npm run build`.

### Task 4: Header and Report Modal UX

**Files:**
- Modify: `frontend/src/layouts/UserLayout.jsx`
- Modify: `frontend/src/features/reports/ReportModal.jsx`
- Modify: `frontend/src/pages/ProductDetailPage.jsx`
- Modify: `frontend/src/styles/theme.css`
- Test: `frontend/src/pages/productUx.test.js`

- [ ] Increase header height and brand prominence.
- [ ] Remove visible My Page text from topbar and expose a user icon/dropdown account menu.
- [ ] Link dropdown items to current mypage-related routes and logout.
- [ ] Keep login/signup flow for logged-out users.
- [ ] Remove product/user ID manual inputs from ReportModal when IDs are supplied by the caller.
- [ ] Keep report reason input and submit behavior.
- [ ] Add tests for account dropdown marker and report modal no manual ID fields.
- [ ] Run `npm test` and `npm run build`.

### Task 5: Final Verification and Review

**Files:**
- Review all changed frontend files.

- [ ] Run full frontend `npm test`.
- [ ] Run full frontend `npm run build`.
- [ ] Inspect `git diff --stat` and changed files.
- [ ] Perform final spec compliance review and code quality review.
