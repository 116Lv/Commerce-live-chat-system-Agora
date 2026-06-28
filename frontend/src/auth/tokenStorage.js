const USER_TOKEN_KEY = 'agora.user.accessToken';
const ADMIN_TOKEN_KEY = 'agora.admin.accessToken';

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

export const getUserToken = () => getToken(USER_TOKEN_KEY);

export const setUserToken = (token) => setToken(USER_TOKEN_KEY, token);

export const clearUserToken = () => clearToken(USER_TOKEN_KEY);

export const getAdminToken = () => getToken(ADMIN_TOKEN_KEY);

export const setAdminToken = (token) => setToken(ADMIN_TOKEN_KEY, token);

export const clearAdminToken = () => clearToken(ADMIN_TOKEN_KEY);
