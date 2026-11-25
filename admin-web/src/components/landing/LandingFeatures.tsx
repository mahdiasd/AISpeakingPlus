import React from 'react';
import {
  Map,
  Volume2,
  SpellCheck,
  BookOpen,
  Lightbulb,
  Target,
  Sparkles,
} from 'lucide-react';

const FEATURES = [
  {
    icon: Map,
    title: 'ماجراجویی داستانی در لندن',
    desc: 'به جای تمرین‌های خشک و پراکنده، تو نقش یک دانشجوی تازه‌وارد در لندن قرار می‌گیری؛ از کنترل مرزی فرودگاه و خرید سیم‌کارت تا کلاس درس و مصاحبهٔ کاری.',
    badge: 'مسیر داستانی',
  },
  {
    icon: Volume2,
    title: 'گفتگو با صداهای طبیعی آمریکایی و بریتانیایی',
    desc: 'با میکروفون حرف بزن یا بنویس تا شخصیت داستان با صدای طبیعی جوابت رو بده. هر جا هم متوجه نشدی، با یک لمس ترجمهٔ فارسی پیام رو ببین.',
    badge: 'صوت و ترجمه',
  },
  {
    icon: SpellCheck,
    title: 'اصلاح گرامر جمله‌ها زیر هر پیام',
    desc: 'بعد از ارسال هر پیام، اگه ایراد گرامری یا ساختاری داشته باشی، شکل درست جمله همراه با توضیح کوتاه فارسی همون‌جا بهت نشون داده می‌شه.',
    badge: 'آموزش گرامر',
  },
  {
    icon: BookOpen,
    title: 'دیکشنری وسط مکالمه و جعبه لایتنر',
    desc: 'روی هر کلمه یا عبارت تو پیام‌ها تپ کن تا معنیش رو در جمله، فونتیک و تلفظ صوتیش رو ببینی و با یک دکمه بفرستیش تو جعبه لایتنر ۵ خانه‌ای.',
    badge: 'مرور لغات',
  },
  {
    icon: Lightbulb,
    title: 'کمک‌دست هر وقت وسط حرف زدن گیر کردی',
    desc: 'اگه ندونستی چی جواب بدی، دکمهٔ راهنما یک جملهٔ آماده و متناسب با سطح خودت رو همراه با معنی فارسی پیشنهاد می‌ده تا مکالمه متوقف نشه.',
    badge: 'راهنمای هوشمند',
  },
  {
    icon: Target,
    title: 'آزمون تعیین سطح و امتیازگیری',
    desc: 'با آزمون رایگان ۲۰ سؤالی سطحت (از A1 تا C1) مشخص می‌شه تا گفتگوها اندازهٔ خودت باشن. پایان هر مرحله هم ستاره و امتیاز می‌گیری و تو جدول برترین‌ها رقابت می‌کنی.',
    badge: 'شخصی‌سازی سطح',
  },
];

export const LandingFeatures: React.FC = () => {
  return (
    <section
      id="features"
      className="scroll-mt-24 py-20 sm:py-24 max-w-7xl mx-auto px-5 sm:px-8 [content-visibility:auto] [contain-intrinsic-size:700px]"
    >
      {/* Section Header */}
      <div className="text-center max-w-3xl mx-auto mb-14 sm:mb-16">
        <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full apple-pill text-indigo-300 text-xs font-semibold mb-4">
          <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
          <span>امکانات واقعی برنامه</span>
        </div>
        <h2 className="text-2xl sm:text-4xl font-extrabold text-white mb-4">
          توی AISpeaking چه امکاناتی داری؟
        </h2>
        <p className="text-slate-400 text-sm sm:text-base leading-relaxed">
          از تمرین مکالمه تو موقعیت‌های واقعی تا معنی کردن کلمه‌ها و مرورشون با لایتنر، همه‌چیز برای این چیده شده که کم‌کم راه بیفتی و راحت‌تر انگلیسی حرف بزنی.
        </p>
      </div>

      {/* Feature Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 sm:gap-6">
        {FEATURES.map((feature) => {
          const Icon = feature.icon;
          return (
            <div
              key={feature.title}
              className="group p-6 sm:p-7 rounded-3xl apple-glass hover:border-indigo-500/40 transition-all duration-300 relative flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="w-11 h-11 rounded-2xl bg-indigo-500/15 border border-indigo-500/25 flex items-center justify-center text-indigo-300 group-hover:scale-105 group-hover:bg-indigo-500/25 transition-all">
                    <Icon className="w-5 h-5" />
                  </div>
                  <span className="text-[11px] font-medium px-2.5 py-1 rounded-full bg-white/5 text-slate-300 border border-white/10">
                    {feature.badge}
                  </span>
                </div>
                <h3 className="text-base sm:text-lg font-bold text-white mb-2.5 group-hover:text-cyan-300 transition-colors">
                  {feature.title}
                </h3>
                <p className="text-xs sm:text-sm text-slate-400 leading-relaxed font-normal">
                  {feature.desc}
                </p>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
};
