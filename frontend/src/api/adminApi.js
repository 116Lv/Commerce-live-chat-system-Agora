import apiClient from './client.js';

const compactParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));

const parsePayloadNumber = (value) => {
  if (value === undefined || value === null || value === '') {
    return value;
  }

  const normalized = String(value).replace(/[^\d.-]/g, '');
  return normalized ? Number(normalized) : value;
};

const normalizeUserIds = (userIds = []) => {
  const seen = new Set();

  return userIds
    .map((userId) => Number(userId))
    .filter((userId) => Number.isSafeInteger(userId) && userId > 0)
    .filter((userId) => {
      if (seen.has(userId)) {
        return false;
      }

      seen.add(userId);
      return true;
    });
};

export const normalizeCouponEventPayload = (event = {}) => {
  const payload = { ...event };

  if (event.name !== undefined) {
    payload.name = String(event.name).trim();
  }

  ['totalQuantity', 'discountAmount', 'minOrderAmount', 'validDays'].forEach((key) => {
    if (payload[key] !== undefined) {
      payload[key] = parsePayloadNumber(payload[key]);
    }
  });

  return payload;
};

export const normalizeCouponIssuePayload = (userIds = []) => ({ userIds: normalizeUserIds(userIds) });

export const getAdminMe = (config = {}) => apiClient.get('/api/admin/me', config);

export const getAdminDashboard = (config = {}) => apiClient.get('/api/admin/dashboard', config);

export const getAdminProducts = (params = {}, config = {}) =>
  apiClient.get('/api/admin/products', {
    ...config,
    params: compactParams({ reportedOnly: false, page: 0, size: 20, ...params })
  });

export const hideAdminProduct = (productId, config = {}) =>
  apiClient.patch(`/api/admin/products/${productId}/hide`, undefined, config);

export const approveAdminProduct = (productId, config = {}) =>
  apiClient.patch(`/api/admin/products/${productId}/approve`, undefined, config);

export const getAdminUsers = (params = {}, config = {}) =>
  apiClient.get('/api/admin/users', { ...config, params: compactParams({ page: 0, size: 20, ...params }) });

export const updateAdminUserStatus = (userId, status, config = {}) =>
  apiClient.patch(`/api/admin/users/${userId}/status`, { status }, config);

export const getAdminAccounts = (config = {}) => apiClient.get('/api/admin/accounts', config);

export const updateAdminAccountRole = (userId, role, config = {}) =>
  apiClient.patch(`/api/admin/accounts/${userId}/role`, { role }, config);

export const getAdminApprovalRequests = (params = {}, config = {}) =>
  apiClient.get('/api/admin/approval-requests', { ...config, params: compactParams(params) });

export const getAdminApprovalRequest = (requestId, config = {}) =>
  apiClient.get(`/api/admin/approval-requests/${requestId}`, config);

export const getMyAdminApprovalRequests = (config = {}) => apiClient.get('/api/admin/my-approval-requests', config);

export const requestAdminRoleChangeApproval = (request, config = {}) =>
  apiClient.post('/api/admin/my-approval-requests/role-change', request, config);

export const approveAdminApprovalRequest = (requestId, memo = '', config = {}) =>
  apiClient.post(`/api/admin/approval-requests/${requestId}/approve`, { memo }, config);

export const rejectAdminApprovalRequest = (requestId, memo = '', config = {}) =>
  apiClient.post(`/api/admin/approval-requests/${requestId}/reject`, { memo }, config);

export const getAdminUserReports = (params = {}, config = {}) =>
  apiClient.get('/api/admin/reports/users', { ...config, params: compactParams(params) });

export const getAdminProductReports = (params = {}, config = {}) =>
  apiClient.get('/api/admin/reports/products', { ...config, params: compactParams(params) });

export const resolveAdminUserReport = (reportId, adminMemo, config = {}) =>
  apiClient.post(`/api/admin/reports/users/${reportId}/resolve`, { adminMemo }, config);

export const resolveAdminProductReport = (reportId, adminMemo, config = {}) =>
  apiClient.post(`/api/admin/reports/products/${reportId}/resolve`, { adminMemo }, config);

export const getAdminPayments = (params = {}, config = {}) =>
  apiClient.get('/api/admin/payments', { ...config, params: { status: '', page: 0, size: 20, ...params } });

export const verifyAdminPayment = (paymentId, config = {}) =>
  apiClient.post(`/api/admin/payments/${paymentId}/verify`, null, config);

export const getAdminRefunds = (config = {}) => apiClient.get('/api/admin/refunds', config);

export const settleAdminSettlement = (settlementId, config = {}) =>
  apiClient.post(`/api/admin/settlements/${settlementId}/settle`, null, config);

export const getAdminCouponEvents = (params = {}, config = {}) =>
  apiClient.get('/api/admin/coupon-events', { ...config, params: compactParams(params) });

export const requestCouponEventCreateApproval = (event, config = {}) =>
  apiClient.post('/api/admin/coupon-events/requests', normalizeCouponEventPayload(event), config);

export const getAdminCouponEvent = (eventId, config = {}) => apiClient.get(`/api/admin/coupon-events/${eventId}`, config);

export const requestCouponEventStopApproval = (eventId, reason, config = {}) =>
  apiClient.post(`/api/admin/coupon-events/${eventId}/stop-requests`, { reason }, config);

export const requestCouponEventIndividualIssue = (eventId, userIds, config = {}) =>
  apiClient.post(`/api/admin/coupon-events/${eventId}/issue-requests`, normalizeCouponIssuePayload(userIds), config);

export const getAdminCouponEventCoupons = (eventId, config = {}) =>
  apiClient.get(`/api/admin/coupon-events/${eventId}/coupons`, config);
