import React from 'react';
import { Headphones, ShieldCheck, CheckCircle2, MessageSquare, Award, Sparkles } from 'lucide-react';

export const LandingFeatures: React.FC = () => {
  const features = [
    {
      icon: Headphones,
      title: 'صداهای طبیعی و زنده با لحن انسانی',
      desc: 'با ۱۱ شخصیت صوتی خانم و آقا در لهجه‌های آمریکایی و بریتانیایی گفتگو کنید. لحن گرم، مکث‌های تنفسی و تلفظ دقیق، حس یک مکالمه واقعی را خلق می‌کند.',
      badge: 'کیفیت استودیویی',
    },
    {
      icon: MessageSquare,
      title: 'سناریوها و ماموریت‌های کاربردی',
      desc: 'به جای حفظ طوطی‌وار گرامر یا لغات، در نقش مسافر، مهمان هتل یا کارجوی مصاحبه کاری قرار بگیرید و در فضایی هدفمند مکالمه کنید.',
      badge: 'مکالمه کاربردی',
    },
    {
      icon: Award,
      title: 'ارزیابی هوشمند تلفظ و تحلیل کلام',
      desc: 'سامانه با بررسی دقیق صدای شما، اشتباهات تلفظی و ساختار دستوری جملاتتان را تحلیل می‌کند و پیشنهادهای کاربردی برای صحبت بهتر ارائه می‌دهد.',
      badge: 'تحلیل در لحظه',
    },
    {
      icon: ShieldCheck,
      title: 'تمرین در فضایی امن و بدون قضاوت',
      desc: 'هر قدر که می‌خواهید مکث کنید، اشتباه کنید و دوباره بگویید. پارتنر هوش مصنوعی با صبوری در کنار شماست تا اعتمادبه‌نفس گفتاری‌تان رشد کند.',
      badge: 'محیط ۱۰۰٪ آرام',
    },
    {
      icon: Sparkles,
      title: 'پیشنهاد هوشمند جملات در بن‌بست مکالمه',
      desc: 'اگر وسط گفتگو متوجه نشدید چه پاسخی بدهید، سامانه عبارات متداول انگلیسی را همراه با معنی فارسی پیشنهاد می‌دهد تا مکالمه متوقف نشود.',
      badge: 'راهنمای همراه',
    },
    {
      icon: CheckCircle2,
      title: 'اجرای مستقیم در مرورگر بدون نیاز به نصب',
      desc: 'تنها با یک کلیک روی مرورگر گوشی، تبلت یا لپ‌تاپ وارد شوید؛ تمام سوابق یادگیری و پیشرفت شما در حسابتان محفوظ می‌ماند.',
      badge: 'دسترسی آسان',
    },
  ];

  return (
    <section id="features" className="scroll-mt-24 py-24 max-w-7xl mx-auto px-5 sm:px-8">
      {/* Section Header */}
      <div className="text-center max-w-3xl mx-auto mb-16">
        <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold mb-4">
          <Sparkles className="w-3.5 h-3.5" />
          <span>امکانات و مزیت‌های منحصربه‌فرد</span>
        </div>
        <h2 className="text-2xl sm:text-4xl font-bold text-white mb-4">
          چرا یادگیری مکالمه با AISpeakingPlus متفاوت است؟
        </h2>
        <p className="text-slate-400 text-sm sm:text-base leading-relaxed">
          ترکیب هوش مصنوعی تعاملی، صداهای طبیعی انسانی و بازخورد بلادرنگ به شما کمک می‌کند سد صحبت کردن به زبان انگلیسی را برای همیشه بشکنید.
        </p>
      </div>

      {/* Feature Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {features.map((feature, idx) => {
          const Icon = feature.icon;
          return (
            <div
              key={idx}
              className="group p-6 rounded-2xl bg-[#111726]/60 border border-slate-800 hover:border-indigo-500/40 hover:bg-[#151D30]/80 transition-all duration-300 relative flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="w-11 h-11 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 group-hover:scale-110 group-hover:bg-indigo-500/20 transition-all">
                    <Icon className="w-5 h-5" />
                  </div>
                  <span className="text-[11px] font-medium px-2.5 py-1 rounded-full bg-slate-800/80 text-slate-300 border border-slate-700/60">
                    {feature.badge}
                  </span>
                </div>
                <h3 className="text-base sm:text-lg font-semibold text-white mb-2.5 group-hover:text-cyan-300 transition-colors">
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
