import apiClient from './client.js';

const compactParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));

export const getMe = (config = {}) => apiClient.get('/api/users/me', config);

export const getSmileScore = (userId, config = {}) => apiClient.get(`/api/users/${userId}/smile-score`, config);

export const updateProfile = ({ nickname, phone }, config = {}) =>
  apiClient.patch('/api/users/me/profile', { nickname, phone }, config);

export const changePassword = ({ currentPassword, newPassword }, config = {}) =>
  apiClient.patch('/api/users/me/password', { currentPassword, newPassword }, config);

export const getMyTrades = (params = {}, config = {}) =>
  apiClient.get('/api/users/me/trades', { ...config, params: compactParams(params) });

export const getMyReviews = (params = {}, config = {}) =>
  apiClient.get('/api/users/me/reviews', { ...config, params: compactParams(params) });
