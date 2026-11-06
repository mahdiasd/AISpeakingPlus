import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Crown,
  CheckCircle2,
  Sparkles,
  ShieldCheck,
  CreditCard,
  MessageSquare,
  Headphones,
  Award,
  ArrowRight,
  Tag,
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { motion, AnimatePresence } from 'motion/react';
import {
  CupertinoNavBar,
  CupertinoCard,
  CupertinoButton,
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
  const [selectedPlanId, setSelectedPlanId] = useState<string>('quarterly_plan');
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
    let isMounted = true;
    setIsLoading(true);

    Promise.all([subscriptionApi.getPlans(), subscriptionApi.getStatus()])
      .then(([plansData, statusData]) => {
        if (!isMounted) return;
        setPlans(plansData);
        setStatus(statusData);
        if (plansData.length > 0) {
          // Default to middle or popular plan
          const popular = plansData.find((p) => p.badge?.includes('محبوب')) || plansData[1] || plansData[0];
          setSelectedPlanId(popular.id);
        }
        setIsLoading(false);
      })
      .catch((err) => {
        if (!isMounted) return;
        console.warn('Failed to load live subscription data', err);
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
        setPromoSuccessMsg(res.userMessage || `${res.discountPercent}٪ تخفیف با موفقیت اعمال شد!`);
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
      title: 'دسترسی کامل به تمامی ۳۰ مرحله سناریو محور',
      desc: 'آزادسازی سناریوهای حرفه‌ای در فرودگاه لندن، رزرو هتل، خرید، کافه و مصاحبه کاری',
    },
    {
      icon: <MessageSquare className="w-5 h-5 text-apple-blue" />,
      title: 'مکالمه زنده و بدون محدودیت با هوش مصنوعی',
      desc: 'پاسخ‌دهی هوشمند به جملات شما بدون محدودیت دور مکالمه به همراه اصلاح گرامری بلادرنگ',
    },
    {
      icon: <Headphones className="w-5 h-5 text-apple-teal" />,
      title: 'تلفظ فوق‌طبیعی با لهجه بومی (Native British Audio)',
      desc: 'شنیدن صدای مکالمه با لهجه استاندارد بریتانیایی جهت تقویت مهارت شنیداری (Listening)',
    },
    {
      icon: <Award className="w-5 h-5 text-apple-purple" />,
      title: 'کارنامه دقیق و ارزیابی نمره CEFR',
      desc: 'تحلیل خطاهای گرامری، تنوع واژگان و اکسنت پس از پایان هر گفتگو با نمودار پیشرفت',
    },
  ];

  return (
    <div className="flex flex-col min-h-screen bg-[#0c0c0e] text-white selection:bg-apple-blue selection:text-white pb-36">
      {/* 1. Cupertino Top App Bar */}
      <CupertinoNavBar
        title="ارتقا به نسخه VIP"
        subtitle="دسترسی نامحدود به تمامی امکانات"
        onBack={() => navigate(-1)}
      />

      <main className="flex-1 max-w-lg w-full mx-auto px-4 pt-4 space-y-5">
        {/* Active Subscriber Status Notice */}
        {status?.isSubscriber && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="p-4 rounded-3xl bg-gradient-to-r from-apple-green/20 via-emerald-600/15 to-transparent border border-apple-green/30 space-y-1"
          >
            <div className="flex items-center gap-2 text-apple-green text-sm font-bold">
              <CheckCircle2 className="w-5 h-5 shrink-0" />
              <span>اشتراک شما فعال است!</span>
            </div>
            <p className="text-xs text-slate-200">
              {status.remainingDays > 0
                ? `${status.remainingDays} روز از اعتبار طرح شما باقی مانده است.`
                : 'اشتراک نامحدود شما فعال می‌باشد.'}
            </p>
          </motion.div>
        )}

        {/* 2. Golden Hero Crown Banner */}
        <div className="relative p-6 rounded-3xl apple-glass bg-gradient-to-br from-apple-yellow/20 via-apple-orange/10 to-[#14151f] border border-apple-yellow/30 overflow-hidden text-center space-y-3 shadow-apple-lg">
          <div className="absolute top-0 right-1/2 translate-x-1/2 w-48 h-32 bg-apple-yellow/20 rounded-full blur-3xl pointer-events-none" />

          <div className="relative inline-flex items-center justify-center w-16 h-16 rounded-full bg-apple-yellow/20 border border-apple-yellow/40 shadow-[0_0_24px_rgba(255,204,0,0.4)]">
            <Crown className="w-9 h-9 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_10px_rgba(255,204,0,0.8)]" />
          </div>

          <div className="space-y-1">
            <h1 className="text-lg font-extrabold text-white">عضویت ویژه طلایی (VIP)</h1>
            <p className="text-xs text-apple-yellow font-semibold">
              کلید تسلط روان بر مکالمه انگلیسی با جدیدترین مدل‌های هوش مصنوعی
            </p>
          </div>
        </div>

        {/* 3. Subscription Plans List */}
        <div className="space-y-3">
          <div className="flex items-center justify-between px-1">
            <h2 className="text-xs font-bold text-apple-gray-300">انتخاب دوره اشتراک:</h2>
            <span className="text-[11px] text-apple-yellow font-semibold">تخفیف ویژه جشنواره</span>
          </div>

          {isLoading ? (
            <div className="space-y-2.5">
              <CupertinoSkeleton className="h-24 rounded-3xl" />
              <CupertinoSkeleton className="h-24 rounded-3xl" />
              <CupertinoSkeleton className="h-24 rounded-3xl" />
            </div>
          ) : (
            <div className="space-y-2.5">
              {plans.map((plan) => {
                const isSelected = plan.id === selectedPlanId;
                const monthlyEquiv = Math.round(plan.priceTomans / (plan.durationDays / 30));

                return (
                  <div
                    key={plan.id}
                    onClick={() => setSelectedPlanId(plan.id)}
                    className={`relative p-4 rounded-3xl apple-glass cursor-pointer transition-all duration-200 border ${
                      isSelected
                        ? 'border-apple-yellow/80 bg-apple-yellow/10 ring-2 ring-apple-yellow/30 shadow-apple-md'
                        : 'border-white/10 bg-white/5 hover:bg-white/10'
                    }`}
                  >
                    {/* Badge */}
                    {plan.badge && (
                      <span className="absolute -top-2.5 left-4 px-2.5 py-0.5 rounded-full bg-gradient-to-r from-apple-yellow to-apple-orange text-black font-extrabold text-[10px] shadow-sm">
                        {plan.badge}
                      </span>
                    )}

                    <div className="flex items-center justify-between">
                      {/* Radio & Title */}
                      <div className="flex items-center gap-3">
                        <div
                          className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all ${
                            isSelected
                              ? 'border-apple-yellow bg-apple-yellow'
                              : 'border-apple-gray-400 bg-transparent'
                          }`}
                        >
                          {isSelected && <div className="w-2 h-2 rounded-full bg-black" />}
                        </div>

                        <div>
                          <h3 className="text-sm font-bold text-white">{plan.titleFa}</h3>
                          <span className="text-[11px] text-apple-gray-400">
                            معادل ماهانه {monthlyEquiv.toLocaleString('fa-IR')} تومان
                          </span>
                        </div>
                      </div>

                      {/* Pricing */}
                      <div className="text-left font-mono">
                        <div className="text-base font-extrabold text-apple-yellow">
                          {plan.priceTomans.toLocaleString('fa-IR')}
                          <span className="text-[11px] font-sans font-normal mr-1 text-apple-gray-300">
                            تومان
                          </span>
                        </div>
                        {plan.discountPercent && plan.discountPercent > 0 && (
                          <span className="text-[10px] text-apple-green font-bold block">
                            {plan.discountPercent}٪ تخفیف
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* 4. Promo Code Box */}
        <CupertinoCard variant="glass" className="space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold text-apple-gray-300">
            <Tag className="w-4 h-4 text-apple-yellow" />
            <span>کد تخفیف دارید؟</span>
          </div>

          <form onSubmit={handleApplyPromo} className="flex items-center gap-2">
            <input
              type="text"
              value={promoCodeInput}
              onChange={(e) => setPromoCodeInput(e.target.value.toUpperCase())}
              placeholder="مثلاً: NOWRUZ یا VIP50"
              dir="ltr"
              className="flex-1 bg-white/10 border border-white/15 focus:border-apple-yellow rounded-2xl px-3.5 py-2.5 text-xs text-white placeholder-apple-gray-400 font-mono tracking-wider focus:outline-none transition-all"
            />
            <CupertinoButton
              variant="tinted"
              size="md"
              type="submit"
              disabled={!promoCodeInput.trim() || isValidatingPromo}
            >
              {isValidatingPromo ? 'بررسی...' : 'اعمال کد'}
            </CupertinoButton>
          </form>

          {promoSuccessMsg && (
            <p className="text-xs text-apple-green font-semibold flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>{promoSuccessMsg}</span>
            </p>
          )}

          {promoErrorMsg && (
            <p className="text-xs text-apple-red font-semibold">{promoErrorMsg}</p>
          )}
        </CupertinoCard>

        {/* 5. Perks Grid */}
        <div className="space-y-2.5 pt-1">
          <h2 className="text-xs font-bold text-apple-gray-300 px-1">
            امکانات همراه با اشتراک ویژه:
          </h2>

          <div className="space-y-2.5">
            {perks.map((perk, i) => (
              <div
                key={i}
                className="flex items-start gap-3 p-3.5 rounded-3xl bg-white/5 border border-white/10 apple-glass"
              >
                <div className="p-2.5 rounded-2xl bg-white/10 shrink-0">{perk.icon}</div>
                <div className="space-y-0.5">
                  <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5 text-apple-green shrink-0" />
                    <span>{perk.title}</span>
                  </h4>
                  <p className="text-[11px] text-apple-gray-400 leading-relaxed font-sans">
                    {perk.desc}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 6. Trust Badges */}
        <div className="grid grid-cols-2 gap-2.5 pt-1 text-center">
          <div className="p-3.5 rounded-3xl bg-white/5 border border-white/10 flex flex-col items-center gap-1">
            <ShieldCheck className="w-6 h-6 text-apple-green" />
            <span className="text-xs font-bold text-white">ضمانت بازگشت وجه</span>
            <span className="text-[10px] text-apple-gray-400">تا ۷ روز در صورت عدم رضایت</span>
          </div>

          <div className="p-3.5 rounded-3xl bg-white/5 border border-white/10 flex flex-col items-center gap-1">
            <CreditCard className="w-6 h-6 text-apple-blue" />
            <span className="text-xs font-bold text-white">درگاه بانکی امن</span>
            <span className="text-[10px] text-apple-gray-400">پرداخت مستقیم شاپرک / زرین‌پال</span>
          </div>
        </div>
      </main>

      {/* 7. Sticky Bottom Checkout Dock */}
      <div className="fixed bottom-0 inset-x-0 bg-[#0e1017]/95 backdrop-blur-2xl border-t border-white/15 p-4 z-40">
        <div className="max-w-lg mx-auto flex items-center justify-between gap-4">
          {/* Price Preview */}
          <div className="space-y-0.5">
            <span className="text-[11px] text-apple-gray-400 block">مبلغ قابل پرداخت:</span>
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

          {/* Action Button */}
          <CupertinoButton
            variant="filled"
            size="lg"
            disabled={isCheckingOut || isLoading}
            onClick={handleCheckout}
            className="flex-1 bg-gradient-to-r from-apple-yellow via-apple-orange to-amber-600 text-black font-extrabold shadow-[0_4px_16px_rgba(255,204,0,0.3)] hover:brightness-105 active:scale-[0.98]"
          >
            <span>{isCheckingOut ? 'در حال اتصال به بانک...' : 'پرداخت و ارتقا آنی'}</span>
            <ArrowRight className="w-4 h-4 rotate-180 mr-1.5 text-black" />
          </CupertinoButton>
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
