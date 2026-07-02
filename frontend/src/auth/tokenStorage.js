const USER_TOKEN_KEY = 'agora.user.accessToken';
const USER_REFRESH_TOKEN_KEY = 'agora.user.refreshToken';
const ADMIN_TOKEN_KEY = 'agora.admin.accessToken';
export const USER_TOKENS_CHANGED_EVENT = 'agora:userTokensChanged';

const getStorage = () => {
  if (typeof localStorage === 'undefined') {
    return null;
  }

  return localStorage;
};

const getToken = (key) => getStorage()?.getItem(key) ?? null;

const setToken = (key, token) => {
  const storage = getStorage();

  if (!storage) {
    return;
  }

  storage.setItem(key, token);
};

const clearToken = (key) => {
  getStorage()?.removeItem(key);
};

const notifyUserTokensChanged = () => {
  if (typeof window !== 'undefined' && typeof window.dispatchEvent === 'function') {
    window.dispatchEvent(new Event(USER_TOKENS_CHANGED_EVENT));
  }
};

export const getUserToken = () => getToken(USER_TOKEN_KEY);

export const setUserToken = (token) => {
  setToken(USER_TOKEN_KEY, token);
  notifyUserTokensChanged();
};

export const clearUserToken = () => {
  clearToken(USER_TOKEN_KEY);
  notifyUserTokensChanged();
};

export const getUserRefreshToken = () => getToken(USER_REFRESH_TOKEN_KEY);

export const setUserRefreshToken = (token) => {
  setToken(USER_REFRESH_TOKEN_KEY, token);
  notifyUserTokensChanged();
};

export const clearUserRefreshToken = () => {
  clearToken(USER_REFRESH_TOKEN_KEY);
  notifyUserTokensChanged();
};

export const clearUserTokens = () => {
  clearToken(USER_TOKEN_KEY);
  clearToken(USER_REFRESH_TOKEN_KEY);
  notifyUserTokensChanged();
};

export const getAdminToken = () => getToken(ADMIN_TOKEN_KEY);

export const setAdminToken = (token) => setToken(ADMIN_TOKEN_KEY, token);

export const clearAdminToken = () => clearToken(ADMIN_TOKEN_KEY);
