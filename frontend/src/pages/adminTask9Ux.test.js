import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf8');

test('admin task 9 routes approval pages for root and domain admins', () => {
  const routes = readSource('../app/routes.jsx');
  const layout = readSource('../layouts/AdminLayout.jsx');
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(routes, /AdminApprovalsPage/);
  assert.match(routes, /AdminMyApprovalRequestsPage/);
  assert.match(routes, /path: 'approval-requests'/);
  assert.match(routes, /path: 'my-approval-requests'/);
  assert.match(layout, /\/admin\/approval-requests/);
  assert.match(layout, /\/admin\/my-approval-requests/);
  assert.match(pages, /export function AdminApprovalsPage/);
  assert.match(pages, /export function AdminMyApprovalRequestsPage/);
});

test('admin task 9 pages expose approval request and product approval actions', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const api = readSource('../api/adminApi.js');

  assert.match(api, /getAdminApprovalRequests/);
  assert.match(api, /getMyAdminApprovalRequests/);
  assert.match(api, /requestAdminRoleChangeApproval/);
  assert.match(api, /updateAdminAccountRole/);
  assert.match(api, /approveAdminApprovalRequest/);
  assert.match(api, /rejectAdminApprovalRequest/);
  assert.match(api, /approveAdminProduct/);
  assert.match(pages, /approvalStatus/);
  assert.match(pages, /selectedProduct/);
  assert.match(pages, /approveAdminProduct/);
  assert.match(pages, /updateAdminAccountRole/);
  assert.doesNotMatch(pages, /requestAdminRoleChangeApproval/);
  assert.match(pages, /value="PENDING"/);
  assert.match(pages, /APPROVED/);
  assert.match(pages, /REJECTED/);
  assert.match(pages, /PENDING/);
});

test('admin products expose domain-specific search conditions and operations table structure', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /productFilters/);
  assert.match(pages, /appliedProductFilters/);
  assert.match(pages, /handleProductSearch/);
  assert.match(pages, /handleProductReset/);
  assert.match(pages, /상품명/);
  assert.match(pages, /판매자 닉네임/);
  assert.match(pages, /판매상태/);
  assert.match(pages, /승인상태/);
  assert.match(pages, /신고 상품만/);
  assert.match(pages, /검색/);
  assert.match(pages, /초기화/);
  assert.match(pages, /등록일/);
  assert.match(pages, /신고/);
  assert.match(pages, /admin-search-panel/);
  assert.match(pages, /admin-filter-options/);
  assert.match(pages, /admin-product-filter-summary/);
  assert.match(pages, /keyword: productFilters\.keyword\.trim\(\)/);
  assert.match(pages, /sellerKeyword: productFilters\.sellerKeyword\.trim\(\)/);
});

test('remaining admin pages expose page-specific Korean filter UX', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /userFilters/);
  assert.match(pages, /filteredUsers/);
  assert.match(pages, /handleUserSearch/);
  assert.match(pages, /handleUserReset/);
  assert.match(pages, /회원 검색/);
  assert.match(pages, /회원 상태/);
  assert.doesNotMatch(pages, /회원 권한/);
  assert.doesNotMatch(pages, /ADMIN_USER_ROLES/);

  assert.match(pages, /reportDraftSearch/);
  assert.match(pages, /handleReportSearch/);
  assert.match(pages, /신고 검색/);
  assert.match(pages, /신고자, 신고된 회원, 상품명/);
  assert.match(pages, /처리 상태/);

  assert.match(pages, /paymentFilters/);
  assert.match(pages, /filteredPayments/);
  assert.match(pages, /handlePaymentSearch/);
  assert.match(pages, /handlePaymentReset/);
  assert.match(pages, /결제 검색/);
  assert.match(pages, /정산 상태/);
  assert.ok(pages.indexOf('controlId="payment-keyword"') < pages.indexOf('controlId="payment-status"'));

  assert.match(pages, /accountFilters/);
  assert.match(pages, /filteredAccounts/);
  assert.match(pages, /handleAccountSearch/);
  assert.match(pages, /handleAccountReset/);
  assert.match(pages, /관리자 검색/);
  assert.match(pages, /관리자 상태/);

  assert.match(pages, /approvalFilters/);
  assert.match(pages, /filteredApprovals/);
  assert.match(pages, /승인 요청 검색/);
  assert.match(pages, /요청 권한/);

  assert.match(pages, /myApprovalFilters/);
  assert.match(pages, /filteredMyApprovals/);
  assert.match(pages, /handleMyApprovalSearch/);
  assert.match(pages, /handleMyApprovalReset/);
  assert.doesNotMatch(pages, /approval-target-admin|approval-requested-role|approval-reason/);
  assert.match(pages, /내 승인 요청/);
  assert.doesNotMatch(pages, /Approval management|My approval requests|Request admin role change|Target admin ID|Requested role|Reason|Request approval|Loading approval requests|Loading my approval requests/);
});

test('admin search panels use a full-width keyword row with compact filters and bottom-right actions', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const styles = readSource('../styles/theme.css');

  assert.match(pages, /admin-search-panel/);
  assert.match(pages, /admin-search-field/);
  assert.match(pages, /admin-filter-options/);
  assert.match(pages, /admin-search-actions/);
  assert.match(styles, /\.admin-search-field/);
  assert.match(styles, /grid-column: 1 \/ -1/);
  assert.match(styles, /\.admin-filter-options/);
  assert.match(styles, /\.admin-search-actions/);
  assert.match(styles, /justify-self: end/);
});

test('admin coupon individual issue form is limited while issued coupon list stays visible for every event type', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const api = readSource('../api/adminApi.js');

  assert.match(pages, /selectedIsAdminIndividualCoupon/);
  assert.match(pages, /selectedCanIndividuallyIssue/);
  assert.match(pages, /selectedCanIndividuallyIssue \?/);
  assert.match(pages, /requestCouponEventCreateApproval/);
  assert.match(pages, /requestCouponEventIndividualIssue/);
  assert.match(api, /coupon-events\/\$\{eventId\}\/issue-requests/);
  assert.doesNotMatch(pages, /createCouponEvent/);
  assert.doesNotMatch(pages, /issueCouponEventToUsers/);
  assert.match(pages, /const issuedCoupons = await getAdminCouponEventCoupons\(eventId\)/);
  assert.match(pages, /setCoupons\(getList\(issuedCoupons\)\)/);
  assert.match(pages, /coupons\.length === 0 \? <EmptyState title="발급된 쿠폰이 없습니다" \/> : null/);
  assert.doesNotMatch(pages, /eventDetail\.type === 'ADMIN_INDIVIDUAL'\s*\?\s*await getAdminCouponEventCoupons/);
  assert.doesNotMatch(api, /export const createCouponEvent/);
  assert.doesNotMatch(api, /export const issueCouponEventToUsers/);
});

test('admin coupon management opens list-first with search filters and separate create request form', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const styles = readSource('../styles/theme.css');

  assert.match(pages, /couponFilters/);
  assert.match(pages, /appliedCouponFilters/);
  assert.match(pages, /filteredCouponEvents/);
  assert.match(pages, /status: ''/);
  assert.match(pages, /getAdminCouponEvents\(\{ status: appliedCouponFilters\.status \}\)/);
  assert.match(pages, /PENDING_APPROVAL/);
  assert.match(pages, /REJECTED/);
  assert.match(pages, /handleCouponSearch/);
  assert.match(pages, /handleCouponReset/);
  assert.match(pages, /showCouponCreateForm/);
  assert.match(pages, /openCouponCreateForm/);
  assert.match(pages, /admin-coupon-list-grid/);
  assert.match(pages, /admin-coupon-create-panel/);
  assert.match(pages, /이벤트 생성 요청/);
  assert.match(pages, /생성 승인요청/);
  assert.match(pages, /요청 사유/);
  assert.match(pages, /요청 요약/);
  assert.match(styles, /\.admin-coupon-list-grid/);
  assert.ok(pages.indexOf('admin-coupon-list-grid') < pages.indexOf('admin-coupon-create-panel'));
});

test('admin coupon create request reloads the list without selecting a created event detail', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /requestCouponEventCreateApproval\(payload\)/);
  assert.match(pages, /couponApprovalMessage\('쿠폰 이벤트 생성'/);
  assert.match(pages, /setShowCouponCreateForm\(false\)/);
  assert.match(pages, /events\.reload\(\)/);
  assert.match(pages, /setSelectedEventId\(null\)/);
  assert.doesNotMatch(pages, /loadEventDetail\(.*created/);
  assert.doesNotMatch(pages, /loadEventDetail\(.*approval/);
});

test('admin coupon individual issue request displays approval summary fields and avoids completed wording', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /couponIssueSummary/);
  assert.match(pages, /CouponApprovalSummary/);
  assert.match(pages, /inputCount/);
  assert.match(pages, /validTargetCount/);
  assert.match(pages, /duplicateCount/);
  assert.match(pages, /excludedCount/);
  assert.match(pages, /plannedIssueCount/);
  assert.match(pages, /expectedIssuedQuantity/);
  assert.match(pages, /exceedsRemainingQuantity/);
  assert.match(pages, /쿠폰 개별발급 승인요청/);
  assert.doesNotMatch(pages, /즉시 발급 완료|발급 완료 문구/);
});

test('admin coupon individual issue success keeps approval summary visible after detail reload', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /loadEventDetail = async \(eventId, options = \{\}\)/);
  assert.match(pages, /preserveIssueSummary/);
  assert.match(pages, /if \(!options\.preserveIssueSummary\) \{\s*setCouponIssueSummary\(null\);/);
  assert.match(pages, /loadEventDetail\(selectedEventId, \{ preserveIssueSummary: true \}\)/);
  assert.match(pages, /setCouponIssueSummary\(summary\)/);
  assert.ok(
    pages.indexOf('loadEventDetail(selectedEventId, { preserveIssueSummary: true })') < pages.indexOf('setCouponIssueSummary(summary)')
  );
  assert.match(pages, /onClick=\{\(\) => loadEventDetail\(eventId\)\}/);
});

test('admin coupon detail load clears stale individual issue and stop request inputs', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const loadDetailStart = pages.indexOf('const loadEventDetail = async (eventId, options = {}) => {');
  const loadDetailTry = pages.indexOf('try {', loadDetailStart);
  const loadDetailSetup = pages.slice(loadDetailStart, loadDetailTry);

  assert.match(loadDetailSetup, /setUserIds\(''\)/);
  assert.match(loadDetailSetup, /setStopCause\(''\)/);
  assert.match(loadDetailSetup, /setStopCauseError\(''\)/);
  assert.match(loadDetailSetup, /setIssueInputError\(''\)/);
  assert.match(loadDetailSetup, /if \(!options\.preserveIssueSummary\) \{\s*setCouponIssueSummary\(null\);/);
  assert.ok(loadDetailSetup.indexOf("setUserIds('')") < loadDetailSetup.indexOf('if (!options.preserveIssueSummary)'));
  assert.ok(loadDetailSetup.indexOf("setStopCause('')") < loadDetailSetup.indexOf('if (!options.preserveIssueSummary)'));
});

test('admin coupon detail load keeps B detail when an earlier A request resolves later', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const loadDetailStart = pages.indexOf('const loadEventDetail = async (eventId, options = {}) => {');
  const loadDetailEnd = pages.indexOf('const handleCreate =', loadDetailStart);
  const loadDetail = pages.slice(loadDetailStart, loadDetailEnd);

  assert.match(pages, /import \{ useCallback, useEffect, useRef, useState \} from 'react';/);
  assert.match(pages, /const detailLoadSeqRef = useRef\(0\);/);
  assert.match(loadDetail, /const loadSeq = detailLoadSeqRef\.current \+ 1;\s*detailLoadSeqRef\.current = loadSeq;/);
  assert.match(loadDetail, /const isLatestDetailLoad = \(\) => detailLoadSeqRef\.current === loadSeq;/);
  assert.match(loadDetail, /const eventDetail = await getAdminCouponEvent\(eventId\);\s*const issuedCoupons = await getAdminCouponEventCoupons\(eventId\);\s*if \(!isLatestDetailLoad\(\)\) \{\s*return;\s*\}\s*setDetail\(eventDetail\);\s*setCoupons\(getList\(issuedCoupons\)\);/);
  assert.match(loadDetail, /catch \(error\) \{\s*if \(!isLatestDetailLoad\(\)\) \{\s*return;\s*\}\s*setDetail\(null\);\s*setCoupons\(\[\]\);\s*setDetailError/);
  assert.match(loadDetail, /finally \{\s*if \(isLatestDetailLoad\(\)\) \{\s*setDetailLoading\(false\);\s*\}\s*\}/);
});

test('admin coupon approval messages keep request wording even when backend returns APPROVED', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /couponApprovalMessage/);
  assert.match(pages, /APPROVED/);
  assert.match(pages, /승인 이력으로 등록되었습니다|승인요청이 접수되었습니다/);
  assert.doesNotMatch(pages, /isApprovalImmediatelyProcessed/);
  assert.doesNotMatch(pages, /즉시 승인되어 처리되었습니다|즉시 발급|발급 완료/);
  assert.doesNotMatch(pages, /APPROVED[\s\S]{0,160}즉시|즉시[\s\S]{0,160}APPROVED/);
});

test('admin coupon active event stop request requires a reason and uses approval API', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const api = readSource('../api/adminApi.js');

  assert.match(api, /requestCouponEventStopApproval/);
  assert.match(api, /coupon-events\/\$\{eventId\}\/stop-requests/);
  assert.doesNotMatch(api, /export const requestCouponEventStop =/);
  assert.match(pages, /requestCouponEventStopApproval/);
  assert.match(pages, /stopCause/);
  assert.match(pages, /stopCauseError/);
  assert.match(pages, /selectedCanRequestStop/);
  assert.match(pages, /handleStopRequest/);
  assert.match(pages, /이벤트 중단 요청/);
  assert.match(pages, /중단 사유/);
  assert.match(pages, /couponApprovalMessage\('쿠폰 이벤트 중단'/);
});

test('admin rejected coupon create approvals can refill the create form for resubmission', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /couponCreateApprovalRequests/);
  assert.match(pages, /rejectedCouponCreateApprovals/);
  assert.match(pages, /openCouponRetryForm/);
  assert.match(pages, /수정하여 재요청/);
  assert.match(pages, /couponPayload/);
  assert.match(pages, /couponApprovalToForm/);
  assert.match(pages, /setCreateCause\(request\.reason \|\| ''\)/);
  assert.match(pages, /payload\.reason = reason/);
  assert.match(pages, /requestCouponEventCreateApproval\(payload\)/);
});
