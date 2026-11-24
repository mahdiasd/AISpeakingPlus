import React from 'react';
import { Compass, Mic, Layers, Globe } from 'lucide-react';

const STATS = [
  {
    icon: Compass,
    value: '۳۱ مرحلهٔ داستانی',
    label: 'سفر و زندگی دانشجویی در لندن در ۶ فاز؛ از فرودگاه تا دانشگاه و کار.',
    color: 'text-indigo-400',
  },
  {
    icon: Mic,
    value: 'گفتگوی صوتی و متنی',
    label: 'با میکروفون حرف بزن یا تایپ کن و جواب کاراکتر داستان رو با صدای طبیعی بشنو.',
    color: 'text-cyan-400',
  },
  {
    icon: Layers,
    value: 'جعبه لایتنر ۵ خانه‌ای',
    label: 'کلمه‌های تازه رو از وسط مکالمه معنی کن و با مرور زمان‌بندی‌شده تو ذهنت تثبیت کن.',
    color: 'text-amber-400',
  },
  {
    icon: Globe,
    value: 'همیشه در دسترس',
    label: 'مستقیم تو مرورگر گوشی، تبلت یا کامپیوتر باز می‌شه و نیازی به نصب نداره.',
    color: 'text-emerald-400',
  },
];

export const LandingStats: React.FC = () => {
  return (
    <section className="relative z-20 pt-14 sm:pt-20 pb-4 max-w-6xl mx-auto px-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 p-4 sm:p-5 rounded-3xl apple-glass shadow-2xl">
        {STATS.map((item) => {
          const Icon = item.icon;
          return (
            <div
              key={item.value}
              className="flex items-start gap-3.5 p-3.5 rounded-2xl bg-white/[0.03] border border-white/5 hover:border-white/15 transition-all"
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
