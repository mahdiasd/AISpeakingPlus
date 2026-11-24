import React, { useState, useEffect } from 'react';
import { Play, Sparkles } from 'lucide-react';

interface LandingHeroProps {
  onLaunchApp: () => void;
}

const MOBILE_BREAKPOINT = '(max-width: 767px)';

export const LandingHero: React.FC<LandingHeroProps> = ({ onLaunchApp }) => {
  const [isMobile, setIsMobile] = useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia(MOBILE_BREAKPOINT).matches;
    }
    return false;
  });

  useEffect(() => {
    const mediaQuery = window.matchMedia(MOBILE_BREAKPOINT);
    const handleChange = (event: MediaQueryListEvent) => {
      setIsMobile(event.matches);
    };
    setIsMobile(mediaQuery.matches);
    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  return (
    <section className="relative w-full h-screen min-h-[600px] overflow-hidden flex flex-col items-center justify-center text-center px-4 select-none">
      {/* Background Video (Responsive: Desktop & Mobile) */}
      <video
        key={isMobile ? 'mobile' : 'desktop'}
        autoPlay
        loop
        muted
        playsInline
        preload="metadata"
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
          <span>تمرین مکالمه انگلیسی در دل یک داستان واقعی</span>
        </div>

        {/* Main Headline */}
        <h1 className="text-[30px] sm:text-[56px] font-black leading-[1.25] text-white tracking-tight mb-4 sm:mb-5">
          انگلیسی رو با حرف زدن یاد بگیر
          <span className="block mt-1.5 sm:mt-2 font-extrabold bg-gradient-to-r from-white via-slate-100 to-cyan-300 bg-clip-text text-transparent">
            بدون استرس، مثل یه گفتگوی واقعی
          </span>
        </h1>

        {/* Secondary Paragraph (Desktop) */}
        <p className="hidden sm:block text-[15px] sm:text-[17px] text-white/85 max-w-2xl mx-auto leading-relaxed mb-8 font-normal">
          تو ۳۱ مرحلهٔ داستانی با شخصیت‌های هوش مصنوعی انگلیسی حرف بزن. هر جا جمله‌ات ایراد داشت شکل درستش رو ببین، کلمه‌های جدید رو همون لحظه معنی کن و بفرست تو جعبه لایتنرت.
        </p>

        {/* Action Button */}
        <div className="mt-3 sm:mt-0 w-full sm:w-auto flex items-center justify-center">
          <button
            onClick={onLaunchApp}
            className="w-full sm:w-auto px-8 sm:px-9 py-3.5 sm:py-4 apple-btn-primary font-extrabold text-[15px] sm:text-[16px] rounded-full flex items-center justify-center gap-2.5 cursor-pointer shadow-2xl"
          >
            <Play className="w-4 h-4 fill-black text-black" />
            <span>شروع رایگان (مرحله ۱ و ۲)</span>
          </button>
        </div>
      </div>
    </section>
  );
};
