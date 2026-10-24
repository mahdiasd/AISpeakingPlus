import React from 'react';
import { Headphones, ShieldCheck, CheckCircle2, MessageSquare, Award, Sparkles } from 'lucide-react';

export const LandingFeatures: React.FC = () => {
  const features = [
    {
      icon: Headphones,
      title: 'صدای فوق‌طبیعی انسان با Kokoro TTS',
      desc: 'با ۱۱ کاراکتر صوتی باکیفیت زن و مرد به لهجه‌های American و British صحبت کنید. لحن صمیمانه، مکث‌های طبیعی و حسی کاملاً زنده.',
      badge: 'کیفیت استودیویی',
    },
    {
      icon: MessageSquare,
      title: 'ماموریت‌ها و سناریوهای داستانی',
      desc: 'به جای حفظ طوطی‌وار لغات، در نقش مسافر فرودگاه، مهمان هتل یا کارجوی مصاحبه کاری قرار بگیرید و هدفمند گفتگو کنید.',
      badge: 'مکالمه کاربردی',
    },
    {
      icon: Award,
      title: 'ارزیابی تلفظ و فیدبک هوشمند',
      desc: 'موتور ارزیابی دقیق گفتار، تلفظ کلمات و ساختار گرامری شما را بلافاصله آنالیز کرده و پیشنهادات نیتیو برای بهبود ارائه می‌دهد.',
      badge: 'تحلیل بلادرنگ',
    },
    {
      icon: ShieldCheck,
      title: 'بدون استرس و قضاوت دیگران',
      desc: 'هر چقدر که می‌خواهید مکث کنید، تکرار کنید و اشتباه کنید. پارتنر هوش مصنوعی با صبوری کامل در کنار شماست تا اعتمادبه‌نفس شما بالا رود.',
      badge: 'محیط ۱۰۰٪ امن',
    },
    {
      icon: Sparkles,
      title: 'پیشنهاد جملات نیتیو و کمکی',
      desc: 'اگر وسط مکالمه ندانستید چه بگویید، سیستم گزینه‌ها و اصطلاحات متداول انگلیسی را همراه با معنی به شما پیشنهاد می‌دهد.',
      badge: 'راهنمای هوشمند',
    },
    {
      icon: CheckCircle2,
      title: 'نسخه وب مدرن و بدون نیاز به نصب',
      desc: 'تنها با یک کلیک روی مرورگر کروم یا سافاری در موبایل، تبلت یا لپ‌تاپ اجرا می‌شود و تمام تاریخچه پیشرفت شما را حفظ می‌کند.',
      badge: 'دسترسی همه‌جا',
    },
  ];

  return (
    <section id="features" className="py-24 max-w-7xl mx-auto px-5 sm:px-8">
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
          ترکیب هوش مصنوعی گفتاری پیشرفته، سناریوهای تعاملی و ارزیابی در لحظه به شما کمک می‌کند سد صحبت کردن به زبان انگلیسی را برای همیشه بشکنید.
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
