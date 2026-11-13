import React from 'react';
import { motion } from 'motion/react';
import { Star, Sparkles, Gem, User } from 'lucide-react';
import { getAvatarUrl } from '../../utils/avatar';

export interface HomeHeaderStatsProps {
  totalStars?: number;
  isSubscriber?: boolean;
  remainingDays?: number;
  avatarUrl?: string | null;
  userName?: string | null;
  onProfileClick: () => void;
  onSubscribeClick: () => void;
  onLeaderboardClick?: () => void;
  className?: string;

  // Optional legacy props for backwards-compatibility:
  stats?: {
    totalStars: number;
    maxStars?: number;
    completedStagesCount?: number;
    totalStages?: number;
    progressPercentage?: number;
    currentTier?: string;
    remainingDays?: number;
  };
  seasonTitleFa?: string;
}

/**
 * Top HUD Bar for AI Speaking Plus Home Screen.
 * Fully aligned with Compose Multiplatform's `JourneyMapScreen` TopHud:
 * - Right (Start in RTL): Circular glass user profile button with avatar
 * - Center: Frosted liquid glass VIP Subscription button («پلاس شو ✨») or remaining days pill
 * - Left (End in RTL): Floating glass capsule star counter pill («۰ ستاره») with shining gold star icon
 */
export const HomeHeaderStats: React.FC<HomeHeaderStatsProps> = ({
  totalStars,
  isSubscriber,
  remainingDays,
  avatarUrl,
  userName,
  onProfileClick,
  onSubscribeClick,
  onLeaderboardClick,
  className = '',
  stats,
}) => {
  const starsCount = totalStars ?? stats?.totalStars ?? 0;
  const subscriberStatus = isSubscriber ?? (stats?.currentTier === 'SUBSCRIBER');
  const daysLeft = remainingDays ?? stats?.remainingDays ?? 0;
  const resolvedAvatar = avatarUrl ? getAvatarUrl(avatarUrl) : null;

  return (
    <header
      dir="rtl"
      className={`w-full relative flex items-center justify-between gap-2 pointer-events-auto select-none ${className}`}
    >
      {/* 1. RIGHT (Start in RTL): Circular Frosted Glass Profile Button */}
      <div className="flex items-center justify-start z-10 shrink-0">
        <motion.button
          whileTap={{ scale: 0.94 }}
          whileHover={{ scale: 1.04 }}
          transition={{ type: 'spring', stiffness: 450, damping: 26 }}
          type="button"
          onClick={onProfileClick}
          className="w-10 h-10 sm:w-11 sm:h-11 rounded-full bg-white/[0.12] hover:bg-white/[0.18] backdrop-blur-md flex items-center justify-center text-white shadow-[0_4px_16px_rgba(0,0,0,0.25)] overflow-hidden shrink-0 border border-white/30 active:scale-95 transition-all group cursor-pointer"
          title={userName || 'پروفایل کاربری'}
          aria-label="پروفایل کاربری"
        >
          {resolvedAvatar ? (
            <img
              src={resolvedAvatar}
              alt={userName || 'User Avatar'}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              onError={(e) => {
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          ) : (
            <User className="w-5 h-5 text-slate-200 group-hover:text-white transition-colors" />
          )}
        </motion.button>
      </div>

      {/* 2. CENTER: Frosted Glass Subscription Button («پلاس شو ✨») / Remaining Days Pill */}
      {/* Symmetrical placement: Middle child of justify-between row for equal spacing to profile and stars */}
      <div className="flex items-center justify-center z-10">
        {subscriberStatus ? (
          <motion.button
            whileTap={{ scale: 0.94 }}
            whileHover={{ scale: 1.03 }}
            transition={{ type: 'spring', stiffness: 450, damping: 26 }}
            type="button"
            onClick={onSubscribeClick}
            className="h-10 px-3.5 sm:px-4 rounded-full bg-indigo-50/95 dark:bg-indigo-500/25 hover:bg-indigo-100 dark:hover:bg-indigo-500/35 backdrop-blur-md border border-indigo-300/80 dark:border-indigo-300/40 flex items-center justify-center gap-1.5 sm:gap-2 text-indigo-950 dark:text-indigo-100 text-xs font-bold shadow-[0_4px_18px_rgba(99,102,241,0.25)] hover:brightness-105 active:scale-95 transition-all cursor-pointer"
            title="مدیریت اشتراک پلاس"
          >
            <Gem className="w-4 h-4 text-indigo-600 dark:text-indigo-300 drop-shadow-[0_0_8px_rgba(165,180,252,0.6)]" />
            <span className="font-bold">
              {daysLeft > 0 ? `${daysLeft.toLocaleString('fa-IR')} روز اشتراک` : 'اشتراک فعال'}
            </span>
          </motion.button>
        ) : (
          <motion.button
            whileTap={{ scale: 0.94 }}
            whileHover={{ scale: 1.03 }}
            transition={{ type: 'spring', stiffness: 450, damping: 26 }}
            type="button"
            onClick={onSubscribeClick}
            className="relative p-[1.5px] rounded-full overflow-hidden shadow-[0_4px_18px_rgba(245,158,11,0.25)] hover:brightness-105 active:scale-95 transition-all cursor-pointer group"
            title="ارتقا به نسخه پلاس و باز کردن تمام مراحل"
          >
            {/* Animated rotating border gradient (matching Compose animatedBorder sweep gradient) */}
            <motion.div
              animate={{ rotate: 360 }}
              transition={{ repeat: Infinity, duration: 3, ease: 'linear' }}
              className="absolute -inset-[100%] bg-[conic-gradient(from_0deg,#ab47bc,#2196f3,#7cb342,#ffb300,#ab47bc)]"
            />
            {/* Inner button surface */}
            <div className="relative z-10 h-10 px-3.5 sm:px-4 rounded-full bg-amber-50/95 dark:bg-[#151324]/90 backdrop-blur-md text-amber-900 dark:text-amber-100 font-black text-xs flex items-center justify-center gap-1.5 sm:gap-2">
              <Sparkles className="w-4 h-4 text-amber-600 dark:text-amber-300 animate-pulse" />
              <span>پلاس شو ✨</span>
            </div>
          </motion.button>
        )}
      </div>

      {/* 3. LEFT (End in RTL): Floating Glass Capsule Star Counter Pill */}
      <div className="flex items-center justify-end z-10 shrink-0">
        <motion.button
          whileTap={{ scale: 0.95 }}
          whileHover={{ scale: 1.03 }}
          transition={{ type: 'spring', stiffness: 450, damping: 26 }}
          type="button"
          onClick={onLeaderboardClick || onProfileClick}
          className="h-10 min-w-10 px-3 sm:px-3.5 rounded-full bg-amber-400/15 hover:bg-amber-400/25 backdrop-blur-md border border-amber-400/40 shadow-[0_4px_16px_rgba(255,204,0,0.2)] flex items-center justify-center gap-1.5 text-apple-yellow cursor-pointer hover:border-amber-400/60 active:scale-95 transition-all"
          title="مجموع ستاره‌های کسب شده در ماجراجویی"
        >
          <Star className="w-4 h-4 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_8px_rgba(255,204,0,0.7)]" />
          <span className="text-sm font-black font-mono tracking-tight text-white drop-shadow-sm">
            {starsCount.toLocaleString('fa-IR')}
          </span>
        </motion.button>
      </div>
    </header>
  );
};
