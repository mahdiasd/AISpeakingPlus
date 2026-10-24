import React from 'react';
import { Play, ChevronDown, Sparkles } from 'lucide-react';

interface LandingHeroProps {
  onLaunchApp: () => void;
}

export const LandingHero: React.FC<LandingHeroProps> = ({ onLaunchApp }) => {
  return (
    <section className="relative w-full h-screen overflow-hidden flex flex-col items-center justify-center text-center px-4 select-none">
      {/* Background Video */}
      <video
        autoPlay
        loop
        muted
        playsInline
        className="absolute inset-0 w-full h-full object-cover pointer-events-none z-0"
        src="https://cdn.sceneai.art/Hero%20Section%20Video/01d1f8de-fec0-4bf5-8b48-9fc2dbc8c6b0.mp4"
      />

      {/* Dark Gradient Overlay */}
      <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-black/55 to-[#0B0F17] z-10 pointer-events-none" />

      {/* Radial vignette for cinematic depth */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,transparent_0%,rgba(11,15,23,0.6)_100%)] z-10 pointer-events-none" />

      {/* Hero Content (Center) */}
      <div className="relative z-20 max-w-4xl mx-auto flex flex-col items-center justify-center pt-16">
        {/* Animated Badge */}
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 backdrop-blur-md border border-white/20 text-cyan-300 text-xs sm:text-sm font-medium mb-6 animate-fade-in-up shadow-sm">
          <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
          <span>نسل نوین آموزش مکالمه انگلیسی با هوش مصنوعی و صدای طبیعی</span>
        </div>

        {/* Main Headline */}
        <h1 className="text-[38px] sm:text-[56px] font-medium leading-[1.1] text-white tracking-tight mb-5 animate-fade-in-up [animation-delay:150ms]">
          مکالمه انگلیسی با هوش مصنوعی
          <span className="block mt-2.5 font-bold bg-gradient-to-r from-white via-slate-100 to-cyan-300 bg-clip-text text-transparent">
            بدون استرس، روان و طبیعی صحبت کن
          </span>
        </h1>

        {/* Secondary Text */}
        <p className="text-[15px] sm:text-[18px] text-white/80 max-w-2xl mx-auto leading-relaxed mb-9 font-normal animate-fade-in-up [animation-delay:300ms]">
          با ۱۱ کاراکتر صوتی فوق‌طبیعی Kokoro TTS در موقعیت‌های واقعی سفر، هتل، کافه و مصاحبه کاری به زبان انگلیسی گفتگو کن و در لحظه فیدبک تلفظ و اصلاح گرامر بگیر.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5 animate-fade-in-up [animation-delay:450ms]">
          {/* Primary CTA Button */}
          <button
            onClick={onLaunchApp}
            className="px-[26px] py-[12px] bg-white text-black text-[15px] font-medium rounded-full shadow-lg shadow-black/40 hover:scale-105 hover:shadow-2xl hover:shadow-white/25 transition-all duration-300 flex items-center gap-2.5 cursor-pointer active:scale-95"
          >
            <Play className="w-4 h-4 fill-black text-black" />
            <span>ورود به اپلیکیشن و شروع رایگان</span>
          </button>

          {/* Secondary Button */}
          <a
            href="#scenarios"
            className="px-[24px] py-[12px] bg-white/10 hover:bg-white/15 text-white/90 hover:text-white text-[15px] font-normal rounded-full border border-white/20 backdrop-blur-sm transition-all duration-300 cursor-pointer"
          >
            مشاهده سناریوهای داستانی
          </a>
        </div>
      </div>

      {/* Bottom Scroll Indicator */}
      <a
        href="#features"
        className="absolute bottom-6 z-20 flex flex-col items-center gap-1.5 text-white/60 hover:text-white transition-colors duration-200 cursor-pointer"
      >
        <span className="text-[11px] font-normal tracking-wide">مشاهده امکانات و ویژگی‌ها</span>
        <ChevronDown className="w-4 h-4 animate-bounce" />
      </a>
    </section>
  );
};
