import apiClient from './client.js';

export const preparePayment = (tradeId, config = {}) =>
  apiClient.post(`/api/payments/trades/${tradeId}/prepare`, null, config);

export const confirmPayment = (paymentId, payload, config = {}) =>
  apiClient.post(`/api/payments/${paymentId}/confirm`, payload, config);

export const refundPayment = (paymentId, payload, config = {}) =>
  apiClient.post(`/api/payments/${paymentId}/refund`, payload, config);

export const getRefundStatus = (paymentId, config = {}) => apiClient.get(`/api/payments/${paymentId}/refund`, config);
