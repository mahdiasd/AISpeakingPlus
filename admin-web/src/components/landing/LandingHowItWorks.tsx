import React from 'react';
import { Target, Mic, Trophy } from 'lucide-react';

export const LandingHowItWorks: React.FC = () => {
  const steps = [
    {
      num: '۰۱',
      icon: Target,
      title: 'انتخاب سناریو و هدف گفتگو',
      desc: 'از بین موضوعات واقعی سفر، مهاجرت، کار یا گفتگوهای دوستانه، سناریوی مدنظر خود را انتخاب کنید.',
    },
    {
      num: '۰۲',
      icon: Mic,
      title: 'مکالمه صوتی طبیعی با هوش مصنوعی',
      desc: 'دکمه میکروفون را بفشارید و به انگلیسی صحبت کنید؛ کاراکتر هوش مصنوعی صدای شما را تحلیل کرده و در لحظه صوتی پاسخ می‌دهد.',
    },
    {
      num: '۰۳',
      icon: Trophy,
      title: 'دریافت فیدبک تلفظ و ارتقای مهارت',
      desc: 'پس از پایان دور، اشکالات گرامری، نمره روانی کلام و اصطلاحات نیتیو را مشاهده کنید تا جلسه بعد با تسلط بیشتر صحبت کنید.',
    },
  ];

  return (
    <section id="how-it-works" className="py-20 bg-[#0E1422]/30 border-t border-slate-800/80">
      <div className="max-w-7xl mx-auto px-5 sm:px-8">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <h2 className="text-2xl sm:text-4xl font-bold text-white mb-3">
            چطور در ۳ مرحله مکالمه را شروع کنیم؟
          </h2>
          <p className="text-slate-400 text-sm sm:text-base">
            شروع یادگیری با AISpeakingPlus ساده‌تر از آن چیزی است که فکر می‌کنید.
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
