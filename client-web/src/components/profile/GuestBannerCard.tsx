import React from 'react';
import { motion } from 'motion/react';
import { KeyRound, ArrowLeft } from 'lucide-react';

interface GuestBannerCardProps {
  onLoginClick: () => void;
}

export const GuestBannerCard: React.FC<GuestBannerCardProps> = ({ onLoginClick }) => {
  return (
    <div className="relative overflow-hidden rounded-[24px] bg-gradient-to-br from-[#142033]/90 via-[#0e1626]/85 to-[#0b101c]/90 border border-apple-blue/35 p-5 shadow-apple-card backdrop-blur-xl">
      {/* Specular Ambient Glow */}
      <div className="absolute top-0 right-0 w-40 h-40 bg-apple-blue/15 rounded-full blur-3xl pointer-events-none" />

      <div className="relative z-10 space-y-3.5">
        {/* Title row with Key icon */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-apple-blue/20 border border-apple-blue/40 flex items-center justify-center text-apple-blue shrink-0">
            <KeyRound className="w-5 h-5 drop-shadow-[0_0_8px_rgba(0,122,255,0.5)]" />
          </div>

          <h4 className="text-sm font-bold text-white tracking-tight">
            ذخیره همیشگی پیشرفت و مراحل
          </h4>
        </div>

        {/* Description */}
        <p className="text-xs text-apple-gray-300 leading-relaxed font-sans">
          اکنون در حالت مهمان هستید. با ثبت‌نام رایگان شماره موبایل، پیشرفت، مراحل و امتیازات شما در فضای ابری ذخیره شده و هیچ‌گاه از دست نمی‌رود.
        </p>

        {/* CTA Button */}
        <div className="pt-1 flex justify-start">
          <motion.button
            type="button"
            onClick={onLoginClick}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.96 }}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-apple-blue hover:bg-blue-600 text-white text-xs font-bold shadow-[0_4px_16px_rgba(0,122,255,0.35)] transition-all"
          >
            <span>ورود یا ثبت‌نام رایگان</span>
            <ArrowLeft className="w-3.5 h-3.5" />
          </motion.button>
        </div>
      </div>
    </div>
  );
};
