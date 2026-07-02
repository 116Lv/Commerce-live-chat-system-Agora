import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf8');

test('admin shell renders topbar account menu and permission-filtered Korean navigation', () => {
  const source = readSource('../layouts/AdminLayout.jsx');

  assert.match(source, /getAdminMe/);
  assert.match(source, /useAuth/);
  assert.match(source, /admin-topbar/);
  assert.match(source, /admin-avatar/);
  assert.match(source, /requiredPermissions/);
  assert.match(source, /USER_MANAGE/);
  assert.match(source, /REPORT_MANAGE/);
  assert.match(source, /PRODUCT_MANAGE/);
  assert.match(source, /PAYMENT_MANAGE/);
  assert.match(source, /COUPON_MANAGE/);
  assert.match(source, /ADMIN_ACCOUNT_MANAGE/);
  assert.match(source, /APPROVAL_MANAGE/);
  assert.doesNotMatch(source, /allowedRoles/);
  assert.match(source, /대시보드/);
  assert.match(source, /상품 관리/);
  assert.match(source, /회원 관리/);
  assert.match(source, /신고 관리/);
  assert.match(source, /결제 관리/);
  assert.match(source, /쿠폰 관리/);
  assert.match(source, /관리자 계정/);
  assert.match(source, /승인 관리/);
  assert.match(source, /내 승인 요청/);
  assert.doesNotMatch(
    source,
    /label: 'Dashboard'|label: 'Products'|label: 'Users'|label: 'Reports'|label: 'Payments'|label: 'Coupons'|label: 'Approval management'|label: 'My approval requests'/
  );
});

test('admin pages move admin role changes to the account management page', () => {
  const source = readSource('./AdminPlaceholderPages.jsx');

  assert.match(source, /AdminAccountsPage/);
  assert.match(source, /getAdminAccounts/);
  assert.match(source, /updateAdminAccountRole/);
  assert.doesNotMatch(source, /requestAdminRoleChangeApproval/);
  assert.match(source, /관리자 계정 관리/);
  assert.doesNotMatch(source, /const handleRole = \(user, role\)/);
});

test('admin reports expose status tabs, search, and readable actor labels', () => {
  const source = readSource('./AdminPlaceholderPages.jsx');
  const utils = readSource('./adminPageUtils.js');

  assert.match(source, /REPORT_STATUS_FILTERS/);
  assert.match(source, /reportSearch/);
  assert.match(source, /reporterNickname/);
  assert.match(source, /reportedUserNickname/);
  assert.match(source, /productTitle/);
  assert.match(source, /productSellerNickname/);
  assert.match(source, /productSellerId/);
  assert.match(source, /productPrice/);
  assert.match(source, /productStatus/);
  assert.match(source, /productApprovalStatus/);
  assert.match(source, /MoneyText amount={report\.productPrice}/);
  assert.match(source, /report\.adminMemo/);
  assert.match(source, /report\.resolvedAt/);
  assert.match(source, /formatDateTime\(report\.resolvedAt\)/);
  assert.match(utils, /productSellerNickname/);
  assert.match(utils, /productPrice/);
  assert.match(utils, /adminMemo/);
  assert.match(utils, /resolvedAt/);
  assert.match(utils, /대기/);
  assert.match(utils, /처리 완료/);
});

test('admin payments classify verification results with readable status text', () => {
  const source = readSource('./AdminPlaceholderPages.jsx');
  const utils = readSource('./adminPageUtils.js');

  assert.match(source, /paymentVerificationSummary/);
  assert.match(source, /verification-result/);
  assert.match(utils, /승인 완료/);
  assert.match(utils, /승인 대기/);
});
