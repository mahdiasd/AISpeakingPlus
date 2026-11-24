import React, { useState } from 'react';
import { ChevronDown, HelpCircle } from 'lucide-react';

const FAQS = [
  {
    q: 'برای استفاده باید برنامه نصب کنم؟',
    a: 'نه، نیازی نیست. مستقیماً تو مرورگر گوشی (اندروید و آیفون)، تبلت یا کامپیوتر باز می‌شه و هیچ فضایی از گوشیت رو اشغال نمی‌کنه.',
  },
  {
    q: 'چقدر از برنامه رایگانه؟ قبل از خرید می‌تونم تستش کنم؟',
    a: 'آره! مرحله اول و دوم کاملاً رایگانه. می‌تونی همین الان وارد بشی، مکالمه صوتی، اصلاح گرامر، دیکشنری و جعبه لایتنر رو امتحان کنی و بعد اگه دوست داشتی اشتراک بگیری.',
  },
  {
    q: 'اگه سطح زبانم پایینه، به دردم می‌خوره؟',
    a: 'حتماً. تو برنامه یک آزمون تعیین سطح ۲۰ سؤالی داریم که سطحت رو از A1 تا C1 مشخص می‌کنه (یا خودت می‌تونی تغییرش بدی). شخصیت‌های داستان هم متناسب با سطح تو حرف می‌زنن، تازه هر جا گیر کنی راهنمای جمله و ترجمه فارسی کمکت می‌کنه.',
  },
  {
    q: 'جعبه لایتنر چطور کار می‌کنه؟',
    a: 'وسط مکالمه روی هر کلمه یا عبارتی که بلد نیستی تپ می‌کنی تا معنی، فونتیک و تلفظش رو ببینی و بفرستیش تو جعبه لایتنر. بعد با مرور زمان‌بندی‌شده تو ۵ جعبه (۱ تا ۳۰ روز) و یادآور روزانه، لغت‌ها تو ذهنت می‌مونن.',
  },
  {
    q: 'اصلاح گرامر چطور انجام می‌شه؟',
    a: 'بعد از هر پیامی که می‌فرستی، هوش مصنوعی جمله‌ات رو بررسی می‌کنه و اگه اشتباه گرامری یا ساختاری داشته باشه، شکل درستش رو همراه با توضیح سادهٔ فارسی زیر همون پیام نشون می‌ده.',
  },
  {
    q: 'چطور همین الان شروع کنم؟',
    a: 'کافیه روی دکمهٔ «شروع رایگان» بالای صفحه کلیک کنی تا وارد برنامه بشی و اولین مکالمه‌ات رو تو پرواز به لندن شروع کنی.',
  },
];

export const LandingFaq: React.FC = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  return (
    <section
      id="faq"
      className="scroll-mt-24 py-20 sm:py-24 max-w-4xl mx-auto px-5 sm:px-8 [content-visibility:auto] [contain-intrinsic-size:620px]"
    >
      <div className="text-center mb-14">
        <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full apple-pill text-indigo-300 text-xs font-semibold mb-3">
          <HelpCircle className="w-3.5 h-3.5 text-indigo-400" />
          <span>سؤال‌های پرتکرار</span>
        </div>
        <h2 className="text-2xl sm:text-4xl font-extrabold text-white mb-3">
          پرسش‌های متداول
        </h2>
        <p className="text-slate-400 text-sm">
          اگه قبل از شروع سؤالی تو ذهنت هست، احتمالاً جوابش همین‌جاست.
        </p>
      </div>

      <div className="space-y-3">
        {FAQS.map((faq, idx) => {
          const isOpen = openIndex === idx;
          return (
            <div key={faq.q} className="rounded-2xl apple-glass overflow-hidden transition-all">
              <button
                onClick={() => setOpenIndex(isOpen ? null : idx)}
                className="w-full px-5 py-4 flex items-center justify-between text-right gap-4 cursor-pointer hover:bg-white/[0.03] transition-colors"
              >
                <span className="text-sm sm:text-base font-semibold text-white">{faq.q}</span>
                <ChevronDown
                  className={`w-4 h-4 text-slate-400 shrink-0 transition-transform duration-300 ${
                    isOpen ? 'rotate-180 text-cyan-400' : ''
                  }`}
                />
              </button>
              {isOpen ? (
                <div className="px-5 pb-4 text-xs sm:text-sm text-slate-300 leading-relaxed border-t border-white/5 pt-3">
                  {faq.a}
                </div>
              ) : null}
            </div>
          );
        })}
      </div>
    </section>
  );
};
