import React from 'react';
import { Volume2, Zap, Compass, Globe } from 'lucide-react';

export const LandingStats: React.FC = () => {
  const stats = [
    {
      icon: Volume2,
      value: '۱۱ صدای طبیعی و زنده',
      label: 'شخصیت‌های صوتی متنوع با لهجه‌های اصیل آمریکایی و بریتانیایی',
      color: 'text-cyan-400',
    },
    {
      icon: Zap,
      value: 'پاسخ فوری و روان',
      label: 'گفتگوی پیوسته در لحظه و بدون وقفه، درست مانند هم‌صحبتی با یک انسان',
      color: 'text-amber-400',
    },
    {
      icon: Compass,
      value: 'سناریوهای کاربردی',
      label: 'تمرین موقعیت‌های ملموس سفر، هتل، خرید، کافه و مصاحبه کاری',
      color: 'text-indigo-400',
    },
    {
      icon: Globe,
      value: 'همیشه در دسترس',
      label: 'اجرای سریع روی مرورگر گوشی، تبلت و لپ‌تاپ بدون نیاز به نصب برنامه',
      color: 'text-emerald-400',
    },
  ];

  return (
    <section className="relative z-20 -mt-10 sm:-mt-14 max-w-6xl mx-auto px-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 p-4 sm:p-5 rounded-2xl bg-[#111726]/90 backdrop-blur-xl border border-white/10 shadow-2xl shadow-black/60">
        {stats.map((item, index) => {
          const Icon = item.icon;
          return (
            <div
              key={index}
              className="flex items-start gap-3.5 p-3.5 rounded-xl bg-white/[0.02] border border-white/5 hover:border-white/15 transition-all"
            >
              <div className={`p-2.5 rounded-xl bg-white/5 border border-white/10 shrink-0 ${item.color}`}>
                <Icon className="w-5 h-5" />
              </div>
              <div>
                <div className="text-base sm:text-lg font-bold text-white mb-1">
                  {item.value}
                </div>
                <div className="text-xs text-slate-400 leading-relaxed font-normal">
                  {item.label}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
};
