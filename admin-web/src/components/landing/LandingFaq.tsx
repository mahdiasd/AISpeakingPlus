import React, { useState } from 'react';
import { ChevronDown, HelpCircle } from 'lucide-react';

export const LandingFaq: React.FC = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  const faqs = [
    {
      q: 'آیا برای استفاده نیاز به نصب برنامه یا اپلیکیشن خاصی دارم؟',
      a: 'خیر. این سامانه تحت وب طراحی شده است و بدون نیاز به دانلود یا اشغال حافظه، مستقیماً از طریق مرورگر انواع گوشی (اندروید و آیفون)، تبلت و کامپیوتر اجرا می‌شود.',
    },
    {
      q: 'آیا استفاده از برنامه رایگان است؟',
      a: 'بله! مرحله اول و دوم در تمامی سناریوها کاملاً رایگان است. شما می‌توانید بدون پرداخت هیچ هزینه‌ای و بدون نیاز به ثبت اطلاعات کارت بانکی، کیفیت صداها، دقت مکالمه و بازخورد هوشمند را ارزیابی فرمایید.',
    },
    {
      q: 'سطح زبان من مقدماتی است؛ آیا این سامانه برای من مناسب است؟',
      a: 'کاملاً. سناریوها برای سطوح مختلف از مقدماتی تا پیشرفته دسته‌بندی شده‌اند. علاوه بر این، در طول مکالمه گزینه‌های پیشنهادی آماده همراه با ترجمه فارسی در اختیارتان قرار می‌گیرد تا بدون نگرانی گفتگو را ادامه دهید.',
    },
    {
      q: 'صداها چگونه تا این اندازه طبیعی صحبت می‌کنند؟',
      a: 'کاراکترهای صوتی بر پایه آهنگ گفتار گویندگان اصیل زبان انگلیسی طراحی شده‌اند تا لحن، فراز و فرود صدا و مکث‌های طبیعی زبان به گوش شما برسد و مهارت شنیداری شما نیز همزمان تقویت شود.',
    },
    {
      q: 'بازخورد گرامر و ارزیابی تلفظ چگونه به من کمک می‌کند؟',
      a: 'پس از هر بار صحبت کردن، جملات شما تحلیل می‌شوند و اگر خطایی در ساختار جمله یا اصطلاحات وجود داشته باشد، شکل طبیعی‌تر و صحیح آن همراه با نکته آموزشی به شما نمایش داده می‌شود.',
    },
    {
      q: 'چگونه می‌توانم مکالمه را شروع کنم؟',
      a: 'کافی است روی دکمه «شروع رایگان» در بالای همین صفحه کلیک کنید تا بلافاصله وارد برنامه شوید و تمرین دو مرحله اول را آغاز نمایید.',
    },
  ];

  return (
    <section id="faq" className="scroll-mt-24 py-20 max-w-4xl mx-auto px-5 sm:px-8">
      <div className="text-center mb-14">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold mb-3">
          <HelpCircle className="w-3.5 h-3.5" />
          <span>پاسخ به پرسش‌های شما</span>
        </div>
        <h2 className="text-2xl sm:text-4xl font-bold text-white mb-3">
          پرسش‌های متداول
        </h2>
        <p className="text-slate-400 text-sm">
          پاسخ به سوالاتی که پیش از شروع تمرین مکالمه برای کاربران مطرح می‌شود.
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
