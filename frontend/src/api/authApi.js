import apiClient from './client.js';

export const loginUser = ({ email, password }) =>
  apiClient.post('/api/auth/login', { email, password }, { authType: 'user' });

export const signupUser = ({ email, password, nickname }) =>
  apiClient.post('/api/auth/signup', { email, password, nickname }, { authType: 'user' });

export const logoutUser = () => apiClient.post('/api/auth/logout', null, { authType: 'user' });

export const loginAdmin = ({ email, password }) =>
  apiClient.post('/api/admin/auth/login', { email, password }, { authType: 'admin' });

export const logoutAdmin = () => apiClient.post('/api/admin/auth/logout', null, { authType: 'admin' });
