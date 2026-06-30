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
  assert.match(api, /approveAdminApprovalRequest/);
  assert.match(api, /rejectAdminApprovalRequest/);
  assert.match(api, /approveAdminProduct/);
  assert.match(pages, /approvalStatus/);
  assert.match(pages, /selectedProduct/);
  assert.match(pages, /approveAdminProduct/);
  assert.match(pages, /requestAdminRoleChangeApproval/);
  assert.match(pages, /value="PENDING"/);
  assert.match(pages, /APPROVED/);
  assert.match(pages, /REJECTED/);
  assert.match(pages, /PENDING/);
});
