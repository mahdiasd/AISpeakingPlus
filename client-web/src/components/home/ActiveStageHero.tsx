import React from 'react';
import { motion } from 'motion/react';
import {
  Play,
  RotateCcw,
  Lock,
  Crown,
  Layers,
  Sparkles,
  User,
} from 'lucide-react';
import { CupertinoStarRating } from '../cupertino';
import { Stage } from '../../types/stage';
import { getAssetUrl } from '../../utils/avatar';

export interface ActiveStageHeroProps {
  stage: Stage | null;
  totalStages?: number;
  completedStagesCount?: number;
  onStartStage: (stage: Stage) => void;
  onOpenTimeline: () => void;
  onOpenDetails?: (stage: Stage) => void;
  onRequireAuth?: () => void;
  onRequireSubscription?: () => void;
  className?: string;
  isReplay?: boolean;
}

/**
 * ActiveStageHero Component
 * Fully aligned with Compose Multiplatform's `JourneyMapScreen.kt` StageCard:
 * - Floating centered card with `.apple-glass-card` (blur 14-16px) allowing London flight wallpaper to shine through.
 * - Top Badge: «مرحله ۱ از ۳۰» with stage star ratings.
 * - Stage Title: «پرواز به لندن» (stage.titleFa).
 * - 3D Character Portrait: "Flight Attendant Emily" with Apple halo ring and status indicator.
 * - Primary Tactile Spring CTA Button: «شروع مرحله» with rich vibrant gradient.
 * - Secondary Glass Button: «همه مرحله‌ها و پیشرفت» which opens the stages roadmap timeline modal.
 */
export const ActiveStageHero: React.FC<ActiveStageHeroProps> = ({
  stage,
  totalStages = 30,
  completedStagesCount = 0,
  onStartStage,
  onOpenTimeline,
  onRequireAuth,
  onRequireSubscription,
  className = '',
  isReplay = false,
}) => {
  if (!stage) return null;

  const stars = stage.progress?.stars || 0;
  const isCompleted = stars > 0;

  // Resolve 3D Character portrait with fallback
  const characterAvatar = stage.characterAvatarUrl
    ? getAssetUrl(stage.characterAvatarUrl)
    : getAssetUrl('resources/avatars/emily.webp');

  const handlePrimaryClick = () => {
    if (stage.lockStatus === 'LOCKED_REGISTRATION') {
      onRequireAuth?.();
      return;
    }
    if (stage.lockStatus === 'LOCKED_SUBSCRIPTION') {
      onRequireSubscription?.();
      return;
    }
    if (stage.lockStatus === 'LOCKED_PREVIOUS_STAGE') {
      return;
    }
    onStartStage(stage);
  };

  return (
    <div
      dir="rtl"
      className={`apple-glass-card rounded-[28px] sm:rounded-[32px] p-5 sm:p-6 w-full max-w-lg mx-auto select-none overflow-hidden relative shadow-[0_24px_60px_-8px_rgba(0,0,0,0.65)] border border-white/20 transition-all ${className}`}
    >
      {/* Specular Apple Rim Light Bar */}
      <div
        aria-hidden="true"
        className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none z-20"
      />

      {/* Ambient Lighting Orbs for Liquid Glass Depth */}
      <div className="absolute -top-10 -right-10 w-44 h-44 bg-apple-blue/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-8 -left-8 w-44 h-44 bg-apple-teal/15 rounded-full blur-3xl pointer-events-none" />

      <div className="relative z-10 space-y-4">
        {/* 1. TOP ROW: Stage Badge («مرحله ۱ از ۳۰») & Stars */}
        <div className="flex items-center justify-between gap-2">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full apple-glass-pill border border-apple-teal/30 text-apple-teal text-xs font-extrabold tracking-wide">
            <Sparkles className="w-3.5 h-3.5 text-apple-teal" />
            <span>
              مرحله {stage.orderIndex.toLocaleString('fa-IR')} از {totalStages.toLocaleString('fa-IR')}
            </span>
          </div>

          <div className="apple-glass-pill px-2.5 py-1 rounded-full border border-white/20">
            <CupertinoStarRating stars={stars} size="sm" />
          </div>
        </div>

        {/* 2. TITLE: Stage Title («پرواز به لندن») & English Subtitle */}
        <div className="space-y-1">
          <h2 className="text-xl sm:text-2xl font-black text-white tracking-tight leading-tight">
            {stage.titleFa}
          </h2>
          <p className="text-xs text-slate-300 font-mono italic tracking-wide" dir="ltr">
            {stage.title}
          </p>
        </div>

        {/* 3. 3D CHARACTER PORTRAIT: "Flight Attendant Emily" */}
        <div className="flex items-center gap-3.5 p-3 rounded-2xl bg-white/[0.05] border border-white/10 backdrop-blur-sm">
          {/* Avatar Disc with Specular Halo Ring */}
          <div className="relative shrink-0">
            <div className="w-13 h-13 sm:w-14 sm:h-14 rounded-full p-0.5 bg-gradient-to-tr from-apple-blue via-teal-400 to-amber-300 shadow-apple-md">
              <div className="w-full h-full rounded-full overflow-hidden bg-[#0c0d16] flex items-center justify-center">
                {characterAvatar ? (
                  <img
                    src={characterAvatar}
                    alt={stage.characterName}
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      // Fallback gracefully
                      (e.target as HTMLElement).style.display = 'none';
                    }}
                  />
                ) : (
                  <User className="w-6 h-6 text-apple-blue" />
                )}
              </div>
            </div>

            {/* Online AI Host Active Pulse Dot */}
            <span
              className="absolute bottom-0 right-0 w-3.5 h-3.5 rounded-full bg-apple-green border-2 border-[#121218] shadow-[0_0_8px_rgba(52,199,89,0.8)]"
              title="هم‌صحبت آماده گفتگو"
            />
          </div>

          {/* Character Identity & British Accent Details */}
          <div className="flex-1 min-w-0 space-y-0.5">
            <div className="flex items-center justify-between">
              <span className="text-sm font-bold text-white tracking-tight" dir="ltr">
                {stage.characterName}
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-apple-blue/20 text-apple-teal font-extrabold border border-apple-blue/30">
                میزبان AI
              </span>
            </div>
            <p className="text-xs text-slate-300 truncate">
              {stage.briefingFa ? stage.briefingFa.slice(0, 52) + '...' : 'سناریوی تعاملی و مکالمه واقعی با هوش مصنوعی'}
            </p>
          </div>
        </div>

        {/* 4. ACTIONS: Tactile Spring Primary CTA & Glass Secondary Button */}
        <div className="space-y-2.5 pt-1">
          {/* Primary Button: Apple Spring Tactile CTA */}
          {stage.lockStatus === 'UNLOCKED' && (
            <motion.button
              whileTap={{ scale: 0.97 }}
              whileHover={{ scale: 1.01 }}
              transition={{ type: 'spring', stiffness: 450, damping: 25 }}
              type="button"
              onClick={handlePrimaryClick}
              className="w-full h-12 px-5 rounded-2xl bg-gradient-to-r from-apple-blue via-[#0066e0] to-indigo-600 hover:brightness-110 active:scale-[0.98] text-white font-black text-sm sm:text-base shadow-[0_6px_22px_rgba(0,122,255,0.45)] flex items-center justify-center gap-2.5 transition-all border border-white/20 cursor-pointer"
            >
              {isReplay || isCompleted ? (
                <>
                  <RotateCcw className="w-4 h-4 ml-0.5" />
                  <span>تکرار مرحله</span>
                </>
              ) : (
                <>
                  <Play className="w-4 h-4 fill-white ml-0.5" />
                  <span>شروع مرحله</span>
                </>
              )}
            </motion.button>
          )}

          {stage.lockStatus === 'LOCKED_REGISTRATION' && (
            <motion.button
              whileTap={{ scale: 0.97 }}
              whileHover={{ scale: 1.01 }}
              transition={{ type: 'spring', stiffness: 450, damping: 25 }}
              type="button"
              onClick={handlePrimaryClick}
              className="w-full h-12 px-5 rounded-2xl bg-gradient-to-r from-[#5856D6] to-purple-700 hover:brightness-110 active:scale-[0.98] text-white font-black text-xs sm:text-sm shadow-apple-md flex items-center justify-center gap-2 transition-all border border-white/20 cursor-pointer"
            >
              <Lock className="w-4 h-4" />
              <span>ورود و باز کردن مرحله</span>
            </motion.button>
          )}

          {stage.lockStatus === 'LOCKED_SUBSCRIPTION' && (
            <motion.button
              whileTap={{ scale: 0.97 }}
              whileHover={{ scale: 1.01 }}
              transition={{ type: 'spring', stiffness: 450, damping: 25 }}
              type="button"
              onClick={handlePrimaryClick}
              className="w-full h-12 px-5 rounded-2xl bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange hover:brightness-110 active:scale-[0.98] text-black font-black text-xs sm:text-sm shadow-[0_6px_20px_rgba(255,204,0,0.4)] flex items-center justify-center gap-2 transition-all border border-yellow-200/50 cursor-pointer"
            >
              <Crown className="w-4 h-4 fill-black" />
              <span>باز کردن با اشتراک ویژه</span>
            </motion.button>
          )}

          {stage.lockStatus === 'LOCKED_PREVIOUS_STAGE' && (
            <button
              type="button"
              disabled
              className="w-full h-12 px-5 rounded-2xl bg-white/10 text-slate-400 font-bold text-xs sm:text-sm border border-white/10 flex items-center justify-center gap-2 cursor-not-allowed opacity-80"
            >
              <Lock className="w-4 h-4" />
              <span>ابتدا مرحله قبل را به پایان برسانید</span>
            </button>
          )}

          {/* Secondary Button: Apple Glass «همه مرحله‌ها و پیشرفت» */}
          <motion.button
            whileTap={{ scale: 0.98 }}
            whileHover={{ scale: 1.01 }}
            transition={{ type: 'spring', stiffness: 450, damping: 25 }}
            type="button"
            onClick={onOpenTimeline}
            className="w-full h-11 px-4 rounded-2xl apple-glass-pill hover:bg-white/15 active:scale-[0.98] text-slate-200 font-bold text-xs sm:text-sm flex items-center justify-center gap-2 transition-all border border-white/15 cursor-pointer"
          >
            <Layers className="w-4 h-4 text-apple-blue" />
            <span>
              {completedStagesCount > 0
                ? `همه مرحله‌ها و پیشرفت (${completedStagesCount.toLocaleString('fa-IR')} انجام شده)`
                : 'همه مرحله‌ها و پیشرفت'}
            </span>
          </motion.button>
        </div>
      </div>
    </div>
  );
};
