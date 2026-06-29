import apiClient from './client.js';

export const createReview = (review, config = {}) => apiClient.post('/api/reviews', review, config);

export const getTradeReviews = (tradeId, config = {}) => apiClient.get(`/api/trades/${tradeId}/reviews`, config);
