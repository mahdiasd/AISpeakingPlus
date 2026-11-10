import React, { useState, useEffect } from 'react';
import { Play, ChevronDown, Sparkles } from 'lucide-react';

interface LandingHeroProps {
  onLaunchApp: () => void;
}

export const LandingHero: React.FC<LandingHeroProps> = ({ onLaunchApp }) => {
  const [isMobile, setIsMobile] = useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      return window.innerWidth < 768;
    }
    return false;
  });

  useEffect(() => {
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

      {/* Glass & Dark Gradient Overlays for high readability without masking the video */}
      <div className="absolute inset-0 bg-gradient-to-b from-black/50 via-black/40 to-[#0B0F17] z-10 pointer-events-none" />
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,transparent_0%,rgba(11,15,23,0.7)_100%)] z-10 pointer-events-none" />

      {/* Hero Content */}
      <div className="relative z-20 max-w-4xl mx-auto flex flex-col items-center justify-center pt-10 sm:pt-14 px-2">
        {/* Apple Pill Badge (Desktop only to prevent clutter on mobile) */}
        <div className="hidden sm:inline-flex items-center gap-2 px-4 py-1.5 rounded-full apple-pill text-cyan-300 text-xs sm:text-sm font-medium mb-6 shadow-sm">
          <Sparkles className="w-3.5 h-3.5 text-cyan-300" />
          <span>تمرین تعاملی مکالمه انگلیسی با پارتنر هوش مصنوعی</span>
        </div>

        {/* Main Headline - Crisp, compact on mobile, majestic on desktop */}
        <h1 className="text-[30px] sm:text-[56px] font-black leading-[1.25] text-white tracking-tight mb-4 sm:mb-5">
          مکالمه انگلیسی با هوش مصنوعی
          <span className="block mt-1.5 sm:mt-2 font-extrabold bg-gradient-to-r from-white via-slate-100 to-cyan-300 bg-clip-text text-transparent">
            بدون استرس، روان و طبیعی صحبت کنید
          </span>
        </h1>

        {/* Secondary Paragraph - Hidden on mobile to keep video clean and decluttered */}
        <p className="hidden sm:block text-[15px] sm:text-[17px] text-white/85 max-w-2xl mx-auto leading-relaxed mb-8 font-normal">
          در موقعیت‌های واقعی با کاراکترهای صوتی زنده به زبان انگلیسی گفتگو کنید و در همان لحظه بازخورد تلفظ و اصلاح گرامر دریافت نمایید.
        </p>

        {/* Action Button - Single, prominent Apple Glass CTA */}
        <div className="mt-3 sm:mt-0 w-full sm:w-auto flex items-center justify-center">
          <button
            onClick={onLaunchApp}
            className="w-full sm:w-auto px-8 sm:px-9 py-3.5 sm:py-4 apple-btn-primary font-extrabold text-[15px] sm:text-[16px] rounded-full flex items-center justify-center gap-2.5 cursor-pointer shadow-2xl"
          >
            <Play className="w-4 h-4 fill-black text-black" />
            <span>شروع رایگان (مرحله اول و دوم)</span>
          </button>
        </div>
      </div>

      {/* Subtle Bottom Scroll Indicator */}
      <a
        href="#features"
        className="absolute bottom-5 sm:bottom-6 z-20 flex flex-col items-center gap-1 text-white/60 hover:text-white transition-colors duration-200 cursor-pointer"
        aria-label="مشاهده امکانات"
      >
        <span className="text-[11px] font-normal tracking-wide hidden sm:block">آشنایی با امکانات و ویژگی‌ها</span>
        <ChevronDown className="w-4 h-4 animate-bounce" />
      </a>
    </section>
  );
};
