import React from 'react';
import { motion } from 'motion/react';
import { Star, Crown, User } from 'lucide-react';
import { getAvatarUrl } from '../../utils/avatar';

export interface HomeHeaderStatsProps {
  totalStars?: number;
  isSubscriber?: boolean;
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
  };
  seasonTitleFa?: string;
}

/**
 * Top HUD Bar for AI Speaking Plus Home Screen.
 * Fully aligned with Compose Multiplatform's `JourneyMapScreen` TopHud:
 * - Right (Start in RTL): Circular user profile button with avatar
 * - Center: Floating glass capsule star counter pill («۰ ستاره») with shining gold star icon
 * - Left (End in RTL): VIP glass button with gold gradient («اشتراک ویژه») with crown icon
 */
export const HomeHeaderStats: React.FC<HomeHeaderStatsProps> = ({
  totalStars,
  isSubscriber,
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
  const resolvedAvatar = avatarUrl ? getAvatarUrl(avatarUrl) : null;

  return (
    <header
      dir="rtl"
      className={`w-full flex items-center justify-between gap-3 pointer-events-auto select-none ${className}`}
    >
      {/* 1. RIGHT (Start in RTL): Circular User Profile Button with Avatar */}
      <motion.button
        whileTap={{ scale: 0.94 }}
        whileHover={{ scale: 1.04 }}
        transition={{ type: 'spring', stiffness: 450, damping: 26 }}
        type="button"
        onClick={onProfileClick}
        className="w-10 h-10 sm:w-11 sm:h-11 rounded-full apple-glass-pill flex items-center justify-center text-white shadow-apple-sm overflow-hidden shrink-0 border border-white/25 active:scale-95 transition-all group cursor-pointer"
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

      {/* 2. CENTER: Floating Glass Capsule Star Counter Pill («۰ ستاره») */}
      <motion.button
        whileTap={{ scale: 0.95 }}
        whileHover={{ scale: 1.03 }}
        transition={{ type: 'spring', stiffness: 450, damping: 26 }}
        type="button"
        onClick={onLeaderboardClick || onProfileClick}
        className="h-10 px-4 rounded-full apple-glass-pill border border-amber-400/40 bg-gradient-to-b from-amber-400/15 via-white/[0.08] to-transparent shadow-[0_4px_18px_rgba(255,204,0,0.22)] flex items-center justify-center gap-2 text-apple-yellow cursor-pointer hover:border-amber-400/60 hover:bg-amber-400/25 active:scale-95 transition-all backdrop-blur-md"
        title="مجموع ستاره‌های کسب شده در ماجراجویی"
      >
        <Star className="w-4 h-4 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_8px_rgba(255,204,0,0.75)] animate-pulse" />
        <span className="text-sm font-black font-mono tracking-tight text-white drop-shadow-sm">
          {starsCount.toLocaleString('fa-IR')}
        </span>
        <span className="text-xs font-bold text-apple-yellow">
          ستاره
        </span>
      </motion.button>

      {/* 3. LEFT (End in RTL): VIP Glass Button with Gold Gradient («اشتراک ویژه») & Crown */}
      {subscriberStatus ? (
        <motion.button
          whileTap={{ scale: 0.94 }}
          whileHover={{ scale: 1.03 }}
          transition={{ type: 'spring', stiffness: 450, damping: 26 }}
          type="button"
          onClick={onSubscribeClick}
          className="h-10 px-3.5 sm:px-4 rounded-full apple-glass bg-gradient-to-r from-purple-900/50 via-purple-800/40 to-indigo-900/50 border border-purple-400/50 flex items-center justify-center gap-1.5 sm:gap-2 text-purple-200 text-xs font-bold shadow-[0_4px_16px_rgba(168,85,247,0.3)] hover:brightness-110 active:scale-95 transition-all cursor-pointer"
          title="عضویت ویژه فعال است"
        >
          <Crown className="w-4 h-4 fill-purple-300 text-purple-200 drop-shadow-[0_0_6px_rgba(168,85,247,0.6)]" />
          <span className="font-bold">اشتراک طلایی</span>
        </motion.button>
      ) : (
        <motion.button
          whileTap={{ scale: 0.94 }}
          whileHover={{ scale: 1.03 }}
          transition={{ type: 'spring', stiffness: 450, damping: 26 }}
          type="button"
          onClick={onSubscribeClick}
          className="h-10 px-3.5 sm:px-4 rounded-full bg-gradient-to-r from-amber-400 via-yellow-400 to-amber-500 hover:from-amber-300 hover:to-yellow-300 text-black font-black text-xs flex items-center justify-center gap-1.5 sm:gap-2 shadow-[0_4px_16px_rgba(255,204,0,0.45)] border border-yellow-200/60 active:scale-95 transition-all cursor-pointer"
          title="خرید یا ارتقا به اشتراک ویژه"
        >
          <Crown className="w-4 h-4 fill-black text-black" />
          <span>اشتراک ویژه</span>
        </motion.button>
      )}
    </header>
  );
};
