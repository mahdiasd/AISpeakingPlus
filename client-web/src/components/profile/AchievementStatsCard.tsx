import React from 'react';
import { motion } from 'motion/react';
import { Star, Gem, AudioWaveform, Gamepad2, TrendingUp, ChevronLeft } from 'lucide-react';
import { getCefrInfo } from '../../utils/cefr';

interface AchievementStatsCardProps {
  totalStars: number;
  score: number;
  languageLevel?: string;
  completedStages: number;
  totalStages: number;
  onLevelClick: () => void;
}

export const AchievementStatsCard: React.FC<AchievementStatsCardProps> = ({
  totalStars,
  score,
  languageLevel,
  completedStages,
  totalStages,
  onLevelClick,
}) => {
  const cefr = getCefrInfo(languageLevel);

  return (
    <div className="space-y-3">
      {/* Section Header */}
      <div className="flex items-center gap-2 px-1">
        <TrendingUp className="w-4 h-4 text-apple-blue" />
        <h3 className="text-sm font-bold text-white">آمار و پیشرفت یادگیری</h3>
      </div>

      {/* 2x2 Grid of Apple Glass Squircles */}
      <div className="grid grid-cols-2 gap-3">
        {/* 1. Stars */}
        <div className="relative overflow-hidden rounded-[20px] apple-glass p-4 flex flex-col justify-between h-[105px] shadow-sm">
          <div className="flex items-center justify-between">
            <div className="w-9 h-9 rounded-xl bg-apple-yellow/15 border border-apple-yellow/30 flex items-center justify-center text-apple-yellow">
              <Star className="w-5 h-5 fill-apple-yellow drop-shadow-[0_0_8px_rgba(255,204,0,0.5)]" />
            </div>
            <span className="text-2xl font-bold font-mono text-white tracking-tight">
              {totalStars.toLocaleString('fa-IR')}
            </span>
          </div>
          <span className="text-xs font-medium text-apple-gray-300">ستاره‌های دریافتی</span>
        </div>

        {/* 2. Total XP Score */}
        <div className="relative overflow-hidden rounded-[20px] apple-glass p-4 flex flex-col justify-between h-[105px] shadow-sm">
          <div className="flex items-center justify-between">
            <div className="w-9 h-9 rounded-xl bg-sky-500/15 border border-sky-500/30 flex items-center justify-center text-sky-400">
              <Gem className="w-5 h-5 drop-shadow-[0_0_8px_rgba(56,189,248,0.5)]" />
            </div>
            <span className="text-2xl font-bold font-mono text-white tracking-tight">
              {score.toLocaleString('fa-IR')}
            </span>
          </div>
          <span className="text-xs font-medium text-apple-gray-300">امتیاز کل</span>
        </div>

        {/* 3. Language Level (Interactive Level Picker Trigger) */}
        <motion.button
          type="button"
          onClick={onLevelClick}
          whileTap={{ scale: 0.97 }}
          className="relative text-right overflow-hidden rounded-[20px] apple-glass hover:bg-white/10 border border-white/20 hover:border-purple-500/40 p-4 shadow-sm flex flex-col justify-between h-[105px] transition-all group focus:outline-none"
        >
          <div className="flex items-center justify-between w-full">
            <div className="w-9 h-9 rounded-xl bg-purple-500/15 border border-purple-500/30 flex items-center justify-center text-purple-400 group-hover:scale-105 transition-transform">
              <AudioWaveform className="w-5 h-5" />
            </div>
            <div className="flex items-center gap-1">
              <span className="text-lg font-bold font-mono text-purple-300">
                {cefr.code}
              </span>
              <ChevronLeft className="w-4 h-4 text-apple-gray-400 group-hover:-translate-x-0.5 transition-transform" />
            </div>
          </div>
          <div className="flex items-center justify-between w-full">
            <span className="text-xs font-medium text-apple-gray-300">سطح زبان انگلیسی</span>
            <span className="text-[11px] font-semibold text-purple-400">{cefr.titleFa}</span>
          </div>
        </motion.button>

        {/* 4. Completed Stages */}
        <div className="relative overflow-hidden rounded-[20px] apple-glass p-4 flex flex-col justify-between h-[105px] shadow-sm">
          <div className="flex items-center justify-between">
            <div className="w-9 h-9 rounded-xl bg-sky-500/15 border border-sky-500/30 flex items-center justify-center text-sky-400">
              <Gamepad2 className="w-5 h-5" />
            </div>
            <div className="flex items-baseline gap-1">
              <span className="text-2xl font-bold font-mono text-white tracking-tight">
                {completedStages.toLocaleString('fa-IR')}
              </span>
              <span className="text-xs font-mono text-apple-gray-400">
                /{totalStages.toLocaleString('fa-IR')}
              </span>
            </div>
          </div>
          <span className="text-xs font-medium text-apple-gray-300">مراحل تمام‌شده</span>
        </div>
      </div>
    </div>
  );
};
