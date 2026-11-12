import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Crown,
  CheckCircle2,
  Sparkles,
  CreditCard,
  MessageSquare,
  Headphones,
  Zap,
  Check,
  Clock,
  Tag,
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { motion, AnimatePresence } from 'motion/react';
import {
  CupertinoNavBar,
  CupertinoSkeleton,
} from '../components/cupertino';
import {
  subscriptionApi,
  SubscriptionPlan,
  SubscriptionStatus,
  defaultFallbackPlans,
} from '../api/subscriptionApi';
import { useAuth } from '../context/AuthContext';
import { LoginDialog } from '../components/auth/LoginDialog';

export const SubscriptionPage: React.FC = () => {
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();

  const [plans, setPlans] = useState<SubscriptionPlan[]>(defaultFallbackPlans);
  const [selectedPlanId, setSelectedPlanId] = useState<string>('plan-3m');
  const [status, setStatus] = useState<SubscriptionStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Promo code state
  const [promoCodeInput, setPromoCodeInput] = useState('');
  const [isValidatingPromo, setIsValidatingPromo] = useState(false);
  const [appliedDiscountPercent, setAppliedDiscountPercent] = useState<number>(0);
  const [promoSuccessMsg, setPromoSuccessMsg] = useState<string | null>(null);
  const [promoErrorMsg, setPromoErrorMsg] = useState<string | null>(null);

  // Checkout state
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage((prev) => (prev === msg ? null : prev));
    }, 4000);
  };

  useEffect(() => {
    // Check payment return status from URL query/hash parameters
    const queryStr = window.location.search || (window.location.hash.includes('?') ? window.location.hash.substring(window.location.hash.indexOf('?')) : '');
    const searchParams = new URLSearchParams(queryStr);
    const paymentStatus = searchParams.get('payment_status');
    const refId = searchParams.get('ref_id');
    const error = searchParams.get('error');

    if (paymentStatus === 'success') {
      confetti({
        particleCount: 80,
        spread: 60,
        origin: { y: 0.6 }
      });
      showToast(`پرداخت شما با موفقیت انجام شد. شماره پیگیری: ${refId || 'ثبت شده'}`);
      if (window.history.replaceState) {
        window.history.replaceState(null, '', window.location.pathname + window.location.hash.split('?')[0]);
      }
    } else if (paymentStatus === 'cancelled') {
      showToast('عملیات پرداخت توسط کاربر لغو گردید.');
      if (window.history.replaceState) {
        window.history.replaceState(null, '', window.location.pathname + window.location.hash.split('?')[0]);
      }
    } else if (paymentStatus === 'failed') {
      showToast(`پرداخت ناموفق بود: ${error || 'خطا در تایید تراکنش بانکی'}`);
      if (window.history.replaceState) {
        window.history.replaceState(null, '', window.location.pathname + window.location.hash.split('?')[0]);
      }
    }

    let isMounted = true;
    setIsLoading(true);

    Promise.all([subscriptionApi.getPlans(), subscriptionApi.getStatus()])
      .then(([plansData, statusData]) => {
        if (!isMounted) return;
        // Merge or fallback to prompt plans if server plans are missing or empty
        const effectivePlans = plansData && plansData.length > 0 ? plansData : defaultFallbackPlans;
        setPlans(effectivePlans);
        setStatus(statusData);
        if (effectivePlans.length > 0) {
          const popular = effectivePlans.find((p) => p.badge?.includes('محبوب')) || effectivePlans[1] || effectivePlans[0];
          setSelectedPlanId(popular.id);
        }
        setIsLoading(false);
      })
      .catch((err) => {
        if (!isMounted) return;
        console.warn('Failed to load live subscription data, using fallbacks', err);
        setPlans(defaultFallbackPlans);
        setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const selectedPlan = plans.find((p) => p.id === selectedPlanId) || plans[0];

  // Calculate final price with discount
  const originalPrice = selectedPlan ? selectedPlan.priceTomans : 0;
  const discountedPrice = appliedDiscountPercent > 0
    ? Math.round(originalPrice * (1 - appliedDiscountPercent / 100))
    : originalPrice;

  // Handle Apply Promo Code
  const handleApplyPromo = async (e?: React.FormEvent) => {
    e?.preventDefault();
    const cleanCode = promoCodeInput.trim();
    if (!cleanCode || isValidatingPromo) return;

    setIsValidatingPromo(true);
    setPromoErrorMsg(null);
    setPromoSuccessMsg(null);

    try {
      const res = await subscriptionApi.validatePromo({
        promoCode: cleanCode,
        planId: selectedPlanId,
      });

      if (res.discountPercent > 0) {
        setAppliedDiscountPercent(res.discountPercent);
        setPromoSuccessMsg(res.userMessage || `${res.discountPercent.toLocaleString('fa-IR')}٪ تخفیف با موفقیت اعمال شد!`);
        confetti({
          particleCount: 40,
          spread: 60,
          origin: { y: 0.7 },
        });
      } else {
        setPromoErrorMsg(res.message || 'کد تخفیف وارد شده معتبر نمی‌باشد.');
      }
    } catch (err: any) {
      setPromoErrorMsg(err?.message || 'کد تخفیف نامعتبر است یا منقضی شده است.');
    } finally {
      setIsValidatingPromo(false);
    }
  };

  // Handle Checkout
  const handleCheckout = async () => {
    if (!isLoggedIn) {
      setIsAuthModalOpen(true);
      return;
    }

    if (!selectedPlan || isCheckingOut) return;

    setIsCheckingOut(true);
    try {
      const res = await subscriptionApi.createDirectCheckout({
        planId: selectedPlan.id,
        promoCode: appliedDiscountPercent > 0 ? promoCodeInput.trim() : null,
      });

      if (res.paymentUrl) {
        showToast('در حال انتقال به درگاه امن بانکی شاپرک...');
        setTimeout(() => {
          window.location.href = res.paymentUrl;
        }, 800);
      } else {
        showToast('خطا در دریافت لینک پرداخت. لطفاً با پشتیبانی تماس بگیرید.');
        setIsCheckingOut(false);
      }
    } catch (err: any) {
      console.error('Checkout failed', err);
      showToast(err?.message || 'خطا در اتصال به درگاه پرداخت. لطفاً دقایقی دیگر تلاش فرمایید.');
      setIsCheckingOut(false);
    }
  };

  const perks = [
    {
      icon: <Sparkles className="w-5 h-5 text-apple-yellow" />,
      title: 'دسترسی نامحدود به تمامی مراحل سفر داستانی',
      desc: 'آزادسازی سناریوهای حرفه‌ای در فرودگاه لندن، رزرو هتل، خرید، کافه و مصاحبه کاری',
    },
    {
      icon: <MessageSquare className="w-5 h-5 text-apple-purple" />,
      title: 'مکالمه بدون وقفه و نامحدود با هوش مصنوعی',
      desc: 'پاسخ‌دهی هوشمند به جملات شما بدون محدودیت دور مکالمه به همراه اصلاح گرامری بلادرنگ',
    },
    {
      icon: <Headphones className="w-5 h-5 text-apple-blue" />,
      title: 'تحلیل هوشمند ساختار جملات و اصلاح گرامر',
      desc: 'تبدیل دقیق صوت به متن و دریافت بازخورد هوشمند روی جملات و اصلاح ساختار مکالمه',
    },
    {
      icon: <Zap className="w-5 h-5 text-pink-400" />,
      title: 'سرعت فوق‌العاده بالا روی سرورهای اختصاصی VIP',
      desc: 'پردازش با کمترین تأخیر و بالاترین کیفیت صدا روی زیرساخت پردازشی اختصاصی',
    },
  ];

  return (
    <div className="flex flex-col min-h-screen bg-[#0F1015] text-white selection:bg-apple-blue selection:text-white pb-36 select-none" dir="rtl">
      {/* 1. Cupertino Top App Bar with RTL Back Button (Chevron pointing right >) */}
      <CupertinoNavBar
        title="اشتراک ویژه"
        subtitle="دسترسی نامحدود به تمامی امکانات"
        onBack={() => navigate(-1)}
        backLabel="بازگشت"
      />

      <main className="flex-1 max-w-lg w-full mx-auto px-4 pt-4 space-y-5">
        {/* Active Subscriber Status Notice */}
        {status?.isSubscriber && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="p-4 rounded-[22px] apple-glass bg-gradient-to-r from-apple-green/20 via-emerald-600/15 to-transparent border border-apple-green/35 space-y-1"
          >
            <div className="flex items-center gap-2 text-apple-green text-sm font-bold">
              <CheckCircle2 className="w-5 h-5 shrink-0" />
              <span>اشتراک ویژه شما فعال است!</span>
            </div>
            <p className="text-xs text-slate-200">
              {status.remainingDays > 0
                ? `${status.remainingDays.toLocaleString('fa-IR')} روز از اعتبار طرح شما باقی مانده است.`
                : 'اشتراک نامحدود شما فعال می‌باشد.'}
            </p>
          </motion.div>
        )}

        {/* 2. Golden Hero Crown Banner (Compact Apple Glass) */}
        <div className="relative p-4 rounded-[22px] apple-glass-card overflow-hidden text-center space-y-2 shadow-apple-card">
          <div className="absolute top-0 right-1/2 translate-x-1/2 w-40 h-24 bg-apple-yellow/20 rounded-full blur-3xl pointer-events-none" />

          {/* Glowing Crown Badge */}
          <div className="relative inline-flex items-center justify-center w-11 h-11 rounded-[16px] bg-gradient-to-br from-[#FFC83D] via-[#FFB300] to-[#D97706] border-2 border-white/40 shadow-[0_0_16px_rgba(255,200,61,0.4)]">
            <Crown className="w-6 h-6 fill-white text-white drop-shadow-[0_2px_6px_rgba(0,0,0,0.3)]" />
          </div>

          <div className="space-y-1">
            <h1 className="text-lg font-extrabold text-white tracking-tight">
              دسترسی نامحدود به AISpeaking Plus
            </h1>
            <p className="text-xs text-apple-gray-300 font-sans max-w-sm mx-auto leading-relaxed">
              تمامی مراحل، سناریوهای پیشرفته و هوش مصنوعی بدون وقفه را باز کنید
            </p>
          </div>
        </div>

        {/* 3. Subscription Plans (3 Distinct Apple Glass Cards) */}
        <div className="space-y-3">
          <div className="flex items-center justify-between px-1">
            <h2 className="text-xs font-bold text-apple-gray-300">انتخاب دوره اشتراک:</h2>
            <span className="text-[11px] text-apple-yellow font-semibold">تخفیف ویژه جشنواره</span>
          </div>

          {isLoading ? (
            <div className="space-y-2.5">
              <CupertinoSkeleton className="h-24 rounded-[22px]" />
              <CupertinoSkeleton className="h-24 rounded-[22px]" />
              <CupertinoSkeleton className="h-24 rounded-[22px]" />
            </div>
          ) : (
            <div className="space-y-2.5">
              {plans.map((plan) => {
                const isSelected = plan.id === selectedPlanId;
                const dailyPrice = Math.round(plan.priceTomans / (plan.durationDays || 30));

                return (
                  <motion.div
                    key={plan.id}
                    onClick={() => setSelectedPlanId(plan.id)}
                    whileHover={{ scale: 1.01 }}
                    whileTap={{ scale: 0.98 }}
                    className={`relative p-4 rounded-[22px] cursor-pointer transition-all duration-200 border ${
                      isSelected
                        ? 'apple-glass-card border-apple-yellow bg-apple-yellow/10 ring-2 ring-apple-yellow/30 shadow-[0_8px_32px_rgba(255,204,0,0.18)]'
                        : 'apple-glass border-white/10 hover:border-white/20'
                    }`}
                  >
                    {/* Badge */}
                    {plan.badge && (
                      <span className={`absolute -top-2.5 left-4 px-2.5 py-0.5 rounded-full font-extrabold text-[10px] shadow-sm ${
                        plan.badge.includes('محبوب')
                          ? 'bg-gradient-to-r from-apple-yellow to-apple-orange text-black'
                          : 'bg-gradient-to-r from-apple-blue to-purple-600 text-white'
                      }`}>
                        {plan.badge}
                      </span>
                    )}

                    <div className="flex items-center justify-between">
                      {/* Radio & Title Info (Right in RTL) */}
                      <div className="flex items-center gap-3.5">
                        {/* Radio Check Circle */}
                        <div
                          className={`w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all ${
                            isSelected
                              ? 'border-apple-yellow bg-gradient-to-br from-apple-yellow to-amber-500 shadow-sm'
                              : 'border-white/30 bg-white/5'
                          }`}
                        >
                          {isSelected && <Check className="w-3.5 h-3.5 text-black stroke-[3]" />}
                        </div>

                        <div className="space-y-0.5">
                          <h3 className="text-sm font-bold text-white flex items-center gap-1.5">
                            <span>{plan.titleFa}</span>
                          </h3>
                          <p className="text-[11px] text-apple-gray-300">
                            {plan.durationDays.toLocaleString('fa-IR')} روز دسترسی نامحدود
                          </p>
                          {plan.durationDays >= 60 ? (
                            <p className={`text-[10px] font-semibold ${isSelected ? 'text-apple-yellow' : 'text-apple-gray-300'}`}>
                              ماهی فقط {Math.round(plan.priceTomans / (plan.durationDays / 30)).toLocaleString('fa-IR')} تومان
                            </p>
                          ) : (
                            <p className="text-[10px] text-apple-gray-400">
                              روزی {dailyPrice.toLocaleString('fa-IR')} تومان
                            </p>
                          )}
                        </div>
                      </div>

                      {/* Pricing Info (Left in RTL) */}
                      <div className="text-left font-mono space-y-0.5">
                        <div className="flex items-baseline gap-1 justify-end">
                          <span className={`text-base font-extrabold ${isSelected ? 'text-apple-yellow' : 'text-white'}`}>
                            {plan.priceTomans.toLocaleString('fa-IR')}
                          </span>
                          <span className="text-[11px] font-sans font-normal text-apple-gray-300">
                            تومان
                          </span>
                        </div>
                        {plan.originalPriceTomans && plan.originalPriceTomans > plan.priceTomans && (
                          <div className="text-[11px] text-apple-gray-400 line-through text-left font-sans">
                            {plan.originalPriceTomans.toLocaleString('fa-IR')} تومان
                          </div>
                        )}
                        {plan.discountPercent && plan.discountPercent > 0 && (
                          <span className="inline-block text-[10px] font-bold px-1.5 py-0.5 rounded-md bg-apple-green/20 text-apple-green">
                            {plan.discountPercent.toLocaleString('fa-IR')}٪ تخفیف
                          </span>
                        )}
                      </div>
                    </div>
                  </motion.div>
                );
              })}
            </div>
          )}
        </div>

        {/* 4. Promo Code Field */}
        <div className="rounded-[22px] apple-glass p-4 space-y-3 shadow-sm">
          <div className="flex items-center gap-2 text-xs font-bold text-apple-gray-300">
            <Tag className="w-4 h-4 text-apple-yellow" />
            <span>کد تخفیف دارید؟</span>
          </div>

          <form onSubmit={handleApplyPromo} className="flex items-center gap-2">
            <input
              type="text"
              value={promoCodeInput}
              onChange={(e) => setPromoCodeInput(e.target.value.toUpperCase())}
              placeholder="مثلاً: VIP10 یا NOWRUZ"
              dir="ltr"
              className="flex-1 bg-white/10 border border-white/20 focus:border-apple-yellow rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-apple-gray-400 font-mono tracking-wider focus:outline-none transition-all"
            />
            <button
              type="submit"
              disabled={!promoCodeInput.trim() || isValidatingPromo}
              className="px-4 py-2.5 rounded-xl bg-apple-blue hover:bg-blue-600 disabled:opacity-40 text-white font-bold text-xs shadow-md transition-all shrink-0 active:scale-95"
            >
              {isValidatingPromo ? 'بررسی...' : 'اعمال'}
            </button>
          </form>

          {promoSuccessMsg && (
            <p className="text-xs text-apple-green font-semibold flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
              <span>{promoSuccessMsg}</span>
            </p>
          )}

          {promoErrorMsg && (
            <p className="text-xs text-red-400 font-semibold">{promoErrorMsg}</p>
          )}
        </div>

        {/* 5. Benefits List (Matches Compose SubscriptionBenefitsList) */}
        <div className="rounded-[22px] apple-glass-card p-4 space-y-3.5 shadow-apple-card">
          <h2 className="text-sm font-bold text-white">
            مزایای اختصاصی اشتراک ویژه
          </h2>

          <div className="space-y-3">
            {perks.map((perk, i) => (
              <div
                key={i}
                className="flex items-start gap-3 p-3 rounded-2xl bg-white/5 border border-white/10"
              >
                <div className="p-2.5 rounded-xl bg-white/10 shrink-0">{perk.icon}</div>
                <div className="space-y-0.5">
                  <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5 text-apple-green shrink-0" />
                    <span>{perk.title}</span>
                  </h4>
                  <p className="text-[11px] text-apple-gray-300 leading-relaxed font-sans">
                    {perk.desc}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 6. Trust Badges Row (Matches Compose TrustBadgesRow) */}
        <div className="grid grid-cols-3 gap-2 pt-1 text-center">
          <div className="p-3 rounded-2xl apple-glass flex flex-col items-center gap-1">
            <Zap className="w-5 h-5 text-apple-yellow" />
            <span className="text-[11px] font-bold text-white">فعال‌سازی آنی</span>
          </div>

          <div className="p-3 rounded-2xl apple-glass flex flex-col items-center gap-1">
            <CreditCard className="w-5 h-5 text-apple-blue" />
            <span className="text-[11px] font-bold text-white">تراکنش امن شاپرک</span>
          </div>

          <div className="p-3 rounded-2xl apple-glass flex flex-col items-center gap-1">
            <Clock className="w-5 h-5 text-apple-green" />
            <span className="text-[11px] font-bold text-white">پشتیبانی ۲۴ ساعته</span>
          </div>
        </div>
      </main>

      {/* 7. Fixed Bottom Checkout Dock (visionOS / Apple Glass Style) */}
      <div className="fixed bottom-0 inset-x-0 apple-glass-dock p-4 z-40">
        <div className="max-w-lg mx-auto flex items-center justify-between gap-4">
          {/* Price Preview */}
          <div className="space-y-0.5">
            <span className="text-[11px] text-apple-gray-300 block">مبلغ قابل پرداخت:</span>
            <div className="flex items-baseline gap-1 font-mono">
              <span className="text-lg font-extrabold text-apple-yellow">
                {discountedPrice.toLocaleString('fa-IR')}
              </span>
              <span className="text-xs text-apple-gray-300 font-sans">تومان</span>
              {appliedDiscountPercent > 0 && (
                <span className="text-xs text-apple-gray-400 line-through mr-1">
                  {originalPrice.toLocaleString('fa-IR')}
                </span>
              )}
            </div>
          </div>

          {/* Action CTA Button */}
          <motion.button
            type="button"
            disabled={isCheckingOut || isLoading}
            onClick={handleCheckout}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.97 }}
            className="flex-1 inline-flex items-center justify-center gap-2 py-3 px-4 rounded-2xl bg-gradient-to-r from-apple-yellow via-amber-400 to-amber-500 hover:brightness-105 active:scale-[0.98] text-black font-extrabold text-sm shadow-[0_4px_20px_rgba(255,204,0,0.35)] disabled:opacity-50 transition-all"
          >
            <Crown className="w-5 h-5 fill-black text-black shrink-0" />
            <span>
              {isCheckingOut ? 'در حال انتقال به درگاه...' : 'خرید اشتراک و شروع یادگیری'}
            </span>
          </motion.button>
        </div>
      </div>

      {/* Login Dialog for Unauthenticated Users */}
      <LoginDialog
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={() => {
          showToast('ورود موفقیت‌آمیز بود! اکنون می‌توانید پرداخت را تکمیل نمایید.');
        }}
      />

      {/* Toast Notification */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div
            initial={{ opacity: 0, y: 24, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 16, scale: 0.95 }}
            className="fixed top-20 inset-x-4 max-w-sm mx-auto z-50 pointer-events-none"
          >
            <div className="apple-glass bg-[#1b1e2c]/95 border border-white/20 text-white text-xs px-4 py-3 rounded-2xl shadow-apple-lg flex items-center gap-2 pointer-events-auto">
              <Sparkles className="w-4 h-4 text-apple-yellow shrink-0" />
              <span>{toastMessage}</span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default SubscriptionPage;
