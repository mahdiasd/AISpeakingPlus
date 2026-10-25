import React, { useState } from 'react';
import { ChevronDown, HelpCircle } from 'lucide-react';

export const LandingFaq: React.FC = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  const faqs = [
    {
      q: 'آیا برای استفاده نیاز به نصب اپلیکیشن خاصی دارم؟',
      a: 'خیر! AISpeakingPlus با فناوری مدرن وب و Compose Multiplatform طراحی شده است. شما می‌توانید مستقیماً از طریق مرورگر موبایل (کروم، سافاری) یا کامپیوتر وارد شوید و بدون هیچ دانلود حجیمی مکالمه را آغاز کنید.',
    },
    {
      q: 'من سطح زبانم مبتدی است؛ آیا این برنامه برای من مناسب است؟',
      a: 'کاملاً بله! سناریوها از سطح مبتدی تا پیشرفته طراحی شده‌اند. علاوه بر این، در حین مکالمه گزینه‌های پیشنهادی به همراه ترجمه فارسی در اختیارتان قرار می‌گیرد تا هرگز احساس درماندگی نکنید.',
    },
    {
      q: 'صدای هوش مصنوعی چگونه این‌قدر طبیعی است؟',
      a: 'ما از پیشرفته‌ترین مدل سبک‌وزن تبدیل متن به گفتار (Kokoro TTS) به همراه ۱۱ صدای ضبط‌شده واقعی استفاده می‌کنیم که لهجه‌ها، آهنگ صدا و مکث‌های طبیعی زبان انگلیسی را با بالاترین کیفیت بازتولید می‌کند.',
    },
    {
      q: 'ارزیابی تلفظ و فیدبک گرامر چگونه انجام می‌شود؟',
      a: 'پس از هر بار که شما صحبت می‌کنید، صدای شما توسط موتور Speech-to-Text تحلیل شده و هوش مصنوعی خطاهای گرامری، اشکالات تلفظی و شیوه بهتر بیان را به شما پیشنهاد می‌دهد.',
    },
    {
      q: 'چطور می‌توانم شروع کنم؟',
      a: 'کافی است روی دکمه "ورود به اپلیکیشن" در بالای همین صفحه کلیک کنید و بلافاصله تمرین مکالمه را شروع نمایید.',
    },
  ];

  return (
    <section id="faq" className="py-20 max-w-4xl mx-auto px-5 sm:px-8">
      <div className="text-center mb-14">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold mb-3">
          <HelpCircle className="w-3.5 h-3.5" />
          <span>پاسخ به پرسش‌های شما</span>
        </div>
        <h2 className="text-2xl sm:text-4xl font-bold text-white mb-3">
          پرسش‌های متداول
        </h2>
        <p className="text-slate-400 text-sm">
          پاسخ به سوالاتی که ممکن است قبل از شروع یادگیری در ذهن داشته باشید.
        </p>
      </div>

      <div className="space-y-3">
        {faqs.map((faq, idx) => {
          const isOpen = openIndex === idx;
          return (
            <div
              key={idx}
              className="rounded-xl bg-[#111726]/80 border border-slate-800 overflow-hidden transition-all"
            >
              <button
                onClick={() => setOpenIndex(isOpen ? null : idx)}
                className="w-full px-5 py-4 flex items-center justify-between text-right gap-4 cursor-pointer hover:bg-slate-800/40 transition-colors"
              >
                <span className="text-sm sm:text-base font-medium text-white">{faq.q}</span>
                <ChevronDown
                  className={`w-4 h-4 text-slate-400 shrink-0 transition-transform duration-300 ${
                    isOpen ? 'rotate-180 text-cyan-400' : ''
                  }`}
                />
              </button>
              {isOpen && (
                <div className="px-5 pb-4 text-xs sm:text-sm text-slate-400 leading-relaxed border-t border-slate-800/60 pt-3">
                  {faq.a}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
};
