import { User } from './api';

// Auth state management
export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
}

// Check if session cookie exists
export const hasSessionCookie = (): boolean => {
  return document.cookie.split(';').some(cookie => 
    cookie.trim().startsWith('SMCCCMS_SESSION=')
  );
};

// Store user data temporarily during auth flow
const AUTH_STORAGE_KEY = 'smcccms_auth_temp';

export const storeAuthData = (data: any) => {
  sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data));
};

export const getAuthData = () => {
  const data = sessionStorage.getItem(AUTH_STORAGE_KEY);
  return data ? JSON.parse(data) : null;
};

export const clearAuthData = () => {
  sessionStorage.removeItem(AUTH_STORAGE_KEY);
};

// Step validation for auth flow
export const canAccessContactPage = (): boolean => {
  const data = getAuthData();
  return data && data.govIdVerified === true;
};

export const canAccessCodePage = (): boolean => {
  const data = getAuthData();
  return data && data.govIdVerified === true && data.codeRequested === true;
};