import { useState, useEffect, useCallback, useRef } from 'react';
import { authApi } from '../api/authApi';
import { TEST_ACCOUNT } from '../api/client';

export type AuthStep = 'phone' | 'otp';

export interface UseAuthFlowOptions {
  initialPhone?: string;
  onSuccess?: () => void;
}

export function useAuthFlow(options: UseAuthFlowOptions = {}) {
  const [step, setStep] = useState<AuthStep>('phone');
  const [phone, setPhone] = useState(options.initialPhone || '');
  const [otp, setOtp] = useState('');
  const [countdown, setCountdown] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const timerRef = useRef<number | null>(null);

  // Clear timer on unmount
  useEffect(() => {
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  const startCountdown = useCallback((seconds: number = 120) => {
    if (timerRef.current) clearInterval(timerRef.current);
    setCountdown(seconds);

    timerRef.current = window.setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          if (timerRef.current) clearInterval(timerRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  }, []);

  const normalizePhone = (raw: string): string => {
    // Convert Persian/Arabic digits to English
    return raw
      .replace(/[۰-۹]/g, (d) => String.fromCharCode(d.charCodeAt(0) - 1728))
      .replace(/[٠-٩]/g, (d) => String.fromCharCode(d.charCodeAt(0) - 1584))
      .replace(/\s+/g, '')
      .trim();
  };

  const isValidIranianMobile = (num: string): boolean => {
    return /^09\d{9}$/.test(num);
  };

  const handleSendOtp = useCallback(
    async (targetPhone?: string) => {
      const cleanPhone = normalizePhone(targetPhone || phone);
      if (!cleanPhone) {
        setError('لطفاً شماره موبایل خود را وارد کنید.');
        return false;
      }

      if (!isValidIranianMobile(cleanPhone)) {
        setError('شماره موبایل نامعتبر است. فرمت صحیح: ۰۹۱۲۳۴۵۶۷۸۹ (۱۱ رقم با 09)');
        return false;
      }

      setError(null);
      setIsLoading(true);
      try {
        const res = await authApi.sendOtp(cleanPhone);
        setPhone(cleanPhone);
        setStep('otp');
        setOtp('');
        startCountdown(res.expiresInSeconds || 120);
        return true;
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : 'خطا در ارسال کد تایید';
        setError(message);
        return false;
      } finally {
        setIsLoading(false);
      }
    },
    [phone, startCountdown]
  );

  const handleVerifyOtp = useCallback(
    async (codeToVerify?: string) => {
      const cleanOtp = (codeToVerify || otp).trim();
      if (!cleanOtp || cleanOtp.length < 5) {
        setError('لطفاً کد تایید ۵ رقمی را کامل وارد کنید.');
        return false;
      }

      setError(null);
      setIsLoading(true);
      try {
        await authApi.verifyOtp(phone, cleanOtp);
        if (timerRef.current) clearInterval(timerRef.current);
        options.onSuccess?.();
        return true;
      } catch (err: unknown) {
        const message =
          err instanceof Error
            ? err.message
            : 'کد تایید وارد شده نامعتبر است یا منقضی شده است.';
        setError(message);
        return false;
      } finally {
        setIsLoading(false);
      }
    },
    [phone, otp, options]
  );

  const handleResendOtp = useCallback(async () => {
    if (countdown > 0) return;
    return handleSendOtp(phone);
  }, [countdown, handleSendOtp, phone]);

  const handleChangePhone = useCallback(() => {
    if (timerRef.current) clearInterval(timerRef.current);
    setStep('phone');
    setOtp('');
    setError(null);
  }, []);

  const handleBypassTestLogin = useCallback(async () => {
    setError(null);
    setIsLoading(true);
    try {
      await authApi.verifyOtp(TEST_ACCOUNT.mobile, TEST_ACCOUNT.otp);
      if (timerRef.current) clearInterval(timerRef.current);
      options.onSuccess?.();
      return true;
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'خطا در ورود تستی');
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [options]);

  const resetFlow = useCallback(() => {
    if (timerRef.current) clearInterval(timerRef.current);
    setStep('phone');
    setPhone('');
    setOtp('');
    setCountdown(0);
    setError(null);
    setIsLoading(false);
  }, []);

  return {
    step,
    phone,
    setPhone,
    otp,
    setOtp,
    countdown,
    canResend: countdown === 0,
    isLoading,
    error,
    setError,
    sendOtp: handleSendOtp,
    verifyOtp: handleVerifyOtp,
    resendOtp: handleResendOtp,
    changePhone: handleChangePhone,
    loginWithTestAccount: handleBypassTestLogin,
    resetFlow,
  };
}
