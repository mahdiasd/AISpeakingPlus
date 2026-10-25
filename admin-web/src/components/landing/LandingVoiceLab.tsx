import React from 'react';
import { Volume2, Sparkles, Mic2 } from 'lucide-react';

export const LandingVoiceLab: React.FC = () => {
  const voices = [
    { name: 'Sarah', lang: 'لهجه آمریکایی', gender: 'خانم', desc: 'لحن صمیمی و روان، بسیار مناسب مکالمات روزمره و دوستانه', badge: 'پرطرفدار' },
    { name: 'Nicole', lang: 'لهجه آمریکایی', gender: 'خانم', desc: 'صدای رسا با تلفظ شفاف واژگان، ایده‌آل برای نوآموزان', badge: 'تلفظ شفاف' },
    { name: 'Michael', lang: 'لهجه آمریکایی', gender: 'آقا', desc: 'لحن طبیعی و محاوره‌ای، مناسب موقعیت‌های کار و زندگی', badge: 'محاوره‌ای' },
    { name: 'Adam', lang: 'لهجه آمریکایی', gender: 'آقا', desc: 'صدای متین و جدی، ایده‌آل برای جلسات رسمی و مصاحبه کاری', badge: 'رسمی و کاری' },
    { name: 'George', lang: 'لهجه بریتانیایی', gender: 'آقا', desc: 'لهجه اصیل و شمرده بریتانیایی، با آهنگ کلام آرام و دقیق', badge: 'بریتانیایی اصیل' },
    { name: 'Bella', lang: 'لهجه آمریکایی', gender: 'خانم', desc: 'لحن پرانرژی و شاداب برای ایجاد انگیزه و اشتیاق یادگیری', badge: 'پویا و صمیمی' },
  ];

  return (
    <section id="voice" className="py-24 max-w-7xl mx-auto px-5 sm:px-8">
      <div className="flex flex-col lg:flex-row items-center justify-between gap-12">
        {/* Left Information */}
        <div className="lg:w-1/2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-semibold mb-4">
            <Sparkles className="w-3.5 h-3.5" />
            <span>تنوع صداهای طبیعی و انسانی</span>
          </div>

          <h2 className="text-2xl sm:text-4xl font-bold text-white mb-5 leading-tight">
            با صداهایی گفتگو کنید که <span className="text-cyan-300">کاملاً زنده و طبیعی</span> هستند
          </h2>

          <p className="text-slate-400 text-sm sm:text-base leading-relaxed mb-6">
            دیگر نگران صداهای ماشینی، رباتیک و خسته‌کننده نباشید. کاراکترهای صوتی این پلتفرم با لحن گرم، مکث‌های تنفسی و آهنگ طبیعی کلام صحبت می‌کنند تا در هر مکالمه دقیقاً حس هم‌صحبتی با یک فرد انگلیسی‌زبان را تجربه کنید.
          </p>

          <div className="space-y-3.5">
            <div className="flex items-center gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold">✓</div>
              <span>۱۱ شخصیت صوتی متنوع با دو لهجه معتبر آمریکایی و بریتانیایی</span>
            </div>
            <div className="flex items-center gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold">✓</div>
              <span>شنیدن آهنگ واقعی جملات برای تقویت همزمان مهارت شنیداری (Listening)</span>
            </div>
            <div className="flex items-center gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold">✓</div>
              <span>امکان انتخاب پارتنر صوتی دلخواه در هر مرحله از یادگیری</span>
            </div>
          </div>
        </div>

        {/* Right Voice Cards Showcase */}
        <div className="lg:w-1/2 w-full grid grid-cols-1 sm:grid-cols-2 gap-3.5">
          {voices.map((v, i) => (
            <div
              key={i}
              className="p-4 rounded-xl bg-[#111726]/80 border border-slate-800 hover:border-cyan-500/40 hover:bg-[#151D30] transition-all group"
            >
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center">
                    <Mic2 className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="font-semibold text-white text-sm group-hover:text-cyan-300 transition-colors flex items-center gap-1.5">
                      <span>{v.name}</span>
                      <Volume2 className="w-3 h-3 text-slate-500 group-hover:text-cyan-400" />
                    </h4>
                    <span className="text-[10px] text-slate-400">{v.lang}</span>
                  </div>
                </div>
                <span className="text-[10px] font-medium px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                  {v.badge}
                </span>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed font-normal">{v.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
