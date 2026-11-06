import React from 'react';
import { motion } from 'motion/react';
import { Sparkles, Star, Award, Layers } from 'lucide-react';
import { CupertinoCard, CupertinoPill } from '../cupertino';
import { JourneyStats } from '../../types/stage';

export interface HomeHeaderStatsProps {
  stats: JourneyStats;
  seasonTitleFa: string;
  className?: string;
}

export const HomeHeaderStats: React.FC<HomeHeaderStatsProps> = ({
  stats,
  seasonTitleFa,
  className,
}) => {
  const percentFormatted = Math.round(stats.progressPercentage * 100);

  return (
    <CupertinoCard variant="glass" withSpecularHighlight className={className}>
      {/* Background ambient lighting */}
      <div className="absolute -top-10 -right-10 w-36 h-36 bg-apple-blue/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-8 -left-8 w-36 h-36 bg-apple-yellow/15 rounded-full blur-3xl pointer-events-none" />

      <div className="relative space-y-3.5">
        {/* Top Header: Season Title & Stars HUD */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <CupertinoPill variant="blue" icon={<Sparkles className="w-3.5 h-3.5" />}>
              داستان اصلی
            </CupertinoPill>
            <h2 className="text-sm font-bold text-white tracking-tight">{seasonTitleFa}</h2>
          </div>

          {/* Stars Pill */}
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-apple-yellow/15 border border-apple-yellow/30 text-apple-yellow select-none">
            <Star className="w-4 h-4 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_6px_rgba(255,204,0,0.6)]" />
            <span className="text-xs font-bold font-mono">
              {stats.totalStars} / {stats.maxStars}
            </span>
          </div>
        </div>

        {/* Progress Bar with Apple Spring */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between text-xs text-apple-gray-400">
            <span>پیشرفت ماجراجویی</span>
            <span className="font-semibold text-white font-mono">{percentFormatted}٪</span>
          </div>

          <div className="relative h-2 w-full rounded-full bg-white/10 overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${stats.progressPercentage * 100}%` }}
              transition={{ type: 'spring', stiffness: 350, damping: 28 }}
              className="h-full rounded-full bg-gradient-to-l from-apple-blue to-apple-teal shadow-[0_0_12px_rgba(0,122,255,0.7)]"
            />
          </div>
        </div>

        {/* Quick Stats Grid */}
        <div className="grid grid-cols-3 gap-2 pt-1 text-center">
          <div className="bg-white/5 border border-white/10 p-2.5 rounded-2xl">
            <div className="flex items-center justify-center gap-1 text-xs text-apple-gray-400 pb-0.5">
              <Layers className="w-3.5 h-3.5 text-apple-blue" />
              <span>مراحل</span>
            </div>
            <span className="text-sm font-bold text-white font-mono">
              {stats.completedStagesCount} از {stats.totalStages}
            </span>
          </div>

          <div className="bg-white/5 border border-white/10 p-2.5 rounded-2xl">
            <div className="flex items-center justify-center gap-1 text-xs text-apple-gray-400 pb-0.5">
              <Star className="w-3.5 h-3.5 text-apple-yellow" />
              <span>امتیاز ستاره</span>
            </div>
            <span className="text-sm font-bold text-apple-yellow font-mono">
              {stats.totalStars}
            </span>
          </div>

          <div className="bg-white/5 border border-white/10 p-2.5 rounded-2xl">
            <div className="flex items-center justify-center gap-1 text-xs text-apple-gray-400 pb-0.5">
              <Award className="w-3.5 h-3.5 text-apple-green" />
              <span>سطح دسترسی</span>
            </div>
            <span className="text-xs font-bold text-apple-green">
              {stats.currentTier === 'SUBSCRIBER'
                ? 'ویژه (VIP)'
                : stats.currentTier === 'REGISTERED'
                ? 'کاربر رسمی'
                : 'مهمان'}
            </span>
          </div>
        </div>
      </div>
    </CupertinoCard>
  );
};
