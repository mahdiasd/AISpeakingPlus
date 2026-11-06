import React, { useState } from 'react';
import { Phone, KeyRound, Sparkles, AlertCircle, ArrowRight, Loader2 } from 'lucide-react';
import { CupertinoModal, CupertinoButton } from '../cupertino';
import { TEST_ACCOUNT } from '../../api/client';

export interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSendOtp: (mobile: string) => Promise<unknown>;
  onVerifyOtp: (mobile: string, otpCode: string) => Promise<unknown>;
  onLoginWithTestAccount: () => Promise<unknown>;
  onSuccess: () => void;
}

export const AuthModal: React.FC<AuthModalProps> = ({
  isOpen,
  onClose,
  onSendOtp,
  onVerifyOtp,
  onLoginWithTestAccount,
  onSuccess,
}) => {
  const [step, setStep] = useState<'mobile' | 'otp'>('mobile');
  const [mobile, setMobile] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const resetState = () => {
    setStep('mobile');
    setMobile('');
    setOtpCode('');
    setError(null);
    setIsLoading(false);
  };

  const handleClose = () => {
    resetState();
    onClose();
  };

  const handleSendOtp = async (e?: React.FormEvent) => {
    e?.preventDefault();
    if (!mobile.trim()) {
      setError('لطفاً شماره موبایل خود را وارد کنید');
      return;
    }
    setError(null);
    setIsLoading(true);
    try {
      await onSendOtp(mobile.trim());
      setStep('otp');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'خطا در ارسال کد تایید');
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyOtp = async (e?: React.FormEvent) => {
    e?.preventDefault();
    if (!otpCode.trim()) {
      setError('لطفاً کد تایید ۵ رقمی را وارد کنید');
      return;
    }
    setError(null);
    setIsLoading(true);
    try {
      await onVerifyOtp(mobile.trim(), otpCode.trim());
      handleClose();
      onSuccess();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'کد تایید نامعتبر است');
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickBypassLogin = async () => {
    setError(null);
    setIsLoading(true);
    try {
      await onLoginWithTestAccount();
      handleClose();
      onSuccess();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'خطا در ورود تستی');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={handleClose}
      title={step === 'mobile' ? 'ورود یا ثبت‌نام' : 'کد تایید پیامک'}
      subtitle={
        step === 'mobile'
          ? 'برای دسترسی به مراحل بعدی، پیشرفت خود را ذخیره کنید'
          : `کد تایید ۵ رقمی ارسال شده به ${mobile} را وارد کنید`
      }
    >
      <div className="space-y-4">
        {/* Quick Test Account Bypass Banner */}
        <div className="p-3.5 rounded-2xl bg-gradient-to-r from-apple-blue/20 to-apple-indigo/20 border border-apple-blue/30 space-y-2">
          <div className="flex items-center gap-1.5 text-xs font-bold text-apple-blue">
            <Sparkles className="w-4 h-4" />
            <span>حساب تستی آماده (بدون نیاز به پیامک واقعی)</span>
          </div>
          <p className="text-[11px] text-apple-gray-300 leading-relaxed">
            می‌توانید با یک لمس با شماره {TEST_ACCOUNT.mobile} و کد {TEST_ACCOUNT.otp} وارد شوید.
          </p>
          <button
            type="button"
            disabled={isLoading}
            onClick={handleQuickBypassLogin}
            className="w-full py-2 px-3 rounded-xl bg-apple-blue text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow-apple-sm active:scale-95 transition-transform"
          >
            {isLoading ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : null}
            <span>ورود آنی با حساب تستی (Bypass)</span>
          </button>
        </div>

        {/* Error message alert */}
        {error && (
          <div className="flex items-center gap-2 p-3 rounded-xl bg-apple-red/15 border border-apple-red/30 text-apple-red text-xs">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Form Steps */}
        {step === 'mobile' ? (
          <form onSubmit={handleSendOtp} className="space-y-3">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-apple-gray-300">شماره موبایل:</label>
              <div className="relative flex items-center">
                <input
                  type="tel"
                  dir="ltr"
                  placeholder="09152413498"
                  value={mobile}
                  onChange={(e) => setMobile(e.target.value)}
                  className="w-full h-12 pl-4 pr-10 rounded-2xl bg-white/5 border border-white/10 text-white placeholder-apple-gray-500 font-mono text-sm focus:outline-none focus:border-apple-blue transition-colors"
                />
                <Phone className="w-4 h-4 text-apple-gray-400 absolute right-3 pointer-events-none" />
              </div>
            </div>

            <CupertinoButton
              type="submit"
              variant="filled"
              size="lg"
              fullWidth
              disabled={isLoading || !mobile.trim()}
            >
              {isLoading ? 'در حال ارسال کد...' : 'ارسال کد تایید'}
            </CupertinoButton>
          </form>
        ) : (
          <form onSubmit={handleVerifyOtp} className="space-y-3">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-apple-gray-300">کد تایید ۵ رقمی:</label>
              <div className="relative flex items-center">
                <input
                  type="text"
                  dir="ltr"
                  placeholder="87799"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value)}
                  className="w-full h-12 pl-4 pr-10 rounded-2xl bg-white/5 border border-white/10 text-white placeholder-apple-gray-500 font-mono text-center tracking-widest text-lg font-bold focus:outline-none focus:border-apple-blue transition-colors"
                />
                <KeyRound className="w-4 h-4 text-apple-gray-400 absolute right-3 pointer-events-none" />
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setStep('mobile')}
                className="px-3 py-2 text-xs text-apple-blue flex items-center gap-1 active:opacity-60"
              >
                <ArrowRight className="w-3.5 h-3.5" />
                <span>ویرایش شماره</span>
              </button>
            </div>

            <CupertinoButton
              type="submit"
              variant="filled"
              size="lg"
              fullWidth
              disabled={isLoading || !otpCode.trim()}
            >
              {isLoading ? 'در حال بررسی کد...' : 'تایید و ورود'}
            </CupertinoButton>
          </form>
        )}
      </div>
    </CupertinoModal>
  );
};
