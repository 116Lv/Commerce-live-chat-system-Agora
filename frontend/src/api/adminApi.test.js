import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  createCouponEvent,
  getAdminCouponEvent,
  getAdminCouponEventCoupons,
  getAdminCouponEvents,
  getAdminDashboard,
  getAdminMe,
  getAdminPayments,
  getAdminProducts,
  getAdminProductReports,
  getAdminRefunds,
  getAdminUserReports,
  getAdminUsers,
  hideAdminProduct,
  issueCouponEventToUsers,
  resolveAdminProductReport,
  resolveAdminUserReport,
  settleAdminSettlement,
  updateAdminUserStatus,
  verifyAdminPayment
} from './adminApi.js';
import { replaceById } from '../pages/adminPageUtils.js';

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
    validDays: 30
  };

  await getAdminMe(config);
  await getAdminDashboard(config);
  await getAdminProducts({ reportedOnly: true, page: 1, size: 10 }, config);
  await hideAdminProduct(7, config);
  await getAdminUsers({ page: 2, size: 5 }, config);
  await updateAdminUserStatus(9, 'SUSPENDED', config);
  await getAdminUserReports(config);
  await getAdminProductReports(config);
  await resolveAdminUserReport(11, '처리 완료', config);
  await resolveAdminProductReport(12, '상품 숨김', config);
  await getAdminPayments({ status: 'PAID', page: 0, size: 20 }, config);
  await getAdminPayments({ status: '', page: 0, size: 20 }, config);
  await verifyAdminPayment(15, config);
  await getAdminRefunds(config);
  await settleAdminSettlement(17, config);
  await getAdminCouponEvents(config);
  await createCouponEvent(couponEvent, config);
  await getAdminCouponEvent(19, config);
  await issueCouponEventToUsers(19, [1, 2], config);
  await getAdminCouponEventCoupons(19, config);

  assert.deepEqual(requests.map(summarizeRequest), [
    { method: 'get', url: '/api/admin/me', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/dashboard', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/products', params: { reportedOnly: true, page: 1, size: 10 }, data: undefined },
    { method: 'patch', url: '/api/admin/products/7/hide', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/users', params: { page: 2, size: 5 }, data: undefined },
    { method: 'patch', url: '/api/admin/users/9/status', params: undefined, data: { status: 'SUSPENDED' } },
    { method: 'get', url: '/api/admin/reports/users', params: undefined, data: undefined },
    { method: 'get', url: '/api/admin/reports/products', params: undefined, data: undefined },
    { method: 'post', url: '/api/admin/reports/users/11/resolve', params: undefined, data: { adminMemo: '처리 완료' } },
    { method: 'post', url: '/api/admin/reports/products/12/resolve', params: undefined, data: { adminMemo: '상품 숨김' } },
    { method: 'get', url: '/api/admin/payments', params: { status: 'PAID', page: 0, size: 20 }, data: undefined },
    { method: 'get', url: '/api/admin/payments', params: { status: '', page: 0, size: 20 }, data: undefined },
    { method: 'post', url: '/api/admin/payments/15/verify', params: undefined, data: null },
    { method: 'get', url: '/api/admin/refunds', params: undefined, data: undefined },
    { method: 'post', url: '/api/admin/settlements/17/settle', params: undefined, data: null },
    { method: 'get', url: '/api/admin/coupon-events', params: undefined, data: undefined },
    { method: 'post', url: '/api/admin/coupon-events', params: undefined, data: couponEvent },
    { method: 'get', url: '/api/admin/coupon-events/19', params: undefined, data: undefined },
    { method: 'post', url: '/api/admin/coupon-events/19/issue', params: undefined, data: { userIds: [1, 2] } },
    { method: 'get', url: '/api/admin/coupon-events/19/coupons', params: undefined, data: undefined }
  ]);
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

test('admin list APIs include backend default paging and filter params', async () => {
  const { requests, config } = captureRequest();

  await getAdminProducts({}, config);
  await getAdminUsers({}, config);
  await getAdminPayments({}, config);

  assert.deepEqual(requests.map(summarizeRequest), [
    { method: 'get', url: '/api/admin/products', params: { reportedOnly: false, page: 0, size: 20 }, data: undefined },
    { method: 'get', url: '/api/admin/users', params: { page: 0, size: 20 }, data: undefined },
    { method: 'get', url: '/api/admin/payments', params: { status: '', page: 0, size: 20 }, data: undefined }
  ]);
});
