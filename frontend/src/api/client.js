import axios from 'axios';
import {
  clearUserTokens,
  getAdminToken,
  getUserRefreshToken,
  getUserToken,
  setUserRefreshToken,
  setUserToken
} from '../auth/tokenStorage.js';

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

const shouldTryUserTokenRefresh = (error) => {
  const config = error?.config ?? error?.response?.config;

  return (
    error?.response?.status === 401 &&
    config &&
    !config._authRetry &&
    !config.skipAuthRefresh &&
    config._authType !== 'none' &&
    config._authType !== 'admin' &&
    !isAdminUrl(config.url)
  );
};

const persistUserTokenPair = (tokens) => {
  if (!tokens?.accessToken || !tokens?.refreshToken) {
    clearUserTokens();
    throw new Error('Token reissue response did not include required tokens.');
  }

  setUserToken(tokens.accessToken);
  setUserRefreshToken(tokens.refreshToken);
};

let userTokenRefreshPromise = null;

const reissueStoredUserTokens = async (adapter) => {
  const refreshToken = getUserRefreshToken();

  if (!refreshToken) {
    clearUserTokens();
    throw new Error('No refresh token is available.');
  }

  const tokens = await apiClient.post(
    '/api/auth/reissue',
    { refreshToken },
    { authType: 'none', skipAuthRefresh: true, adapter }
  );

  persistUserTokenPair(tokens);

  return tokens;
};

const getSharedUserTokenRefresh = (adapter) => {
  if (!userTokenRefreshPromise) {
    userTokenRefreshPromise = reissueStoredUserTokens(adapter).finally(() => {
      userTokenRefreshPromise = null;
    });
  }

  return userTokenRefreshPromise;
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

const toApiError = (error) => {
  const apiError = new Error(getApiErrorMessage(error));
  apiError.status = error?.response?.status;
  apiError.data = error?.response?.data;
  apiError.cause = error;

  return apiError;
};

apiClient.interceptors.request.use((config) => {
  const authType = config.authType || (isAdminUrl(config.url) ? 'admin' : 'user');
  const token = selectAuthToken(config);
  config.headers = config.headers ?? {};
  config._authType = config.authType ?? authType;

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
  async (error) => {
    if (shouldTryUserTokenRefresh(error)) {
      const originalConfig = error.config ?? error.response.config;

      try {
        const tokens = await getSharedUserTokenRefresh(originalConfig.adapter);
        originalConfig._authRetry = true;
        originalConfig.headers = originalConfig.headers ?? {};
        originalConfig.headers.Authorization = formatAuthorizationHeader(tokens.accessToken);

        return apiClient(originalConfig);
      } catch (reissueError) {
        clearUserTokens();

        if (reissueError.message === 'No refresh token is available.') {
          return Promise.reject(toApiError(error));
        }

        return Promise.reject(reissueError);
      }
    }

    return Promise.reject(toApiError(error));
  }
);

export default apiClient;
