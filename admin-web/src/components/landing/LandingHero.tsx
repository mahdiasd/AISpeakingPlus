import React from 'react';
import { Play, ChevronDown, Sparkles } from 'lucide-react';

interface LandingHeroProps {
  onLaunchApp: () => void;
}

export const LandingHero: React.FC<LandingHeroProps> = ({ onLaunchApp }) => {
  const [isMobile, setIsMobile] = React.useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      return window.innerWidth < 768;
    }
    return false;
  });

  React.useEffect(() => {
    const checkIsMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };
    window.addEventListener('resize', checkIsMobile);
    return () => window.removeEventListener('resize', checkIsMobile);
  }, []);

  return (
    <section className="relative w-full h-screen overflow-hidden flex flex-col items-center justify-center text-center px-4 select-none">
      {/* Background Video (Responsive: Desktop & Mobile) */}
      <video
        key={isMobile ? 'mobile' : 'desktop'}
        autoPlay
        loop
        muted
        playsInline
        preload="auto"
        className="absolute inset-0 w-full h-full object-cover pointer-events-none z-0"
        src={isMobile ? '/videos/hero-mobile.mp4' : '/videos/hero-desktop.mp4'}
      />

      {/* Dark Gradient Overlay */}
      <div className="absolute inset-0 bg-gradient-to-b from-black/40 via-black/60 to-[#0B0F17] z-10 pointer-events-none" />

      {/* Radial vignette for depth */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,transparent_0%,rgba(11,15,23,0.7)_100%)] z-10 pointer-events-none" />

      {/* Hero Content */}
      <div className="relative z-20 max-w-4xl mx-auto flex flex-col items-center justify-center pt-16">
        {/* Badge */}
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 backdrop-blur-md border border-white/20 text-cyan-300 text-xs sm:text-sm font-medium mb-6 animate-fade-in-up shadow-sm">
          <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
          <span>تمرین تعاملی مکالمه انگلیسی با پارتنر هوش مصنوعی</span>
        </div>

        {/* Main Headline */}
        <h1 className="text-[34px] sm:text-[56px] font-bold leading-[1.2] text-white tracking-tight mb-5 animate-fade-in-up [animation-delay:150ms]">
          مکالمه انگلیسی با هوش مصنوعی
          <span className="block mt-2 font-extrabold bg-gradient-to-r from-white via-slate-100 to-cyan-300 bg-clip-text text-transparent">
            بدون استرس، روان و طبیعی صحبت کنید
          </span>
        </h1>

        {/* Secondary Text */}
        <p className="text-[14px] sm:text-[17px] text-white/85 max-w-2xl mx-auto leading-relaxed mb-8 font-normal animate-fade-in-up [animation-delay:300ms]">
          در موقعیت‌های واقعی سفر، رزرو هتل، سفارش کافه و مصاحبه کاری با کاراکترهای صوتی زنده به زبان انگلیسی گفتگو کنید و در همان لحظه بازخورد تلفظ و اصلاح گرامر دریافت نمایید.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5 animate-fade-in-up [animation-delay:450ms] w-full sm:w-auto px-4 sm:px-0">
          {/* Primary CTA Button */}
          <button
            onClick={onLaunchApp}
            className="w-full sm:w-auto px-7 py-3.5 bg-white hover:bg-slate-100 text-black text-[15px] font-bold rounded-full shadow-lg shadow-black/40 hover:scale-105 hover:shadow-2xl hover:shadow-white/20 transition-all duration-300 flex items-center justify-center gap-2.5 cursor-pointer active:scale-95"
          >
            <Play className="w-4 h-4 fill-black text-black" />
            <span>شروع رایگان (مرحله اول و دوم)</span>
          </button>

          {/* Secondary Button */}
          <a
            href="#scenarios"
            className="w-full sm:w-auto px-6 py-3.5 bg-white/10 hover:bg-white/15 text-white text-[15px] font-medium rounded-full border border-white/20 backdrop-blur-sm transition-all duration-300 cursor-pointer flex items-center justify-center"
          >
            مشاهده سناریوهای مکالمه
          </a>
        </div>
      </div>

      {/* Bottom Scroll Indicator */}
      <a
        href="#features"
        className="absolute bottom-6 z-20 flex flex-col items-center gap-1.5 text-white/60 hover:text-white transition-colors duration-200 cursor-pointer"
      >
        <span className="text-[11px] font-normal tracking-wide">آشنایی با امکانات و ویژگی‌ها</span>
        <ChevronDown className="w-4 h-4 animate-bounce" />
      </a>
    </section>
  );
};
