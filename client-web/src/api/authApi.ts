import { api, setAuthToken, clearAuthToken } from './client';
import { AuthResponse, UserProfile, UpdateProfileRequest } from '../types/user';

export const authApi = {
  /**
   * Sends 5-digit verification code to mobile number.
   */
  async sendOtp(mobile: string): Promise<{ mobile: string; expiresInSeconds: number }> {
    return api.post<{ mobile: string; expiresInSeconds: number }>('/v2/auth/otp/send', {
      mobile,
    });
  },

  /**
   * Verifies OTP code and returns JWT token & user profile.
   */
  async verifyOtp(mobile: string, otpCode: string): Promise<AuthResponse> {
    const res = await api.post<AuthResponse>('/v2/auth/otp/verify', {
      mobile,
      otpCode,
    });

    if (res?.token) {
      setAuthToken(res.token);
    }

    return res;
  },

  /**
   * Fetches detailed profile of the authenticated user.
   */
  async getProfile(): Promise<UserProfile> {
    return api.get<UserProfile>('/v2/user/profile');
  },

  /**
   * Updates user profile (nickname, avatar, name, languageLevel).
   */
  async updateProfile(data: UpdateProfileRequest): Promise<UserProfile> {
    return api.put<UserProfile>('/v2/user/profile', data);
  },

  /**
   * Logs out user and cleans local state.
   */
  async logout(): Promise<void> {
    try {
      await api.post('/v2/auth/logout', {});
    } catch {
      // Ignore network errors on logout
    } finally {
      clearAuthToken();
    }
  },
};
