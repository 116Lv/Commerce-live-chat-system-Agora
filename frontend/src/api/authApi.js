import apiClient from './client.js';

export const loginUser = ({ email, password }, config = {}) =>
  apiClient.post('/api/auth/login', { email, password }, { ...config, authType: 'none' });

export const signupUser = ({ email, password, nickname }, config = {}) =>
  apiClient.post('/api/auth/signup', { email, password, nickname }, { ...config, authType: 'none' });

export const logoutUser = (config = {}) => apiClient.post('/api/auth/logout', null, { ...config, authType: 'user' });

export const loginAdmin = ({ email, password }, config = {}) =>
  apiClient.post('/api/admin/auth/login', { email, password }, { ...config, authType: 'none' });

export const logoutAdmin = (config = {}) =>
  apiClient.post('/api/admin/auth/logout', null, { ...config, authType: 'admin' });
