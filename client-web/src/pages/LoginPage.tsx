import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import confetti from 'canvas-confetti';
import {
  Phone,
  AlertCircle,
  ArrowRight,
  RotateCcw,
  Loader2,
  CheckCircle2,
  ShieldCheck,
  User,
  GraduationCap,
} from 'lucide-react';
import {
  CupertinoNavBar,
  CupertinoCard,
  CupertinoButton,
  CupertinoPinInput,
} from '../components/cupertino';
import { useAuthFlow } from '../hooks/useAuthFlow';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  const handleSuccess = () => {
    confetti({
      particleCount: 60,
      spread: 70,
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
  } = useAuthFlow({
    onSuccess: handleSuccess,
  });

  const formatCountdown = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="flex flex-col min-h-screen bg-[#0c0c0e] text-white selection:bg-apple-blue selection:text-white pb-16">
      {/* 1. Cupertino Top App Bar with RTL Back Button */}
      <CupertinoNavBar
        title="ورود به حساب کاربری"
        subtitle="AI Speaking Plus"
        onBack={() => navigate('/')}
        backLabel="خانه"
      />

      {/* 2. Main Centered Container */}
      <main className="flex-1 w-full max-w-md mx-auto p-4 flex flex-col justify-center">
        <CupertinoCard
          variant="glass"
          withSpecularHighlight
          className="relative overflow-hidden group space-y-6 p-6 sm:p-8"
        >
          {/* Subtle Ambient Background Orbs */}
          <div className="absolute -top-14 -right-14 w-48 h-48 bg-apple-blue/25 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-12 -left-12 w-48 h-48 bg-apple-indigo/25 rounded-full blur-3xl pointer-events-none" />

          {/* Brand Identity Header */}
          <div className="relative text-center space-y-2 select-none">
            <div className="w-16 h-16 rounded-3xl mx-auto p-0.5 bg-gradient-to-br from-apple-blue via-apple-indigo to-apple-teal shadow-apple-lg flex items-center justify-center">
              <div className="w-full h-full rounded-[22px] bg-[#141418] flex items-center justify-center text-apple-blue">
                <GraduationCap className="w-8 h-8 text-white drop-shadow-[0_0_10px_rgba(0,122,255,0.8)]" />
              </div>
            </div>

            <h2 className="text-xl font-bold text-white tracking-tight pt-1">
              {step === 'phone' ? 'ورود به دنیای مکالمه هوشمند' : 'تایید شماره موبایل'}
            </h2>

            <p className="text-xs text-apple-gray-400 max-w-xs mx-auto leading-relaxed">
              {step === 'phone'
                ? 'شماره موبایل خود را وارد کنید تا پیشرفت آموزشی شما در لندن ذخیره شود.'
                : `کد تایید پیامک‌شده به شماره ${phone} را وارد کنید.`}
            </p>
          </div>

          {/* Error Banner */}
          {error && (
            <motion.div
              initial={{ opacity: 0, y: -6 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex items-center gap-2 p-3 rounded-xl bg-apple-red/15 border border-apple-red/30 text-apple-red text-xs"
            >
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span className="leading-relaxed">{error}</span>
            </motion.div>
          )}

          {/* Animated Steps */}
          <AnimatePresence mode="wait">
            {step === 'phone' ? (
              <motion.form
                key="login-step-phone"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.2 }}
                onSubmit={(e) => {
                  e.preventDefault();
                  sendOtp();
                }}
                className="space-y-4"
              >
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-apple-gray-300 flex items-center justify-between">
                    <span>شماره موبایل:</span>
                    <span className="text-[11px] text-apple-gray-500 font-normal">نمونه: ۰۹۱۵۲۴۱۳۴۹۸</span>
                  </label>

                  <div className="relative flex items-center">
                    <input
                      type="tel"
                      dir="ltr"
                      placeholder="09152413498"
                      value={phone}
                      disabled={isLoading}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full h-13 pl-4 pr-11 rounded-2xl bg-white/5 border border-white/12 text-white placeholder-apple-gray-500 font-mono text-base focus:outline-none focus:border-apple-blue focus:ring-2 focus:ring-apple-blue/30 transition-all"
                    />
                    <Phone className="w-5 h-5 text-apple-gray-400 absolute right-3.5 pointer-events-none" />
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
                    'دریافت کد تایید پیامکی'
                  )}
                </CupertinoButton>

                {/* Continue as Guest option */}
                <div className="pt-2 text-center">
                  <button
                    type="button"
                    onClick={() => navigate('/')}
                    className="inline-flex items-center gap-1.5 text-xs text-apple-gray-400 hover:text-white transition-colors active:opacity-60"
                  >
                    <User className="w-3.5 h-3.5" />
                    <span>ادامه به عنوان مهمان (بدون ثبت‌نام)</span>
                  </button>
                </div>
              </motion.form>
            ) : (
              <motion.form
                key="login-step-otp"
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
                {/* Phone badge with Edit */}
                <div className="flex items-center justify-between p-3 rounded-2xl bg-white/5 border border-white/10 text-xs">
                  <div className="flex items-center gap-1.5 text-apple-gray-300">
                    <CheckCircle2 className="w-4 h-4 text-apple-green" />
                    <span>کد پیامک‌شده به:</span>
                    <span className="font-mono text-white font-bold tracking-wider">{phone}</span>
                  </div>

                  <button
                    type="button"
                    onClick={changePhone}
                    className="text-apple-blue text-xs font-semibold flex items-center gap-1 active:opacity-60 hover:underline"
                  >
                    <ArrowRight className="w-3.5 h-3.5" />
                    <span>ویرایش شماره</span>
                  </button>
                </div>

                {/* 5-digit PIN input */}
                <div className="py-2 space-y-1 text-center">
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
                      className="inline-flex items-center gap-1.5 text-apple-blue font-semibold active:opacity-60 hover:underline"
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
                      <span>در حال تایید و راه‌اندازی...</span>
                    </span>
                  ) : (
                    'ورود به برنامه'
                  )}
                </CupertinoButton>
              </motion.form>
            )}
          </AnimatePresence>

          {/* Privacy & Security Note */}
          <div className="flex items-center justify-center gap-1.5 text-[11px] text-apple-gray-500 pt-2 border-t border-white/5 select-none">
            <ShieldCheck className="w-3.5 h-3.5 text-apple-green" />
            <span>اطلاعات شما با رمزنگاری استاندارد محافظت می‌شود</span>
          </div>
        </CupertinoCard>
      </main>
    </div>
  );
};

export default LoginPage;
