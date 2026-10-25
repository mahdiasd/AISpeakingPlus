import React from 'react';
import { Target, Mic, Trophy } from 'lucide-react';

export const LandingHowItWorks: React.FC = () => {
  const steps = [
    {
      num: '۰۱',
      icon: Target,
      title: 'انتخاب موقعیت و سناریو',
      desc: 'از میان موضوعات ملموس مانند سفر، هتل، خرید، کار یا مکالمات روزمره، سناریوی مورد نظر خود را انتخاب کنید.',
    },
    {
      num: '۰۲',
      icon: Mic,
      title: 'صحبت کردن با صدای خود',
      desc: 'دکمه میکروفون را لمس کنید و به انگلیسی صحبت کنید؛ کاراکتر هوش مصنوعی گفته شما را می‌شنود و با صدای طبیعی پاسخ می‌دهد.',
    },
    {
      num: '۰۳',
      icon: Trophy,
      title: 'دریافت بازخورد و ارتقای مهارت',
      desc: 'پس از پایان مکالمه، اصلاحات گرامری، تلفظ واژگان و پیشنهادهای کاربردی را بررسی کنید تا در گفتگوی بعدی مسلط‌تر صحبت کنید.',
    },
  ];

  return (
    <section id="how-it-works" className="py-20 bg-[#0E1422]/30 border-t border-slate-800/80">
      <div className="max-w-7xl mx-auto px-5 sm:px-8">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <h2 className="text-2xl sm:text-4xl font-bold text-white mb-3">
            چگونه در ۳ گام ساده مکالمه را آغاز کنیم؟
          </h2>
          <p className="text-slate-400 text-sm sm:text-base">
            شروع تمرین در AISpeakingPlus سریع و بدون کوچک‌ترین پیچیدگی است.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
          {steps.map((s, idx) => {
            const Icon = s.icon;
            return (
              <div
                key={idx}
                className="relative p-6 sm:p-7 rounded-2xl bg-[#111726]/80 border border-slate-800 hover:border-indigo-500/30 transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-5">
                    <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center">
                      <Icon className="w-6 h-6" />
                    </div>
                    <span className="text-3xl font-black text-slate-700/60 font-inter">
                      {s.num}
                    </span>
                  </div>
                  <h3 className="text-base sm:text-lg font-bold text-white mb-2.5">
                    {s.title}
                  </h3>
                  <p className="text-xs sm:text-sm text-slate-400 leading-relaxed font-normal">
                    {s.desc}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};
