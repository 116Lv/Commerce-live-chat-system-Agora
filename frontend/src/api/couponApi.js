import apiClient from './client.js';

export const getCouponEvents = (config = {}) => apiClient.get('/api/coupon-events', config);

export const issueCoupon = (eventId, config = {}) => apiClient.post(`/api/coupon-events/${eventId}/issue`, null, config);

export const getMyCoupons = (config = {}) => apiClient.get('/api/users/me/coupons', config);
