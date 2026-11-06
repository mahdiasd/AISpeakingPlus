import React from 'react';
import { motion } from 'motion/react';
import { KeyRound, ArrowLeft, ShieldAlert } from 'lucide-react';

interface GuestBannerCardProps {
  onLoginClick: () => void;
}

export const GuestBannerCard: React.FC<GuestBannerCardProps> = ({ onLoginClick }) => {
  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-blue-900/30 via-apple-surface to-apple-surface border border-apple-blue/30 p-4 shadow-apple-card backdrop-blur-xl">
      <div className="flex items-start gap-3.5">
        <div className="w-10 h-10 rounded-xl bg-apple-blue/20 border border-apple-blue/40 flex items-center justify-center text-apple-blue flex-shrink-0 mt-0.5">
          <KeyRound className="w-5 h-5" />
        </div>

        <div className="flex-1 min-w-0 space-y-1.5">
          <div className="flex items-center gap-1.5">
            <h4 className="text-sm font-bold text-white">ذخیره دائمی پیشرفت و مراحل</h4>
            <ShieldAlert className="w-3.5 h-3.5 text-apple-yellow" />
          </div>
          <p className="text-xs text-apple-gray-300 leading-relaxed">
            در حال حاضر در حالت مهمان هستید. با ورود به حساب کاربری، ستاره‌ها و رکوردهای شما در سرور ابری همگام‌سازی می‌شوند.
          </p>

          <div className="pt-1.5">
            <motion.button
              type="button"
              onClick={onLoginClick}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.96 }}
              className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-apple-blue text-white text-xs font-semibold shadow-md active:bg-blue-600 transition-colors"
            >
              <span>ورود با شماره موبایل</span>
              <ArrowLeft className="w-3.5 h-3.5" />
            </motion.button>
          </div>
        </div>
      </div>
    </div>
  );
};
