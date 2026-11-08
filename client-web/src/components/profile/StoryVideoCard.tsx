import React from 'react';
import { motion } from 'motion/react';
import { Play, ChevronLeft } from 'lucide-react';

interface StoryVideoCardProps {
  onPlay: () => void;
}

export const StoryVideoCard: React.FC<StoryVideoCardProps> = ({ onPlay }) => {
  return (
    <motion.button
      type="button"
      onClick={onPlay}
      whileHover={{ scale: 1.01 }}
      whileTap={{ scale: 0.98 }}
      className="w-full relative overflow-hidden rounded-[24px] apple-glass-card p-4 shadow-apple-card text-right flex items-center justify-between gap-3.5 transition-all focus:outline-none group"
    >
      <div className="flex items-center gap-3.5">
        <div className="w-11 h-11 rounded-2xl bg-blue-500/15 border border-blue-500/30 flex items-center justify-center text-blue-400 group-hover:scale-105 transition-transform shrink-0">
          <Play className="w-5 h-5 fill-blue-500/40 text-blue-400 ml-0.5" />
        </div>

        <div className="space-y-0.5">
          <h4 className="text-sm font-bold text-white group-hover:text-blue-300 transition-colors">
            داستان بازی (ویدیوی افتتاحیه)
          </h4>
          <p className="text-xs text-apple-gray-300 font-sans">
            تماشای مجدد سفر آغازین قهرمان از تهران تا لندن
          </p>
        </div>
      </div>

      <ChevronLeft className="w-5 h-5 text-apple-gray-400 group-hover:-translate-x-1 group-hover:text-white transition-all shrink-0" />
    </motion.button>
  );
};
