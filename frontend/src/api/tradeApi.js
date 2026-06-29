import apiClient from './client.js';

export const startTrade = (productId, config = {}) => apiClient.post(`/api/trades/products/${productId}`, null, config);

export const getTradeDetail = (tradeId, config = {}) => apiClient.get(`/api/trades/${tradeId}`, config);

export const completeTrade = (tradeId, config = {}) => apiClient.post(`/api/trades/${tradeId}/complete`, null, config);

export const expireReservation = (tradeId, config = {}) =>
  apiClient.post(`/api/trades/${tradeId}/expire-reservation`, null, config);

export const requestRatingMessage = (tradeId, config = {}) =>
  apiClient.post(`/api/trades/${tradeId}/rating-request-message`, null, config);
