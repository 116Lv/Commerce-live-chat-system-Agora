import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  approveAdminApprovalRequest,
  approveAdminProduct,
  getAdminApprovalRequests,
  getAdminApprovalRequest,
  getAdminAccounts,
  getAdminCouponEvent,
  getAdminCouponEventCoupons,
  getAdminCouponEvents,
  getAdminDashboard,
  getAdminMe,
  getAdminPayments,
  getAdminProducts,
  getAdminProductReports,
  getAdminRefunds,
  getMyAdminApprovalRequests,
  getAdminUserReports,
  getAdminUsers,
  hideAdminProduct,
  normalizeCouponEventPayload,
  normalizeCouponIssuePayload,
  requestCouponEventCreateApproval,
  requestCouponEventIndividualIssue,
  requestCouponEventStopApproval,
  rejectAdminApprovalRequest,
  requestAdminRoleChangeApproval,
  resolveAdminProductReport,
  resolveAdminUserReport,
  settleAdminSettlement,
  updateAdminAccountRole,
  updateAdminUserStatus,
  verifyAdminPayment
} from './adminApi.js';
import { canSettlePayment, getReportTabs, replaceById } from '../pages/adminPageUtils.js';

const captureRequest = () => {
  const requests = [];
  const adapter = (config) => {
    requests.push(config);
    return Promise.resolve({
      config,
      data: { status: 'SUCCESS', message: 'ok', data: { ok: true } },
      headers: {},
      status: 200,
      statusText: 'OK'
    });
  };

  return { requests, config: { adapter } };
};

const parseJsonBody = (data) => (typeof data === 'string' ? JSON.parse(data) : data);
const summarizeRequest = ({ method, url, params, data }) => ({ method, url, params, data: parseJsonBody(data) });

test('admin API maps dashboard, product, user, report, payment, settlement, and coupon endpoints', async () => {
  const { requests, config } = captureRequest();
  const couponEvent = {
    type: 'WELCOME',
    name: '신규 쿠폰',
    startAt: '2026-06-29T09:00',
    endAt: '2026-07-29T09:00',
    totalQuantity: 100,
    discountAmount: 3000,
    minOrderAmount: 10000,
    validDays: 30,
    reason: 'Campaign launch approval'
  };

  await getAdminMe(config);
  await getAdminDashboard(config);
  await getAdminProducts({ reportedOnly: true, approvalStatus: 'REJECTED', page: 1, size: 10 }, config);
  await hideAdminProduct(7, config);
  await approveAdminProduct(7, config);
  await getAdminUsers({ page: 2, size: 5 }, config);
  await getAdminAccounts(config);
  await updateAdminUserStatus(9, 'SUSPENDED', config);
  await updateAdminAccountRole(9, 'PRODUCT_ADMIN', config);
  await getAdminApprovalRequests({ status: 'PENDING' }, config);
  await getAdminApprovalRequest(20, config);
  await getMyAdminApprovalRequests(config);
  await requestAdminRoleChangeApproval({ targetAdminId: 9, requestedRole: 'SETTLEMENT_ADMIN', reason: 'Need backup' }, config);
  await approveAdminApprovalRequest(21, 'Approved', config);
  await rejectAdminApprovalRequest(22, 'Missing reason', config);
  await getAdminUserReports({ status: 'PENDING', search: 'seller' }, config);
  await getAdminProductReports({ status: 'RESOLVED' }, config);
  await resolveAdminUserReport(11, '처리 완료', config);
  await resolveAdminProductReport(12, '상품 숨김', config);
  await getAdminPayments({ status: 'PAID', page: 0, size: 20 }, config);
  await getAdminPayments({ status: '', page: 0, size: 20 }, config);
  await verifyAdminPayment(15, config);
  await getAdminRefunds(config);
  await settleAdminSettlement(17, config);
  await getAdminCouponEvents({}, config);
  await getAdminCouponEvents({ status: 'PENDING_APPROVAL' }, config);
  await requestCouponEventCreateApproval(couponEvent, config);
  await getAdminCouponEvent(19, config);
  await requestCouponEventStopApproval(19, 'Campaign ended', config);
  await requestCouponEventIndividualIssue(19, [1, 2], config);
  await getAdminCouponEventCoupons(19, config);

  assert.deepEqual(requests.map(summarizeRequest), [
    { method: 'get', url: '/api/admin/me', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/dashboard', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/products', params: { reportedOnly: true, approvalStatus: 'REJECTED', page: 1, size: 10 }, data: undefined },
    { method: 'patch', url: '/api/admin/products/7/hide', params: undefined, data: undefined },
    { method: 'patch', url: '/api/admin/products/7/approve', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/users', params: { page: 2, size: 5 }, data: undefined },
    { method: 'get', url: '/api/admin/accounts', params: undefined, data: undefined },
    { method: 'patch', url: '/api/admin/users/9/status', params: undefined, data: { status: 'SUSPENDED' } },
    { method: 'patch', url: '/api/admin/accounts/9/role', params: undefined, data: { role: 'PRODUCT_ADMIN' } },
    { method: 'get', url: '/api/admin/approval-requests', params: { status: 'PENDING' }, data: undefined },
    { method: 'get', url: '/api/admin/approval-requests/20', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/my-approval-requests', params: undefined, data: undefined },
    {
      method: 'post',
      url: '/api/admin/my-approval-requests/role-change',
      params: undefined,
      data: { targetAdminId: 9, requestedRole: 'SETTLEMENT_ADMIN', reason: 'Need backup' }
    },
    { method: 'post', url: '/api/admin/approval-requests/21/approve', params: undefined, data: { memo: 'Approved' } },
    { method: 'post', url: '/api/admin/approval-requests/22/reject', params: undefined, data: { memo: 'Missing reason' } },
    { method: 'get', url: '/api/admin/reports/users', params: { status: 'PENDING', search: 'seller' }, data: undefined },
    { method: 'get', url: '/api/admin/reports/products', params: { status: 'RESOLVED' }, data: undefined },
    { method: 'post', url: '/api/admin/reports/users/11/resolve', params: undefined, data: { adminMemo: '처리 완료' } },
    { method: 'post', url: '/api/admin/reports/products/12/resolve', params: undefined, data: { adminMemo: '상품 숨김' } },
    { method: 'get', url: '/api/admin/payments', params: { status: 'PAID', page: 0, size: 20 }, data: undefined },
    { method: 'get', url: '/api/admin/payments', params: { status: '', page: 0, size: 20 }, data: undefined },
    { method: 'post', url: '/api/admin/payments/15/verify', params: undefined, data: null },
    { method: 'get', url: '/api/admin/refunds', params: undefined, data: undefined },
    { method: 'post', url: '/api/admin/settlements/17/settle', params: undefined, data: null },
    { method: 'get', url: '/api/admin/coupon-events', params: {}, data: undefined },
    { method: 'get', url: '/api/admin/coupon-events', params: { status: 'PENDING_APPROVAL' }, data: undefined },
    { method: 'post', url: '/api/admin/coupon-events/requests', params: undefined, data: couponEvent },
    { method: 'get', url: '/api/admin/coupon-events/19', params: undefined, data: undefined },
    { method: 'post', url: '/api/admin/coupon-events/19/stop-requests', params: undefined, data: { reason: 'Campaign ended' } },
    { method: 'post', url: '/api/admin/coupon-events/19/issue-requests', params: undefined, data: { userIds: [1, 2] } },
    { method: 'get', url: '/api/admin/coupon-events/19/coupons', params: undefined, data: undefined }
  ]);
});

test('coupon approval wrappers preserve backend approval and summary response shape', async () => {
  const approvalResponse = {
    status: 'SUCCESS',
    message: 'ok',
    data: {
      id: 10,
      operation: 'COUPON_EVENT_CREATE',
      status: 'PENDING',
      couponPayload: {
        couponEventId: 31,
        eventType: 'ADMIN_INDIVIDUAL',
        eventName: 'VIP coupon',
        inputCount: 0,
        validTargetCount: 0,
        duplicateCount: 0,
        excludedCount: 0,
        plannedIssueCount: 0,
        expectedIssuedQuantity: 0,
        exceedsRemainingQuantity: false
      }
    }
  };
  const summaryResponse = {
    status: 'SUCCESS',
    message: 'ok',
    data: {
      approvalRequestId: 11,
      status: 'PENDING',
      eventId: 31,
      inputCount: 3,
      validTargetCount: 2,
      duplicateCount: 1,
      excludedCount: 0,
      plannedIssueCount: 2,
      expectedIssuedQuantity: 2,
      exceedsRemainingQuantity: false
    }
  };
  const responses = [approvalResponse, approvalResponse, summaryResponse];
  const adapter = (config) =>
    Promise.resolve({
      config,
      data: responses.shift(),
      headers: {},
      status: 200,
      statusText: 'OK'
    });

  const createResult = await requestCouponEventCreateApproval({ type: 'FIRST_COME', name: 'Coupon' }, { adapter });
  const stopResult = await requestCouponEventStopApproval(31, 'Campaign ended', { adapter });
  const issueResult = await requestCouponEventIndividualIssue(31, [7, 8, 7], { adapter });

  assert.equal(createResult.operation, 'COUPON_EVENT_CREATE');
  assert.equal(stopResult.couponPayload.couponEventId, 31);
  assert.deepEqual(issueResult, summaryResponse.data);
});

test('admin page row replacement updates the item returned by an action', () => {
  const rows = [
    { id: 1, title: 'A', status: 'AVAILABLE' },
    { id: 2, title: 'B', status: 'AVAILABLE' }
  ];

  const updated = replaceById(rows, { id: 2, title: 'B', status: 'HIDDEN' }, 'id');

  assert.deepEqual(updated, [
    { id: 1, title: 'A', status: 'AVAILABLE' },
    { id: 2, title: 'B', status: 'HIDDEN' }
  ]);
});

test('report tabs are scoped to admin report permissions', () => {
  assert.deepEqual(getReportTabs({ role: 'USER_ADMIN' }).map((tab) => tab.key), ['users']);
  assert.deepEqual(getReportTabs({ role: 'PRODUCT_ADMIN' }).map((tab) => tab.key), ['products']);
  assert.deepEqual(getReportTabs({ role: 'ROOT_ADMIN' }).map((tab) => tab.key), ['users', 'products']);
});

test('settlement action is available only when payment rows expose settlementId', () => {
  assert.equal(canSettlePayment({ paymentId: 1, settlementId: 11, settlementStatus: 'READY' }), true);
  assert.equal(canSettlePayment({ paymentId: 2 }), false);
  assert.equal(canSettlePayment({ paymentId: 3, settlementId: 12, settlementStatus: 'HELD' }), false);
  assert.equal(canSettlePayment({ paymentId: 4, settlementId: 13, settlementStatus: 'FAILED' }), false);
  assert.equal(canSettlePayment({ paymentId: 5, settlementId: 14, settlementStatus: 'SETTLED' }), false);
});

test('admin list APIs include backend default paging and filter params', async () => {
  const { requests, config } = captureRequest();

  await getAdminProducts({}, config);
  await getAdminProducts(
    {
      keyword: '의자',
      sellerKeyword: 'seller01',
      status: 'AVAILABLE',
      approvalStatus: 'PENDING',
      reportedOnly: true,
      page: 2,
      size: 30
    },
    config
  );
  await getAdminUsers({}, config);
  await getAdminPayments({}, config);

  assert.deepEqual(requests.map(summarizeRequest), [
    { method: 'get', url: '/api/admin/products', params: { reportedOnly: false, page: 0, size: 20 }, data: undefined },
    {
      method: 'get',
      url: '/api/admin/products',
      params: {
        reportedOnly: true,
        page: 2,
        size: 30,
        keyword: '의자',
        sellerKeyword: 'seller01',
        status: 'AVAILABLE',
        approvalStatus: 'PENDING'
      },
      data: undefined
    },
    { method: 'get', url: '/api/admin/users', params: { page: 0, size: 20 }, data: undefined },
    { method: 'get', url: '/api/admin/payments', params: { status: '', page: 0, size: 20 }, data: undefined }
  ]);
});

test('admin account and report APIs expose dedicated admin account list and optional report filters', async () => {
  const { requests, config } = captureRequest();

  await getAdminAccounts(config);
  await getAdminUserReports({ status: 'PENDING', search: 'blocked' }, config);
  await getAdminProductReports({ status: '', search: '' }, config);

  assert.deepEqual(requests.map(summarizeRequest), [
    { method: 'get', url: '/api/admin/accounts', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/reports/users', params: { status: 'PENDING', search: 'blocked' }, data: undefined },
    { method: 'get', url: '/api/admin/reports/products', params: {}, data: undefined }
  ]);
});

test('coupon API normalizes formatted admin UI numbers while preserving raw enum values', async () => {
  const { requests, config } = captureRequest();

  await requestCouponEventCreateApproval(
    {
      type: 'ADMIN_INDIVIDUAL',
      name: ' 여름 쿠폰 ',
      startAt: '2026-07-01T09:00',
      endAt: '2026-07-31T23:00',
      totalQuantity: '1,000',
      discountAmount: '5,000',
      minOrderAmount: '10,000',
      validDays: '30',
      reason: 'Campaign launch approval'
    },
    config
  );
  await requestCouponEventIndividualIssue(31, ['1', 2, 'abc', 3], config);

  assert.deepEqual(requests.map(summarizeRequest), [
    {
      method: 'post',
      url: '/api/admin/coupon-events/requests',
      params: undefined,
      data: {
        type: 'ADMIN_INDIVIDUAL',
        name: '여름 쿠폰',
        startAt: '2026-07-01T09:00',
        endAt: '2026-07-31T23:00',
        totalQuantity: 1000,
        discountAmount: 5000,
        minOrderAmount: 10000,
        validDays: 30,
        reason: 'Campaign launch approval'
      }
    },
    { method: 'post', url: '/api/admin/coupon-events/31/issue-requests', params: undefined, data: { userIds: [1, 2, 3] } }
  ]);

  assert.deepEqual(normalizeCouponEventPayload({ type: 'FIRST_COME', totalQuantity: '2,500' }), {
    type: 'FIRST_COME',
    totalQuantity: 2500
  });
  assert.deepEqual(normalizeCouponIssuePayload(['5', 'x', 6]), { userIds: [5, 6] });
});

test('account role update surfaces backend authorization errors', async () => {
  const adapter = (config) =>
    Promise.reject({
      config,
      response: {
        status: 403,
        data: { status: 'ERROR', message: 'ROOT_ADMIN만 변경할 수 있습니다.', data: null }
      }
    });

  await assert.rejects(
    () => updateAdminAccountRole(3, 'USER_ADMIN', { adapter }),
    /ROOT_ADMIN만 변경할 수 있습니다/
  );
});
