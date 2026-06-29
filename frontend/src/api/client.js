import axios from 'axios';
import { getAdminToken, getUserToken } from '../auth/tokenStorage.js';

const DEFAULT_API_BASE_URL = 'http://127.0.0.1:8080';
const API_RESPONSE_KEYS = ['status', 'message', 'data'];

export const apiClient = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || DEFAULT_API_BASE_URL
});

const isApiResponse = (value) =>
  value &&
  typeof value === 'object' &&
  API_RESPONSE_KEYS.every((key) => Object.prototype.hasOwnProperty.call(value, key));

const isAdminUrl = (url = '') => {
  if (url.startsWith('/api/admin')) {
    return true;
  }

  try {
    return new URL(url, DEFAULT_API_BASE_URL).pathname.startsWith('/api/admin');
  } catch {
    return false;
  }
};

export const selectAuthToken = (config = {}) => {
  if (config.authType === 'none') {
    return null;
  }

  const authType = config.authType || (isAdminUrl(config.url) ? 'admin' : 'user');

  return authType === 'admin' ? getAdminToken() : getUserToken();
};

export const formatAuthorizationHeader = (token) => {
  const value = typeof token === 'string' ? token.trim() : '';

  if (!value) {
    return null;
  }

  return value.toLowerCase().startsWith('bearer ') ? value : `Bearer ${value}`;
};

export const stripAuthorizationHeaders = (headers) => {
  if (!headers) {
    return;
  }

  if (typeof headers.delete === 'function') {
    headers.delete('Authorization');
    headers.delete('authorization');
  }

  Object.keys(headers).forEach((key) => {
    if (key.toLowerCase() === 'authorization') {
      delete headers[key];
    }
  });
};

export const unwrapApiResponse = (response) => {
  const payload = response?.data;

  return isApiResponse(payload) ? payload.data : payload;
};

export const getApiErrorMessage = (error) => {
  const data = error?.response?.data;

  if (typeof data?.message === 'string' && data.message.trim()) {
    return data.message;
  }

  if (typeof data === 'string' && data.trim()) {
    return data;
  }

  return error?.message || 'Request failed';
};

apiClient.interceptors.request.use((config) => {
  const token = selectAuthToken(config);
  config.headers = config.headers ?? {};

  if (token) {
    config.headers.Authorization = formatAuthorizationHeader(token);
  } else if (config.authType === 'none') {
    stripAuthorizationHeaders(config.headers);
  }

  delete config.authType;

  return config;
});

apiClient.interceptors.response.use(
  (response) => unwrapApiResponse(response),
  (error) => {
    const apiError = new Error(getApiErrorMessage(error));
    apiError.status = error?.response?.status;
    apiError.data = error?.response?.data;
    apiError.cause = error;

    return Promise.reject(apiError);
  }
);

export default apiClient;
