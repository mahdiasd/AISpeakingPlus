import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import confetti from 'canvas-confetti';
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
  CupertinoNavBar,
  CupertinoPinInput,
} from '../components/cupertino';
import { useAuthFlow } from '../hooks/useAuthFlow';
import { toPersianDigits } from '../utils/numberUtils';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  const handleSuccess = () => {
    confetti({
      particleCount: 70,
      spread: 75,
      origin: { y: 0.6 },
      colors: ['#007aff', '#34c759', '#ffcc00', '#5856d6'],
    });
    setTimeout(() => {
      navigate('/');
    }, 600);
  };

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
  } = useAuthFlow({
    onSuccess: handleSuccess,
  });

  return (
    <div
      dir="rtl"
      className="flex flex-col min-h-screen bg-gradient-to-b from-[#1B1F4B] via-[#0E1433] to-[#0D1026] text-white selection:bg-apple-blue selection:text-white"
    >
      {/* 1. Cupertino Top App Bar with RTL Back Button (Points Right '>') */}
      <CupertinoNavBar
        title="ورود به حساب کاربری"
        subtitle="AI Speaking Plus"
        onBack={() => navigate('/')}
        backLabel="بازگشت"
      />

      {/* 2. Main Centered Screen Area */}
      <main className="flex-1 w-full max-w-md mx-auto px-4 py-4 sm:py-6 flex flex-col justify-center items-center relative z-10">
        {/* Subtle Ambient Background Glows */}
        <div className="absolute top-1/4 -right-20 w-64 h-64 bg-apple-blue/15 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-1/4 -left-20 w-64 h-64 bg-apple-indigo/20 rounded-full blur-3xl pointer-events-none" />

        {/* 3. Welcome Header & Floating Character Illustration */}
        <motion.div
          animate={{ y: [-5, 5, -5] }}
          transition={{ duration: 2.8, repeat: Infinity, ease: 'easeInOut' }}
          className="flex flex-col items-center mb-3 text-center select-none"
        >
          {/* Glass Speech Bubble */}
          <div className="relative mb-2 px-5 py-2.5 rounded-2xl apple-glass border border-white/25 text-center shadow-apple-sm max-w-xs">
            <p className="text-xs sm:text-sm font-bold text-white leading-relaxed">
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
            {/* Speech bubble pointer arrow */}
            <div className="absolute -bottom-1.5 left-1/2 -translate-x-1/2 w-3 h-3 bg-[#161c36] rotate-45 border-r border-b border-white/20" />
          </div>

          {/* Character 3D Illustration */}
          <div className="relative w-28 h-28 sm:w-32 sm:h-32 flex items-center justify-center">
            <img
              src={step === 'phone' ? '/resources/auth_vector.png' : '/resources/dialog_message_vector.png'}
              alt="راهنمای مکالمه"
              className="w-full h-full object-contain drop-shadow-[0_12px_24px_rgba(0,0,0,0.5)] pointer-events-none"
              onError={(e) => {
                // Graceful fallback if image path resolution differs in dev
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          </div>
        </motion.div>

        {/* 4. Apple Glass Form Card (.apple-glass-card with 14-16px blur) */}
        <div className="w-full apple-glass-card rounded-[30px] p-6 sm:p-7 relative overflow-hidden backdrop-blur-[14px] shadow-2xl border border-white/20">
          {/* Specular Top Edge Light Bar */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
          />

          {/* Error Banner */}
          {error && (
            <motion.div
              initial={{ opacity: 0, y: -6 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex items-center gap-2 p-3 rounded-2xl bg-apple-red/15 border border-apple-red/30 text-apple-red text-xs mb-4"
            >
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span className="leading-relaxed">{error}</span>
            </motion.div>
          )}

          {/* Animated Step Transitions */}
          <AnimatePresence mode="wait">
            {step === 'phone' ? (
              <motion.div
                key="step-phone"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.22 }}
                className="space-y-4"
              >
                {/* Titles */}
                <div className="text-center space-y-1">
                  <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                    ورود / ثبت‌نام
                  </h2>
                  <p className="text-xs sm:text-sm text-apple-gray-300 leading-relaxed max-w-xs mx-auto">
                    برای شروع ماجراجویی مکالمه انگلیسی، شماره موبایلت را وارد کن.
                  </p>
                </div>

                {/* Form */}
                <form
                  onSubmit={(e) => {
                    e.preventDefault();
                    sendOtp();
                  }}
                  className="space-y-4 pt-1"
                >
                  {/* Phone Input */}
                  <div className="space-y-1.5">
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
                        className="w-full h-12 pl-4 pr-11 rounded-2xl bg-white/5 border border-white/15 text-white placeholder-apple-gray-500 font-mono text-base focus:outline-none focus:border-apple-blue focus:ring-2 focus:ring-apple-blue/30 transition-all text-left"
                      />
                      <Phone className="w-5 h-5 text-apple-gray-400 absolute right-3.5 pointer-events-none" />
                    </div>
                  </div>

                  {/* Primary Spring Touch Button: ارسال کد تایید */}
                  <motion.button
                    type="submit"
                    whileTap={{ scale: 0.97 }}
                    transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                    disabled={isLoading || !phone.trim()}
                    className="w-full h-12 rounded-2xl bg-gradient-to-r from-apple-blue via-blue-600 to-[#5856D6] text-white font-bold text-sm sm:text-base shadow-[0_4px_18px_rgba(0,122,255,0.35)] hover:brightness-105 active:scale-[0.97] transition-all flex items-center justify-center gap-2 border border-white/20 disabled:opacity-40 disabled:cursor-not-allowed"
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
                  <div className="flex items-center gap-3 py-1">
                    <div className="flex-1 h-[1px] bg-white/10" />
                    <span className="text-xs text-apple-gray-400 font-medium select-none">یا</span>
                    <div className="flex-1 h-[1px] bg-white/10" />
                  </div>

                  {/* Secondary Glass Button: ورود به‌عنوان مهمان */}
                  <div className="space-y-1.5 text-center">
                    <motion.button
                      type="button"
                      whileTap={{ scale: 0.97 }}
                      transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                      onClick={() => navigate('/')}
                      className="w-full h-12 rounded-2xl apple-glass-pill hover:bg-white/15 active:scale-[0.97] text-white font-semibold text-sm border border-white/25 flex items-center justify-center gap-2 transition-all shadow-apple-sm"
                    >
                      <User className="w-4 h-4 text-apple-gray-300" />
                      <span>ورود به‌عنوان مهمان</span>
                    </motion.button>

                    <p className="text-[11px] text-apple-gray-400 leading-relaxed px-2">
                      بدون ثبت‌نام شروع کن؛ پیشرفتت فقط روی همین دستگاه ذخیره می‌شود.
                    </p>
                  </div>

                  {/* Quick Test Login Bypass for QA / Verification */}
                  <div className="pt-2 text-center">
                    <button
                      type="button"
                      onClick={() => loginWithTestAccount()}
                      className="inline-flex items-center gap-1.5 text-[11px] text-apple-blue/80 hover:text-apple-blue transition-colors underline-offset-4 hover:underline active:opacity-60"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                      <span>ورود سریع با اکانت تستی (۰۹۱۵۲۴۱۳۴۹۸)</span>
                    </button>
                  </div>
                </form>
              </motion.div>
            ) : (
              <motion.div
                key="step-otp"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.22 }}
                className="space-y-4"
              >
                {/* Titles */}
                <div className="text-center space-y-1">
                  <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                    کد تایید را وارد کن
                  </h2>
                  <p className="text-xs sm:text-sm text-apple-gray-300 leading-relaxed">
                    کد پیامک‌شده به این شماره را بنویس:
                  </p>
                </div>

                {/* Target Phone Badge + Edit Chip */}
                <div className="flex items-center justify-between p-3 rounded-2xl bg-white/5 border border-white/10 text-xs">
                  <div className="flex items-center gap-2 text-apple-gray-200">
                    <CheckCircle2 className="w-4 h-4 text-apple-green" />
                    <span className="font-mono text-white font-bold tracking-wider text-sm" dir="ltr">
                      {phone}
                    </span>
                  </div>

                  <button
                    type="button"
                    onClick={changePhone}
                    className="text-apple-blue hover:text-blue-400 text-xs font-semibold flex items-center gap-1 active:opacity-60 transition-colors"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
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
                  {/* 5-digit Cupertino PIN input */}
                  <div className="py-1 space-y-1 text-center">
                    <CupertinoPinInput
                      length={5}
                      value={otp}
                      onChange={setOtp}
                      onComplete={(code) => verifyOtp(code)}
                      disabled={isLoading}
                      error={Boolean(error)}
                    />
                  </div>

                  {/* Primary Spring Touch Button: تایید و ورود */}
                  <motion.button
                    type="submit"
                    whileTap={{ scale: 0.97 }}
                    transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                    disabled={isLoading || otp.length < 5}
                    className="w-full h-12 rounded-2xl bg-gradient-to-r from-[#FFB800] via-[#FFA000] to-[#FF8F00] text-slate-950 font-bold text-sm sm:text-base shadow-[0_4px_20px_rgba(255,184,0,0.35)] hover:brightness-105 active:scale-[0.97] transition-all flex items-center justify-center gap-2 border border-white/30 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    {isLoading ? (
                      <span className="flex items-center gap-2 text-slate-950">
                        <Loader2 className="w-4 h-4 animate-spin" />
                        <span>در حال تایید و راه‌اندازی...</span>
                      </span>
                    ) : (
                      'تایید و ورود'
                    )}
                  </motion.button>

                  {/* Resend Countdown / Button */}
                  <div className="flex items-center justify-center text-xs pt-1">
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
        </div>

        {/* 5. Bottom Trust Note */}
        <div className="mt-4 flex items-center justify-center gap-1.5 text-xs text-apple-gray-400 select-none">
          <ShieldCheck className="w-4 h-4 text-apple-green" />
          <span>ورود امن و سریع با کد یک‌بار مصرف پیامکی</span>
        </div>
      </main>
    </div>
  );
};

export default LoginPage;
