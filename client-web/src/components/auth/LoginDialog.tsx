import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import {
  Phone,
  AlertCircle,
  ArrowRight,
  RotateCcw,
  Loader2,
  CheckCircle2,
} from 'lucide-react';
import {
  CupertinoModal,
  CupertinoButton,
  CupertinoPinInput,
} from '../cupertino';
import { useAuthFlow } from '../../hooks/useAuthFlow';

export interface LoginDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  initialPhone?: string;
}

export const LoginDialog: React.FC<LoginDialogProps> = ({
  isOpen,
  onClose,
  onSuccess,
  initialPhone,
}) => {
  const {
    step,
    phone,
    setPhone,
    otp,
    setOtp,
    countdown,
    canResend,
    isLoading,
    error,
    sendOtp,
    verifyOtp,
    resendOtp,
    changePhone,
    resetFlow,
  } = useAuthFlow({
    initialPhone,
    onSuccess: () => {
      onSuccess();
      onClose();
      resetFlow();
    },
  });

  const handleClose = () => {
    resetFlow();
    onClose();
  };

  const formatCountdown = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={handleClose}
      title={step === 'phone' ? 'ورود یا ثبت‌نام' : 'کد تایید ۵ رقمی'}
      subtitle={
        step === 'phone'
          ? 'برای ذخیره امتیازات و دسترسی به مراحل بعدی وارد شوید'
          : `کد پیامک‌شده به ${phone} را وارد کنید`
      }
    >
      <div className="space-y-4">
        {/* Error Banner */}
        {error && (
          <div className="flex items-center gap-2 p-3 rounded-xl bg-apple-red/15 border border-apple-red/30 text-apple-red text-xs">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span className="leading-relaxed">{error}</span>
          </div>
        )}

        <AnimatePresence mode="wait">
          {step === 'phone' ? (
            <motion.form
              key="step-phone"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
              onSubmit={(e) => {
                e.preventDefault();
                sendOtp();
              }}
              className="space-y-3"
            >
              <div className="space-y-1">
                <label className="text-xs font-semibold text-apple-gray-300">شماره موبایل:</label>
                <div className="relative flex items-center">
                  <input
                    type="tel"
                    dir="ltr"
                    placeholder="09152413498"
                    value={phone}
                    disabled={isLoading}
                    onChange={(e) => setPhone(e.target.value)}
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
                disabled={isLoading || !phone.trim()}
              >
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>در حال ارسال پیامک...</span>
                  </span>
                ) : (
                  'ارسال کد تایید'
                )}
              </CupertinoButton>
            </motion.form>
          ) : (
            <motion.form
              key="step-otp"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.2 }}
              onSubmit={(e) => {
                e.preventDefault();
                verifyOtp();
              }}
              className="space-y-4"
            >
              {/* Target Phone Indicator + Edit */}
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-white/5 border border-white/10 text-xs">
                <div className="flex items-center gap-1.5 text-apple-gray-300">
                  <CheckCircle2 className="w-3.5 h-3.5 text-apple-green" />
                  <span>ارسال به:</span>
                  <span className="font-mono text-white font-bold">{phone}</span>
                </div>

                <button
                  type="button"
                  onClick={changePhone}
                  className="text-apple-blue text-xs font-semibold flex items-center gap-1 active:opacity-60"
                >
                  <ArrowRight className="w-3 h-3" />
                  <span>تغییر شماره</span>
                </button>
              </div>

              {/* 5-digit PIN input */}
              <div className="py-2">
                <CupertinoPinInput
                  length={5}
                  value={otp}
                  onChange={setOtp}
                  onComplete={(code) => verifyOtp(code)}
                  disabled={isLoading}
                  error={Boolean(error)}
                />
              </div>

              {/* Resend Countdown */}
              <div className="flex items-center justify-center text-xs">
                {canResend ? (
                  <button
                    type="button"
                    onClick={resendOtp}
                    disabled={isLoading}
                    className="inline-flex items-center gap-1 text-apple-blue font-semibold active:opacity-60"
                  >
                    <RotateCcw className="w-3.5 h-3.5" />
                    <span>ارسال مجدد کد تایید</span>
                  </button>
                ) : (
                  <span className="text-apple-gray-400 font-mono">
                    ارسال مجدد کد پس از {formatCountdown(countdown)}
                  </span>
                )}
              </div>

              <CupertinoButton
                type="submit"
                variant="filled"
                size="lg"
                fullWidth
                disabled={isLoading || otp.length < 5}
              >
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>در حال بررسی کد...</span>
                  </span>
                ) : (
                  'تایید و ورود به اپلیکیشن'
                )}
              </CupertinoButton>
            </motion.form>
          )}
        </AnimatePresence>
      </div>
    </CupertinoModal>
  );
};
