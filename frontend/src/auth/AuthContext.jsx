import { createContext, useContext, useMemo, useState } from 'react';
import {
  loginAdmin as loginAdminRequest,
  loginUser as loginUserRequest,
  logoutAdmin as logoutAdminRequest,
  logoutUser as logoutUserRequest,
  signupUser as signupUserRequest
} from '../api/authApi.js';
import {
  clearAdminToken,
  clearUserToken,
  getAdminToken,
  getUserToken,
  setAdminToken,
  setUserToken
} from './tokenStorage.js';

const AuthContext = createContext(null);

const getAccessToken = (response) => {
  if (!response?.accessToken) {
    throw new Error('Authentication response did not include an access token.');
  }

  return response.accessToken;
};

export function AuthProvider({ children }) {
  const [userToken, setUserTokenState] = useState(() => getUserToken());
  const [adminToken, setAdminTokenState] = useState(() => getAdminToken());
  const [userProfile, setUserProfile] = useState(null);

  const persistUserToken = (token) => {
    setUserToken(token);
    setUserTokenState(token);
  };

  const persistAdminToken = (token) => {
    setAdminToken(token);
    setAdminTokenState(token);
  };

  const loginUser = async (credentials) => {
    const response = await loginUserRequest(credentials);
    const token = getAccessToken(response);

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
      clearUserToken();
      setUserTokenState(null);
      setUserProfile(null);
    }
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
