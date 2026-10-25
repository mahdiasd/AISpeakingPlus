import React, { useEffect, useState } from 'react';
import { Check, Sparkles, Zap, ShieldCheck, Gift, ArrowLeft, RefreshCw } from 'lucide-react';

interface SubscriptionPlan {
  id: string;
  type: string;
  titleFa: string;
  durationDays: number;
  priceTomans: number;
  discountPercent?: number;
  badge?: string | null;
}

interface LandingPricingProps {
  onLaunchApp: () => void;
}

const DEFAULT_PLANS: SubscriptionPlan[] = [
  {
    id: 'plan-1m',
    type: '1_MONTH',
    titleFa: 'اشتراک ۱ ماهه',
    durationDays: 30,
    priceTomans: 199000,
    discountPercent: 0,
    badge: null,
  },
  {
    id: 'plan-3m',
    type: '3_MONTHS',
    titleFa: 'اشتراک ۳ ماهه',
    durationDays: 90,
    priceTomans: 499000,
    discountPercent: 15,
    badge: 'محبوب‌ترین',
  },
  {
    id: 'plan-6m',
    type: '6_MONTHS',
    titleFa: 'اشتراک ۶ ماهه',
    durationDays: 180,
    priceTomans: 899000,
    discountPercent: 25,
    badge: 'بهترین ارزش',
  },
];

const toPersianDigits = (value: number | string): string => {
  const farsiDigits = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
  return value
    .toString()
    .replace(/[0-9]/g, (d) => farsiDigits[parseInt(d, 10)]);
};

const formatPrice = (tomans: number): string => {
  try {
    return tomans.toLocaleString('fa-IR');
  } catch {
    const parts = tomans.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '٬');
    return toPersianDigits(parts);
  }
};

export const LandingPricing: React.FC<LandingPricingProps> = ({ onLaunchApp }) => {
  const [plans, setPlans] = useState<SubscriptionPlan[]>(DEFAULT_PLANS);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    let isMounted = true;
    const fetchPlans = async () => {
      try {
        setIsLoading(true);
        const res = await fetch('/api/v2/subscriptions/plans');
        if (!res.ok) throw new Error('Failed to fetch plans');
        const json = await res.json();
        if (json?.data && Array.isArray(json.data) && json.data.length > 0 && isMounted) {
          setPlans(json.data);
        }
      } catch {
        // Fallback to default plans already set
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    fetchPlans();
    return () => {
      isMounted = false;
    };
  }, []);

  const featuresList = [
    'دسترسی کامل به سناریوهای سفر، هتل، کافه و مصاحبه کاری',
    'مکالمه صوتی با تمام کاراکترهای زن و مرد (آمریکایی و بریتانیایی)',
    'تحلیل در لحظه گرامر، اشکالات ساختاری و تصحیح هوشمند',
    'ارزیابی هوشمند تلفظ واژگان و روانی کلام',
    'پیشنهاد جملات کاربردی و اصطلاحات متداول حین مکالمه',
    'ذخیره کامل سابقه گفتگوها و نمودار پیشرفت آموزشی',
    'دسترسی همزمان در گوشی موبایل، تبلت و کامپیوتر',
  ];

  return (
    <section id="pricing" className="scroll-mt-24 py-24 max-w-7xl mx-auto px-5 sm:px-8 relative">
      {/* Background glow effects */}
      <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[300px] bg-indigo-600/10 rounded-full blur-[120px] pointer-events-none" />

      {/* Section Header */}
      <div className="text-center max-w-3xl mx-auto mb-14 relative z-10">
        <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold mb-4">
          <Sparkles className="w-3.5 h-3.5" />
          <span>{isLoading ? 'در حال دریافت تعرفه‌ها...' : 'پلن‌ها و تعرفه‌ها'}</span>
        </div>
        <h2 className="text-2xl sm:text-4xl font-bold text-white mb-4">
          سرمایه‌گذاری برای تسلط کامل بر مکالمه انگلیسی
        </h2>
        <p className="text-slate-400 text-sm sm:text-base leading-relaxed">
          بدون نیاز به پرداخت هزینه‌های سنگین کلاس‌های حضوری، هر روز در هر زمان و مکانی با پارتنر هوش مصنوعی مکالمه کنید.
        </p>
      </div>

      {/* Prominent Free Stages Reassurance Card */}
      <div className="relative z-10 max-w-4xl mx-auto mb-14 rounded-2xl bg-gradient-to-r from-emerald-950/40 via-[#111C28] to-cyan-950/40 border border-emerald-500/30 p-6 sm:p-8 shadow-xl">
        <div className="flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-400 shrink-0 mt-0.5">
              <Gift className="w-6 h-6" />
            </div>
            <div>
              <div className="inline-flex items-center gap-2 px-2.5 py-0.5 rounded-full bg-emerald-500/15 text-emerald-300 text-xs font-semibold mb-2 border border-emerald-500/30">
                <span>تست رایگان و بدون ریسک</span>
              </div>
              <h3 className="text-lg sm:text-xl font-bold text-white mb-1.5">
                مرحله اول و دوم کاملاً رایگان است!
              </h3>
              <p className="text-xs sm:text-sm text-slate-300 leading-relaxed font-normal">
                برای ورود و تجربه مکالمه نیازی به پرداخت هزینه یا ثبت کارت بانکی نیست. شما می‌توانید دو مرحله اول تمام سناریوها را با صداهای طبیعی و بازخورد هوشمند رایگان تجربه کنید و پس از اطمینان از کیفیت، اشتراک تهیه فرمایید.
              </p>
            </div>
          </div>

          <button
            onClick={onLaunchApp}
            className="w-full md:w-auto shrink-0 inline-flex items-center justify-center gap-2 px-6 py-3 rounded-full bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-sm shadow-lg shadow-emerald-500/25 transition-all hover:scale-105 cursor-pointer"
          >
            <span>شروع دو مرحله اول رایگان</span>
            <ArrowLeft className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Pricing Cards Grid */}
      <div className={`relative z-10 grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8 max-w-6xl mx-auto transition-opacity duration-300 ${isLoading ? 'opacity-70' : 'opacity-100'}`}>
        {plans.map((plan) => {
          const isFeatured = plan.discountPercent && plan.discountPercent > 0 && plan.durationDays === 90;
          const isBestValue = plan.discountPercent && plan.discountPercent >= 20;
          const originalPrice = plan.discountPercent && plan.discountPercent > 0
            ? Math.round(plan.priceTomans / (1 - plan.discountPercent / 100))
            : null;

          return (
            <div
              key={plan.id}
              className={`rounded-2xl p-6 sm:p-7 flex flex-col justify-between transition-all duration-300 relative ${
                isFeatured
                  ? 'bg-gradient-to-b from-indigo-950/60 via-[#13192B] to-[#0E1422] border-2 border-indigo-500/60 shadow-2xl shadow-indigo-600/20 md:-translate-y-2'
                  : 'bg-[#111726]/80 border border-slate-800 hover:border-slate-700'
              }`}
            >
              {/* Badge on top if present */}
              {plan.badge && (
                <div className="absolute -top-3.5 left-1/2 -translate-x-1/2">
                  <span
                    className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold shadow-md ${
                      isFeatured
                        ? 'bg-indigo-600 text-white'
                        : isBestValue
                        ? 'bg-amber-500 text-slate-950'
                        : 'bg-cyan-500 text-slate-950'
                    }`}
                  >
                    <Zap className="w-3 h-3 fill-current" />
                    <span>{plan.badge}</span>
                  </span>
                </div>
              )}

              <div>
                {/* Title & Duration */}
                <div className="text-center pb-5 border-b border-slate-800/80">
                  <h3 className="text-lg sm:text-xl font-bold text-white mb-1">
                    {plan.titleFa}
                  </h3>
                  <p className="text-xs text-slate-400">
                    دسترسی نامحدود به‌مدت {toPersianDigits(plan.durationDays)} روز
                  </p>
                </div>

                {/* Price Display */}
                <div className="py-6 text-center">
                  {originalPrice && (
                    <div className="text-xs sm:text-sm text-slate-500 line-through mb-1">
                      {formatPrice(originalPrice)} تومان
                    </div>
                  )}
                  <div className="flex items-baseline justify-center gap-1.5">
                    <span className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
                      {formatPrice(plan.priceTomans)}
                    </span>
                    <span className="text-xs text-slate-400 font-medium">تومان</span>
                  </div>

                  {plan.discountPercent && plan.discountPercent > 0 ? (
                    <div className="mt-2.5 inline-block">
                      <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                        {toPersianDigits(plan.discountPercent)}٪ تخفیف ویژه
                      </span>
                    </div>
                  ) : (
                    <div className="mt-2.5 text-[11px] text-slate-500">
                      تعرفه عادی ماهانه
                    </div>
                  )}
                </div>

                {/* Features List */}
                <div className="space-y-3 pt-2 pb-6 border-t border-slate-800/60 text-right">
                  {featuresList.map((feat, idx) => (
                    <div key={idx} className="flex items-start gap-2.5 text-xs sm:text-sm text-slate-300">
                      <div className="w-4 h-4 rounded-full bg-indigo-500/20 text-indigo-400 flex items-center justify-center shrink-0 mt-0.5">
                        <Check className="w-2.5 h-2.5 stroke-[3]" />
                      </div>
                      <span className="leading-relaxed">{feat}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Action Button */}
              <button
                onClick={onLaunchApp}
                className={`w-full py-3 px-4 rounded-xl font-medium text-sm transition-all duration-200 flex items-center justify-center gap-2 cursor-pointer ${
                  isFeatured
                    ? 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30 hover:scale-[1.02]'
                    : 'bg-white/10 hover:bg-white/15 text-white border border-white/15 hover:border-white/30'
                }`}
              >
                <span>انتخاب اشتراک</span>
                <ArrowLeft className="w-4 h-4" />
              </button>
            </div>
          );
        })}
      </div>

      {/* Trust Badges */}
      <div className="relative z-10 mt-14 pt-8 border-t border-slate-800/60 flex flex-wrap items-center justify-center gap-8 text-xs sm:text-sm text-slate-400">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
          <span>پرداخت امن با کلیه کارت‌های عضو شتاب</span>
        </div>
        <div className="flex items-center gap-2">
          <Zap className="w-4 h-4 text-cyan-400" />
          <span>فعال‌سازی آنی اشتراک پس از پرداخت</span>
        </div>
        <div className="flex items-center gap-2">
          <RefreshCw className="w-4 h-4 text-indigo-400" />
          <span>بدون نیاز به تمدید اجباری یا برداشت خودکار</span>
        </div>
      </div>
    </section>
  );
};
