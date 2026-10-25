import React from 'react';
import { Volume2, Sparkles, Mic2 } from 'lucide-react';

export const LandingVoiceLab: React.FC = () => {
  const voices = [
    { name: 'Sarah', lang: 'English (US)', gender: 'خانم', desc: 'لحن صمیمی، ریتم روان و مناسب سناریوهای روزمره و دوستانه', badge: 'پرطرفدار' },
    { name: 'Nicole', lang: 'English (US)', gender: 'خانم', desc: 'صدای رسا، تلفظ شفاف واج‌ها و مناسب نوآموزان', badge: 'تلفظ واضح' },
    { name: 'Michael', lang: 'English (US)', gender: 'آقا', desc: 'لحن طبیعی، محاوره‌ای و شبیه‌ساز مکالمات واقعی کافه و کار', badge: 'آمریکایی نیتیو' },
    { name: 'Adam', lang: 'English (US)', gender: 'آقا', desc: 'صدای بم، جدی و ایده‌آل برای مصاحبه‌های استخدامی و جلسات رسمی', badge: 'رسمی و بیزینس' },
    { name: 'George', lang: 'English (UK)', gender: 'آقا', desc: 'لهجه اصیل بریتانیایی (RP)، بسیار تمیز و متین', badge: 'بریتیش کلاسیک' },
    { name: 'Bella', lang: 'English (US)', gender: 'خانم', desc: 'لحن شاد و پرانرژی برای تقویت اشتیاق یادگیری', badge: 'مکالمه پویا' },
  ];

  return (
    <section id="voice" className="py-24 max-w-7xl mx-auto px-5 sm:px-8">
      <div className="flex flex-col lg:flex-row items-center justify-between gap-12">
        {/* Left Information */}
        <div className="lg:w-1/2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-semibold mb-4">
            <Sparkles className="w-3.5 h-3.5" />
            <span>موتور صوتی نسل جدید Kokoro TTS</span>
          </div>

          <h2 className="text-2xl sm:text-4xl font-bold text-white mb-5 leading-tight">
            با صدایی صحبت کن که <span className="text-cyan-300">واقعاً شبیه انسان</span> است
          </h2>

          <p className="text-slate-400 text-sm sm:text-base leading-relaxed mb-6">
            دیگر خبری از صداهای بی‌روح، رباتیک و خسته‌کننده ماشین نیست. AISpeakingPlus از مدل سبک‌وزن و فوق‌طبیعی Kokoro استفاده می‌کند که فراز و فرود صدا، مکث‌های تنفسی و تاکیدهای احساسی را دقیقاً مانند یک پارتنر انگلیسی‌زبان اجرا می‌کند.
          </p>

          <div className="space-y-3.5">
            <div className="flex items-center gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold">✓</div>
              <span>۱۱ مدل صوتی متنوع با دو لهجه معتبر آمریکایی (US) و بریتانیایی (UK)</span>
            </div>
            <div className="flex items-center gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold">✓</div>
              <span>تولید بلادرنگ صدا در کسری از ثانیه روی سرورهای پردازشی اختصاصی</span>
            </div>
            <div className="flex items-center gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold">✓</div>
              <span>قابلیت تست و تغییر صداها در بخش اختصاصی Voice Lab</span>
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
                    <h4 className="font-inter font-semibold text-white text-sm group-hover:text-cyan-300 transition-colors flex items-center gap-1.5">
                      <span>{v.name}</span>
                      <Volume2 className="w-3 h-3 text-slate-500 group-hover:text-cyan-400" />
                    </h4>
                    <span className="text-[10px] text-slate-400 font-inter">{v.lang}</span>
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
