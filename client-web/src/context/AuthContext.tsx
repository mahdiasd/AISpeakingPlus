import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/authApi';
import { getAuthToken, TEST_ACCOUNT } from '../api/client';
import { UserProfile, UpdateProfileRequest, AuthResponse } from '../types/user';

interface AuthContextType {
  token: string | null;
  profile: UserProfile | null;
  isLoggedIn: boolean;
  isLoadingProfile: boolean;
  authError: string | null;
  sendOtp: (mobile: string) => Promise<{ mobile: string; expiresInSeconds: number }>;
  verifyOtp: (mobile: string, otpCode: string) => Promise<AuthResponse>;
  loginWithTestAccount: () => Promise<AuthResponse>;
  updateProfile: (data: UpdateProfileRequest) => Promise<UserProfile>;
  logout: () => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => getAuthToken());
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState<boolean>(false);
  const [authError, setAuthError] = useState<string | null>(null);

  const fetchProfile = useCallback(async () => {
    const currentToken = getAuthToken();
    if (!currentToken) {
      setToken(null);
      setProfile(null);
      return;
    }

    setToken(currentToken);
    setIsLoadingProfile(true);
    setAuthError(null);
    try {
      const data = await authApi.getProfile();
      setProfile(data);
    } catch (err: unknown) {
      console.warn('Failed to load profile, token might be expired', err);
      await authApi.logout();
      setToken(null);
      setProfile(null);
    } finally {
      setIsLoadingProfile(false);
    }
  }, []);

  useEffect(() => {
    if (token) {
      fetchProfile();
    }
  }, [token, fetchProfile]);

  useEffect(() => {
    const handleAuthChange = () => {
      const currentToken = getAuthToken();
      setToken(currentToken);
      if (currentToken) {
        fetchProfile();
      } else {
        setProfile(null);
      }
    };

    window.addEventListener('auth-token-changed', handleAuthChange);
    window.addEventListener('storage', handleAuthChange);
    return () => {
      window.removeEventListener('auth-token-changed', handleAuthChange);
      window.removeEventListener('storage', handleAuthChange);
    };
  }, [fetchProfile]);

  const sendOtp = useCallback(async (mobile: string) => {
    setAuthError(null);
    try {
      return await authApi.sendOtp(mobile);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'خطا در ارسال کد تایید';
      setAuthError(msg);
      throw err;
    }
  }, []);

  const verifyOtp = useCallback(
    async (mobile: string, otpCode: string) => {
      setAuthError(null);
      try {
        const res = await authApi.verifyOtp(mobile, otpCode);
        setToken(res.token);
        await fetchProfile();
        return res;
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : 'کد تایید وارد شده نامعتبر است';
        setAuthError(msg);
        throw err;
      }
    },
    [fetchProfile]
  );

  const updateProfile = useCallback(
    async (data: UpdateProfileRequest) => {
      setAuthError(null);
      try {
        const updated = await authApi.updateProfile(data);
        setProfile(updated);
        return updated;
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : 'خطا در ذخیره تغییرات پروفایل';
        setAuthError(msg);
        throw err;
      }
    },
    []
  );

  const loginWithTestAccount = useCallback(async () => {
    return verifyOtp(TEST_ACCOUNT.mobile, TEST_ACCOUNT.otp);
  }, [verifyOtp]);

  const logout = useCallback(async () => {
    await authApi.logout();
    setToken(null);
    setProfile(null);
  }, []);

  const value: AuthContextType = {
    token,
    profile,
    isLoggedIn: Boolean(token),
    isLoadingProfile,
    authError,
    sendOtp,
    verifyOtp,
    loginWithTestAccount,
    updateProfile,
    logout,
    refreshProfile: fetchProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
