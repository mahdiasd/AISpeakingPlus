import React from 'react';
import { motion } from 'motion/react';
import { Headphones, ChevronLeft } from 'lucide-react';

interface SupportCardProps {
  onClick: () => void;
}

export const SupportCard: React.FC<SupportCardProps> = ({ onClick }) => {
  return (
    <motion.button
      type="button"
      onClick={onClick}
      whileHover={{ scale: 1.01 }}
      whileTap={{ scale: 0.98 }}
      className="w-full relative overflow-hidden rounded-[24px] apple-glass-card p-4 shadow-apple-card text-right flex items-center justify-between gap-3.5 transition-all focus:outline-none group"
    >
      <div className="flex items-center gap-3.5">
        <div className="w-11 h-11 rounded-2xl bg-apple-blue/15 border border-apple-blue/30 flex items-center justify-center text-apple-blue group-hover:scale-105 transition-transform shrink-0">
          <Headphones className="w-5 h-5 drop-shadow-[0_0_8px_rgba(0,122,255,0.4)]" />
        </div>

        <div className="space-y-0.5">
          <h4 className="text-sm font-bold text-white group-hover:text-apple-blue transition-colors">
            پشتیبانی و ارتباط با ما
          </h4>
          <p className="text-xs text-apple-gray-300 font-sans">
            ارتباط سریع در تلگرام و روبیکا
          </p>
        </div>
      </div>

      <ChevronLeft className="w-5 h-5 text-apple-gray-400 group-hover:-translate-x-1 group-hover:text-white transition-all shrink-0" />
    </motion.button>
  );
};
