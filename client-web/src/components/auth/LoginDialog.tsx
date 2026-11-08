import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import {
  Phone,
  AlertCircle,
  RotateCcw,
  Loader2,
  CheckCircle2,
  ShieldCheck,
  User,
  Sparkles,
  Edit3,
} from 'lucide-react';
import {
  CupertinoModal,
  CupertinoPinInput,
} from '../cupertino';
import { useAuthFlow } from '../../hooks/useAuthFlow';
import { toPersianDigits } from '../../utils/numberUtils';

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
    loginWithTestAccount,
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

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={handleClose}
      title={step === 'phone' ? 'ورود به حساب' : 'تایید شماره'}
    >
      <div dir="rtl" className="space-y-4">
        {/* Welcome Character & Speech Bubble */}
        <motion.div
          animate={{ y: [-4, 4, -4] }}
          transition={{ duration: 2.6, repeat: Infinity, ease: 'easeInOut' }}
          className="flex flex-col items-center text-center select-none pt-1"
        >
          {/* Glass Speech Bubble */}
          <div className="relative mb-2 px-4 py-2 rounded-2xl apple-glass border border-white/20 text-center shadow-apple-sm max-w-[280px]">
            <p className="text-xs font-bold text-white leading-relaxed">
              {step === 'phone' ? (
                <>
                  سلام دوست من!
                  <br />
                  شماره موبایلت رو بنویس تا با هم شروع کنیم.
                </>
              ) : (
                <>
                  عالیه!
                  <br />
                  کدی که برات پیامک شد رو وارد کن.
                </>
              )}
            </p>
            <div className="absolute -bottom-1.5 left-1/2 -translate-x-1/2 w-2.5 h-2.5 bg-[#161c36] rotate-45 border-r border-b border-white/20" />
          </div>

          {/* Character 3D Illustration */}
          <div className="relative w-24 h-24 flex items-center justify-center">
            <img
              src={step === 'phone' ? '/resources/auth_vector.png' : '/resources/dialog_message_vector.png'}
              alt="راهنمای مکالمه"
              className="w-full h-full object-contain drop-shadow-[0_8px_16px_rgba(0,0,0,0.45)] pointer-events-none"
              onError={(e) => {
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          </div>
        </motion.div>

        {/* Error Banner */}
        {error && (
          <motion.div
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex items-center gap-2 p-3 rounded-2xl bg-apple-red/15 border border-apple-red/30 text-apple-red text-xs"
          >
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span className="leading-relaxed">{error}</span>
          </motion.div>
        )}

        {/* Animated Step Form */}
        <AnimatePresence mode="wait">
          {step === 'phone' ? (
            <motion.div
              key="modal-step-phone"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
              className="space-y-4"
            >
              {/* Titles */}
              <div className="text-center space-y-1">
                <h3 className="text-lg font-bold text-white">ورود / ثبت‌نام</h3>
                <p className="text-xs text-apple-gray-300 leading-relaxed">
                  برای شروع ماجراجویی مکالمه انگلیسی، شماره موبایلت را وارد کن.
                </p>
              </div>

              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  sendOtp();
                }}
                className="space-y-3 pt-1"
              >
                {/* Phone Input */}
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-apple-gray-300 block text-right">
                    شماره موبایل
                  </label>
                  <div className="relative flex items-center">
                    <input
                      type="tel"
                      dir="ltr"
                      placeholder="09123456789"
                      value={phone}
                      disabled={isLoading}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full h-12 pl-4 pr-11 rounded-2xl bg-white/5 border border-white/15 text-white placeholder-apple-gray-500 font-mono text-sm focus:outline-none focus:border-apple-blue focus:ring-2 focus:ring-apple-blue/30 transition-all text-left"
                    />
                    <Phone className="w-4 h-4 text-apple-gray-400 absolute right-3.5 pointer-events-none" />
                  </div>
                </div>

                {/* Primary Button */}
                <motion.button
                  type="submit"
                  whileTap={{ scale: 0.97 }}
                  transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                  disabled={isLoading || !phone.trim()}
                  className="w-full h-12 rounded-2xl bg-gradient-to-r from-apple-blue via-blue-600 to-[#5856D6] text-white font-bold text-sm shadow-[0_4px_18px_rgba(0,122,255,0.35)] hover:brightness-105 active:scale-[0.97] transition-all flex items-center justify-center gap-2 border border-white/20 disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  {isLoading ? (
                    <span className="flex items-center gap-2">
                      <Loader2 className="w-4 h-4 animate-spin" />
                      <span>در حال ارسال پیامک...</span>
                    </span>
                  ) : (
                    'ارسال کد تایید'
                  )}
                </motion.button>

                {/* Divider: یا */}
                <div className="flex items-center gap-3 py-0.5">
                  <div className="flex-1 h-[1px] bg-white/10" />
                  <span className="text-xs text-apple-gray-400 font-medium select-none">یا</span>
                  <div className="flex-1 h-[1px] bg-white/10" />
                </div>

                {/* Guest Button */}
                <div className="space-y-1 text-center">
                  <motion.button
                    type="button"
                    whileTap={{ scale: 0.97 }}
                    transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                    onClick={handleClose}
                    className="w-full h-11 rounded-2xl apple-glass-pill hover:bg-white/15 active:scale-[0.97] text-white font-semibold text-xs border border-white/25 flex items-center justify-center gap-2 transition-all shadow-apple-sm"
                  >
                    <User className="w-3.5 h-3.5 text-apple-gray-300" />
                    <span>ورود به‌عنوان مهمان</span>
                  </motion.button>
                  <p className="text-[10.5px] text-apple-gray-400 leading-relaxed px-1">
                    بدون ثبت‌نام شروع کن؛ پیشرفتت فقط روی همین دستگاه ذخیره می‌شود.
                  </p>
                </div>

                {/* Quick Test Login */}
                <div className="pt-1 text-center">
                  <button
                    type="button"
                    onClick={() => loginWithTestAccount()}
                    className="inline-flex items-center gap-1.5 text-[11px] text-apple-blue/80 hover:text-apple-blue transition-colors underline-offset-4 hover:underline active:opacity-60"
                  >
                    <Sparkles className="w-3 h-3 text-amber-400" />
                    <span>ورود سریع تستی (۰۹۱۵۲۴۱۳۴۹۸)</span>
                  </button>
                </div>
              </form>
            </motion.div>
          ) : (
            <motion.div
              key="modal-step-otp"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.2 }}
              className="space-y-4"
            >
              {/* Titles */}
              <div className="text-center space-y-1">
                <h3 className="text-lg font-bold text-white">کد تایید را وارد کن</h3>
                <p className="text-xs text-apple-gray-300 leading-relaxed">
                  کد پیامک‌شده به این شماره را بنویس:
                </p>
              </div>

              {/* Target Phone Badge + Edit */}
              <div className="flex items-center justify-between p-2.5 rounded-2xl bg-white/5 border border-white/10 text-xs">
                <div className="flex items-center gap-1.5 text-apple-gray-200">
                  <CheckCircle2 className="w-3.5 h-3.5 text-apple-green" />
                  <span className="font-mono text-white font-bold tracking-wider text-xs" dir="ltr">
                    {phone}
                  </span>
                </div>

                <button
                  type="button"
                  onClick={changePhone}
                  className="text-apple-blue hover:text-blue-400 text-xs font-semibold flex items-center gap-1 active:opacity-60 transition-colors"
                >
                  <Edit3 className="w-3 h-3" />
                  <span>ویرایش شماره</span>
                </button>
              </div>

              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  verifyOtp();
                }}
                className="space-y-4"
              >
                {/* 5-digit PIN input */}
                <div className="py-1 text-center">
                  <CupertinoPinInput
                    length={5}
                    value={otp}
                    onChange={setOtp}
                    onComplete={(code) => verifyOtp(code)}
                    disabled={isLoading}
                    error={Boolean(error)}
                  />
                </div>

                {/* Primary Button */}
                <motion.button
                  type="submit"
                  whileTap={{ scale: 0.97 }}
                  transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                  disabled={isLoading || otp.length < 5}
                  className="w-full h-12 rounded-2xl bg-gradient-to-r from-[#FFB800] via-[#FFA000] to-[#FF8F00] text-slate-950 font-bold text-sm shadow-[0_4px_20px_rgba(255,184,0,0.35)] hover:brightness-105 active:scale-[0.97] transition-all flex items-center justify-center gap-2 border border-white/30 disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  {isLoading ? (
                    <span className="flex items-center gap-2 text-slate-950">
                      <Loader2 className="w-4 h-4 animate-spin" />
                      <span>در حال تایید و ورود...</span>
                    </span>
                  ) : (
                    'تایید و ورود'
                  )}
                </motion.button>

                {/* Resend Countdown */}
                <div className="flex items-center justify-center text-xs pt-0.5">
                  {canResend ? (
                    <button
                      type="button"
                      onClick={resendOtp}
                      disabled={isLoading}
                      className="inline-flex items-center gap-1.5 text-apple-blue font-semibold active:opacity-60 hover:underline"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      <span>ارسال دوباره کد</span>
                    </button>
                  ) : (
                    <span className="text-apple-gray-400">
                      ارسال دوباره کد تا {toPersianDigits(countdown)} ثانیه دیگر
                    </span>
                  )}
                </div>
              </form>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Footer */}
        <div className="pt-2 flex items-center justify-center gap-1.5 text-[11px] text-apple-gray-400 select-none border-t border-white/5">
          <ShieldCheck className="w-3.5 h-3.5 text-apple-green" />
          <span>ورود امن و سریع با کد یک‌بار مصرف پیامکی</span>
        </div>
      </div>
    </CupertinoModal>
  );
};
