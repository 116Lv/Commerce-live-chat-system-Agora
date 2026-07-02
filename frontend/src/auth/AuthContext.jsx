import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import {
  loginAdmin as loginAdminRequest,
  loginUser as loginUserRequest,
  logoutAdmin as logoutAdminRequest,
  logoutUser as logoutUserRequest,
  reissueUser as reissueUserRequest,
  signupUser as signupUserRequest
} from '../api/authApi.js';
import {
  clearAdminToken,
  clearUserTokens,
  getAdminToken,
  getUserRefreshToken,
  getUserToken,
  setAdminToken,
  setUserRefreshToken,
  setUserToken,
  USER_TOKENS_CHANGED_EVENT
} from './tokenStorage.js';

const AuthContext = createContext(null);

const getAccessToken = (response) => {
  if (!response?.accessToken) {
    throw new Error('Authentication response did not include an access token.');
  }

  return response.accessToken;
};

const getUserTokenPair = (response) => {
  const accessToken = getAccessToken(response);

  if (!response?.refreshToken) {
    throw new Error('Authentication response did not include a refresh token.');
  }

  return { accessToken, refreshToken: response.refreshToken };
};

export function AuthProvider({ children }) {
  const [userToken, setUserTokenState] = useState(() => getUserToken());
  const [adminToken, setAdminTokenState] = useState(() => getAdminToken());
  const [userProfile, setUserProfile] = useState(null);

  const persistUserTokens = ({ accessToken, refreshToken }) => {
    setUserToken(accessToken);
    setUserRefreshToken(refreshToken);
    setUserTokenState(accessToken);
  };

  const persistAdminToken = (token) => {
    setAdminToken(token);
    setAdminTokenState(token);
  };

  const loginUser = async (credentials) => {
    const response = await loginUserRequest(credentials);
    const tokens = getUserTokenPair(response);

    persistUserToken(token);
    setUserProfile(null);
    return response;
  };

  const updateUserProfile = (profile) => {
    setUserProfile((current) => ({ ...(current || {}), ...(profile || {}) }));
  };

  const signupUser = async (formData) => {
    const signupResponse = await signupUserRequest(formData);
    const loginResponse = await loginUser({
      email: formData.email,
      password: formData.password
    });

    return { signup: signupResponse, login: loginResponse };
  };

  const logoutUser = async () => {
    try {
      if (getUserToken()) {
        await logoutUserRequest();
      }
    } finally {
      clearUserTokens();
      setUserTokenState(null);
      setUserProfile(null);
    }
  };

  const extendUserSession = async () => {
    const refreshToken = getUserRefreshToken();

    if (!refreshToken) {
      clearUserTokens();
      setUserTokenState(null);
      throw new Error('로그인 연장에 필요한 refresh token이 없습니다.');
    }

    const response = await reissueUserRequest(refreshToken);
    const tokens = getUserTokenPair(response);

    persistUserTokens(tokens);
    return response;
  };

  const loginAdmin = async (credentials) => {
    const response = await loginAdminRequest(credentials);
    const token = getAccessToken(response);

    persistAdminToken(token);
    return response;
  };

  const logoutAdmin = async () => {
    try {
      if (getAdminToken()) {
        await logoutAdminRequest();
      }
    } finally {
      clearAdminToken();
      setAdminTokenState(null);
    }
  };

  useEffect(() => {
    const syncUserToken = () => setUserTokenState(getUserToken());

    window.addEventListener(USER_TOKENS_CHANGED_EVENT, syncUserToken);
    return () => window.removeEventListener(USER_TOKENS_CHANGED_EVENT, syncUserToken);
  }, []);

  const value = useMemo(
    () => ({
      userToken,
      adminToken,
      userProfile,
      isUserAuthenticated: Boolean(userToken),
      isAdminAuthenticated: Boolean(adminToken),
      updateUserProfile,
      loginUser,
      signupUser,
      logoutUser,
      extendUserSession,
      loginAdmin,
      logoutAdmin
    }),
    [userToken, adminToken, userProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider.');
  }

  return context;
};
