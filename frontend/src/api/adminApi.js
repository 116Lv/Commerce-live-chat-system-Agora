import apiClient from './client.js';

const compactParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));

export const getAdminMe = (config = {}) => apiClient.get('/api/admin/me', config);

export const getAdminDashboard = (config = {}) => apiClient.get('/api/admin/dashboard', config);

export const getAdminProducts = (params = {}, config = {}) =>
  apiClient.get('/api/admin/products', {
    ...config,
    params: compactParams({ reportedOnly: false, page: 0, size: 20, ...params })
  });

export const hideAdminProduct = (productId, config = {}) =>
  apiClient.patch(`/api/admin/products/${productId}/hide`, undefined, config);

export const getAdminUsers = (params = {}, config = {}) =>
  apiClient.get('/api/admin/users', { ...config, params: compactParams({ page: 0, size: 20, ...params }) });

export const updateAdminUserStatus = (userId, status, config = {}) =>
  apiClient.patch(`/api/admin/users/${userId}/status`, { status }, config);

export const getAdminUserReports = (config = {}) => apiClient.get('/api/admin/reports/users', config);

export const getAdminProductReports = (config = {}) => apiClient.get('/api/admin/reports/products', config);

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

export const getAdminCouponEvents = (config = {}) => apiClient.get('/api/admin/coupon-events', config);

export const createCouponEvent = (event, config = {}) => apiClient.post('/api/admin/coupon-events', event, config);

export const getAdminCouponEvent = (eventId, config = {}) => apiClient.get(`/api/admin/coupon-events/${eventId}`, config);

export const issueCouponEventToUsers = (eventId, userIds, config = {}) =>
  apiClient.post(`/api/admin/coupon-events/${eventId}/issue`, { userIds }, config);

export const getAdminCouponEventCoupons = (eventId, config = {}) =>
  apiClient.get(`/api/admin/coupon-events/${eventId}/coupons`, config);
