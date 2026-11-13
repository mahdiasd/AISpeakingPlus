import React, { useMemo } from 'react';
import { motion } from 'motion/react';
import {
  Play,
  Lock,
  Crown,
  UserCheck,
  Star,
  CheckCircle2,
  ChevronRight,
  Sparkles,
  RotateCcw,
} from 'lucide-react';
import { clsx } from 'clsx';
import { PhaseGroup, Stage } from '../../types/stage';
import { CupertinoStarRating } from '../cupertino';

export interface StagesTimelineProps {
  phaseGroups: PhaseGroup[];
  currentActiveStageId: string | null;
  onSelectStage: (stage: Stage) => void;
  className?: string;
  onBack?: () => void;
  showHeaderBar?: boolean;
  showChapterCard?: boolean;
  totalStars?: number;
  maxStars?: number;
  completedStages?: number;
  totalStages?: number;
  progressPercentage?: number;
  seasonTitle?: string;
}

const toFa = (n: number | string) => Number(n).toLocaleString('fa-IR');

export const StagesTimeline: React.FC<StagesTimelineProps> = ({
  phaseGroups,
  currentActiveStageId,
  onSelectStage,
  className,
  onBack,
  showHeaderBar = false,
  showChapterCard = true,
  totalStars,
  maxStars,
  completedStages,
  totalStages,
  progressPercentage,
  seasonTitle,
}) => {
  // Derive fallback stats from phaseGroups if not provided
  const derivedTotalStages = useMemo(() => {
    if (totalStages !== undefined) return totalStages;
    return phaseGroups.reduce((acc, g) => acc + g.stages.length, 0);
  }, [phaseGroups, totalStages]);

  const derivedCompletedStages = useMemo(() => {
    if (completedStages !== undefined) return completedStages;
    return phaseGroups.reduce(
      (acc, g) => acc + g.stages.filter((s) => (s.progress?.stars || 0) > 0).length,
      0
    );
  }, [phaseGroups, completedStages]);

  const derivedTotalStars = useMemo(() => {
    if (totalStars !== undefined) return totalStars;
    return phaseGroups.reduce(
      (acc, g) =>
        acc + g.stages.reduce((sAcc, s) => sAcc + (s.progress?.stars || 0), 0),
      0
    );
  }, [phaseGroups, totalStars]);

  const derivedMaxStars = useMemo(() => {
    if (maxStars !== undefined) return maxStars;
    return derivedTotalStages * 3;
  }, [derivedTotalStages, maxStars]);

  const derivedProgressPercentage = useMemo(() => {
    if (progressPercentage !== undefined) return progressPercentage;
    return derivedTotalStages > 0 ? derivedCompletedStages / derivedTotalStages : 0;
  }, [derivedCompletedStages, derivedTotalStages, progressPercentage]);

  const derivedSeasonTitle = useMemo(() => {
    if (seasonTitle) return seasonTitle;
    const firstStage = phaseGroups[0]?.stages[0];
    return firstStage?.seasonTitleFa || 'فصل اول: روزهای آغازین در لندن';
  }, [phaseGroups, seasonTitle]);

  return (
    <div className={clsx('w-full space-y-6 select-none', className)} dir="rtl">
      {/* 1. Optional Floating Apple Navigation Bar */}
      {showHeaderBar && (
        <header className="sticky top-0 z-40 w-full pt-[max(env(safe-area-inset-top),10px)] pb-2.5 px-4 apple-glass bg-[#0e101a]/85 backdrop-blur-md border-b border-white/15 shadow-apple-sm select-none">
          <div className="flex items-center justify-between gap-3 max-w-lg mx-auto">
            {/* RTL Start Anchor (Right): Back Button pointing to right (>) */}
            <div className="flex items-center justify-start min-w-[44px]">
              {onBack ? (
                <button
                  type="button"
                  onClick={onBack}
                  className="w-10 h-10 rounded-full flex items-center justify-center bg-white/10 hover:bg-white/15 active:scale-95 border border-white/20 backdrop-blur-md shadow-apple-sm text-white transition-all touch-manipulation"
                  aria-label="بازگشت"
                >
                  <ChevronRight className="w-5 h-5 stroke-[2.5]" />
                </button>
              ) : (
                <div className="w-10 h-10" />
              )}
            </div>

            {/* Center Anchor: Symmetrical Title & Subtitle */}
            <div className="flex-1 flex flex-col items-center justify-center text-center">
              <h1 className="text-base sm:text-lg font-bold text-white tracking-tight">
                مرحله‌های ماجراجویی
              </h1>
              <span className="text-[11px] sm:text-xs text-apple-gray-400 font-normal">
                مسیر مکالمه زبان انگلیسی
              </span>
            </div>

            {/* RTL End Anchor (Left): Star Counter Badge */}
            <div className="flex items-center justify-end min-w-[44px]">
              <div className="h-9 px-3 rounded-full bg-apple-yellow/15 border border-apple-yellow/45 backdrop-blur-md flex items-center gap-1.5 text-apple-yellow shadow-apple-sm select-none">
                <Star className="w-3.5 h-3.5 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_6px_rgba(255,204,0,0.6)]" />
                <span className="text-xs font-bold font-mono">{toFa(derivedTotalStars)}</span>
              </div>
            </div>
          </div>
        </header>
      )}

      {/* 2. Pinned Chapter & Overall Progress Header Card */}
      {showChapterCard && (
        <div className="relative rounded-3xl apple-glass-card p-4 sm:p-5 select-none overflow-hidden border border-white/20 shadow-apple-md">
          {/* Specular highlight rim */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 top-0 h-[1.5px] bg-gradient-to-r from-transparent via-white/40 to-transparent pointer-events-none"
          />

          {/* Atmospheric ambient glow */}
          <div
            aria-hidden="true"
            className="absolute -top-12 right-1/4 w-40 h-40 rounded-full bg-apple-blue/20 blur-3xl pointer-events-none"
          />

          <div className="relative z-10 space-y-3.5">
            {/* Top Row: Season Badge + Star Collectibles Capsule */}
            <div className="flex items-center justify-between gap-2">
              {/* Season Pill */}
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-apple-yellow/15 border border-apple-yellow/35 text-apple-yellow text-xs font-bold">
                <Sparkles className="w-3.5 h-3.5 shrink-0 text-apple-yellow" />
                <span className="truncate max-w-[210px]">{derivedSeasonTitle}</span>
              </div>

              {/* Collectible Stars Capsule */}
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-apple-yellow/20 border border-apple-yellow/50 text-apple-yellow text-xs font-bold font-mono">
                <Star className="w-3.5 h-3.5 fill-apple-yellow shrink-0" />
                <span>
                  {toFa(derivedTotalStars)} / {toFa(derivedMaxStars)}
                </span>
              </div>
            </div>

            {/* Middle Row: Progress Numbers + Percentage Badge */}
            <div className="flex items-center justify-between gap-2 pt-0.5">
              <div className="space-y-0.5">
                <span className="text-[11px] text-apple-gray-400 block font-medium">
                  پیشرفت ماجراجویی مکالمه
                </span>
                <div className="flex items-baseline gap-1.5">
                  <span className="text-2xl font-extrabold text-white font-mono">
                    {toFa(derivedCompletedStages)}
                  </span>
                  <span className="text-xs font-semibold text-apple-gray-400">
                    از {toFa(derivedTotalStages)} مرحله تکمیل شده
                  </span>
                </div>
              </div>

              {/* Apple Percentage Pill */}
              <div className="px-3 py-1 rounded-xl bg-apple-blue/20 border border-apple-blue/40 text-apple-blue font-bold text-xs sm:text-sm font-mono shadow-sm">
                ٪{toFa(Math.round(derivedProgressPercentage * 100))}
              </div>
            </div>

            {/* Apple Gradient Progress Bar */}
            <div className="w-full h-2 rounded-full bg-white/10 overflow-hidden relative">
              <div
                className="h-full rounded-full bg-gradient-to-l from-apple-blue via-apple-teal to-apple-yellow transition-all duration-500 shadow-[0_0_12px_rgba(0,122,255,0.5)]"
                style={{ width: `${Math.min(100, Math.max(0, derivedProgressPercentage * 100))}%` }}
              />
            </div>
          </div>
        </div>
      )}

      {/* 3. Phase Sections & Stage Items */}
      {phaseGroups.map((group, groupIdx) => {
        const isPhaseFinished = group.isCompleted;

        return (
          <section key={group.phaseNumber} className={clsx('space-y-3.5', groupIdx > 0 && 'pt-3')}>
            {/* Phase Section Header Card */}
            <div
              className={clsx(
                'relative rounded-[22px] p-3.5 sm:p-4 select-none overflow-hidden transition-all duration-300 border backdrop-blur-md',
                isPhaseFinished
                  ? 'bg-gradient-to-br from-[#261e12]/95 via-[#1a140c]/95 to-[#120e09]/95 border-amber-500/45 shadow-[0_4px_24px_rgba(255,204,0,0.12)]'
                  : 'bg-gradient-to-br from-[#131b2e]/95 via-[#0e1322]/95 to-[#080b14]/95 border-apple-blue/45 shadow-[0_4px_24px_rgba(0,122,255,0.12)]'
              )}
            >
              {/* Specular Edge Highlight */}
              <div
                aria-hidden="true"
                className={clsx(
                  'absolute inset-x-0 top-0 h-[1.5px] pointer-events-none',
                  isPhaseFinished
                    ? 'bg-gradient-to-r from-transparent via-apple-yellow/80 to-transparent'
                    : 'bg-gradient-to-r from-transparent via-apple-blue/80 to-transparent'
                )}
              />

              <div className="relative z-10 space-y-3">
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-3 min-w-0">
                    {/* 3D Phase Orb Badge */}
                    <div
                      className={clsx(
                        'w-11 h-11 rounded-full flex items-center justify-center font-bold text-sm shadow-apple-sm shrink-0 border border-white/40',
                        isPhaseFinished
                          ? 'bg-gradient-to-br from-[#FFD60A] to-[#D4A000] text-black shadow-[0_0_14px_rgba(255,214,10,0.4)]'
                          : 'bg-gradient-to-br from-[#0A84FF] to-[#0056B3] text-white font-mono shadow-[0_0_14px_rgba(10,132,255,0.4)]'
                      )}
                    >
                      {isPhaseFinished ? (
                        <CheckCircle2 className="w-5 h-5 fill-black text-black" />
                      ) : (
                        toFa(group.phaseNumber)
                      )}
                    </div>

                    <div className="space-y-0.5 min-w-0">
                      <div className="flex items-center gap-2">
                        <span
                          className={clsx(
                            'text-[10px] font-bold px-2 py-0.5 rounded-full border',
                            isPhaseFinished
                              ? 'bg-apple-yellow/15 border-apple-yellow/35 text-apple-yellow'
                              : 'bg-apple-blue/15 border-apple-blue/35 text-apple-blue'
                          )}
                        >
                          فاز {toFa(group.phaseNumber)}
                        </span>
                      </div>
                      <h3 className="text-sm sm:text-base font-bold text-white tracking-tight truncate">
                        {group.phaseTitleFa}
                      </h3>
                    </div>
                  </div>

                  {isPhaseFinished ? (
                    <div className="px-3 py-1 rounded-full text-xs font-bold bg-apple-yellow/20 text-apple-yellow border border-apple-yellow/45 shadow-sm shrink-0">
                      تکمیل شد ✨
                    </div>
                  ) : (
                    <div className="px-2.5 py-1 rounded-full text-[11px] font-mono font-semibold bg-white/10 text-apple-gray-300 border border-white/15 shrink-0">
                      {toFa(group.completedStages)} از {toFa(group.totalStages)} مرحله
                    </div>
                  )}
                </div>

                {/* Slim Apple progress indicator */}
                <div className="w-full h-1.5 rounded-full bg-white/10 overflow-hidden">
                  <div
                    className={clsx(
                      'h-full rounded-full transition-all duration-500',
                      isPhaseFinished
                        ? 'bg-gradient-to-l from-apple-yellow to-amber-500'
                        : 'bg-gradient-to-l from-apple-blue to-apple-teal'
                    )}
                    style={{
                      width: `${Math.min(100, Math.max(0, group.progressPercentage * 100))}%`,
                    }}
                  />
                </div>
              </div>
            </div>

            {/* Stages Journey Track & Items */}
            <div className="relative pl-0.5 pr-0.5 space-y-3">
              {group.stages.map((stage, idx) => {
                const isActive = stage.id === currentActiveStageId;
                const starsEarned = stage.progress?.stars || 0;
                const isCompleted = starsEarned > 0;
                const isLocked = stage.lockStatus !== 'UNLOCKED';
                const isFirstInPhase = idx === 0;
                const isLastInPhase = idx === group.stages.length - 1;

                return (
                  <div key={stage.id} className="relative flex items-stretch gap-3">
                    {/* Vertical Timeline Track Column (on Right in RTL) */}
                    <div className="relative flex flex-col items-center w-11 shrink-0 select-none">
                      {/* Connecting vertical road track */}
                      <div
                        className={clsx(
                          'absolute w-[2.5px] z-0 transition-all',
                          isFirstInPhase ? 'top-6' : 'top-0',
                          isLastInPhase ? 'bottom-6' : 'bottom-0',
                          isCompleted
                            ? 'bg-apple-yellow/60'
                            : isActive
                            ? 'bg-gradient-to-b from-apple-blue to-white/20'
                            : 'bg-white/15'
                        )}
                      />

                      {/* Timeline Node Circle */}
                      <div className="relative z-10 mt-3.5 flex items-center justify-center">
                        {isActive && (
                          <motion.div
                            animate={{ scale: [1, 1.35, 1], opacity: [0.65, 0.15, 0.65] }}
                            transition={{ repeat: Infinity, duration: 1.6, ease: 'easeInOut' }}
                            className="absolute -inset-1.5 rounded-full bg-apple-blue/50 pointer-events-none"
                          />
                        )}

                        <div
                          className={clsx(
                            'rounded-full flex items-center justify-center font-bold text-xs transition-transform shadow-apple-sm',
                            isActive
                              ? 'w-9 h-9 bg-gradient-to-br from-apple-blue to-blue-700 text-white ring-2 ring-white shadow-[0_0_16px_rgba(0,122,255,0.7)]'
                              : isCompleted
                              ? 'w-8 h-8 bg-gradient-to-br from-apple-yellow to-amber-600 text-black ring-1 ring-white/60'
                              : stage.lockStatus === 'LOCKED_SUBSCRIPTION'
                              ? 'w-8 h-8 bg-gradient-to-br from-[#3A3010] to-[#221A06] text-apple-yellow border border-apple-yellow/60'
                              : stage.lockStatus === 'LOCKED_REGISTRATION'
                              ? 'w-8 h-8 bg-purple-950 text-purple-300 border border-purple-500/40'
                              : isLocked
                              ? 'w-8 h-8 bg-[#1c1f2e] text-apple-gray-400 border border-white/10'
                              : 'w-8 h-8 bg-sky-600 text-white border border-white/30'
                          )}
                        >
                          {isActive ? (
                            <Play className="w-4 h-4 fill-white translate-x-[0.5px]" />
                          ) : isCompleted ? (
                            <Star className="w-4 h-4 fill-black text-black" />
                          ) : stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                            <Crown className="w-3.5 h-3.5 text-apple-yellow" />
                          ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                            <UserCheck className="w-3.5 h-3.5 text-purple-300" />
                          ) : isLocked ? (
                            <Lock className="w-3.5 h-3.5 text-apple-gray-400" />
                          ) : (
                            <span className="font-mono">{toFa(stage.orderIndex)}</span>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Apple Frosted Glass Stage Card (on Left of track in RTL) */}
                    <motion.div
                      whileTap={{ scale: 0.98 }}
                      transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                      onClick={() => onSelectStage(stage)}
                      className={clsx(
                        'flex-1 relative rounded-[22px] p-3.5 sm:p-4 select-none cursor-pointer transition-all duration-200 touch-manipulation',
                        isActive
                          ? 'border border-apple-blue/80 shadow-[0_0_28px_rgba(0,122,255,0.4)] bg-gradient-to-br from-blue-950/40 via-slate-900/60 to-slate-950/80 backdrop-blur-[14px]'
                          : isCompleted
                          ? 'apple-glass border border-apple-yellow/30 shadow-apple-sm hover:border-apple-yellow/50'
                          : isLocked
                          ? 'apple-glass-card opacity-80 hover:opacity-100 border border-white/10'
                          : 'apple-glass-card border border-white/20 hover:border-white/30 shadow-apple-sm'
                      )}
                    >
                      {/* Top Row: Character Identity + Status / Stars Badge */}
                      <div className="flex items-center justify-between gap-2 pb-2">
                        {/* Start (Right in RTL): Character Avatar & Name */}
                        <div className="flex items-center gap-2 min-w-0">
                          <div
                            className={clsx(
                              'w-6 h-6 rounded-full overflow-hidden bg-slate-800 flex items-center justify-center shrink-0 ring-1',
                              isActive
                                ? 'ring-apple-blue'
                                : isCompleted
                                ? 'ring-apple-yellow/70'
                                : 'ring-white/20'
                            )}
                          >
                            {stage.characterAvatarUrl ? (
                              <img
                                src={stage.characterAvatarUrl}
                                alt={stage.characterName}
                                className="w-full h-full object-cover"
                                onError={(e) => {
                                  (e.target as HTMLElement).style.display = 'none';
                                }}
                              />
                            ) : (
                              <span className="text-[10px] font-bold text-white uppercase font-mono">
                                {stage.characterName.charAt(0)}
                              </span>
                            )}
                          </div>
                          <span
                            className={clsx(
                              'text-xs font-semibold font-mono tracking-tight truncate max-w-[150px] sm:max-w-[210px]',
                              isCompleted ? 'text-apple-yellow font-bold' : 'text-slate-300'
                            )}
                            dir="ltr"
                          >
                            {stage.characterName}
                          </span>
                        </div>

                        {/* End (Left in RTL): Badge / Stars */}
                        <div className="shrink-0">
                          {isActive ? (
                            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-apple-blue/20 text-apple-blue border border-apple-blue/40 shadow-sm">
                              مرحله فعال شما
                            </span>
                          ) : isCompleted ? (
                            <CupertinoStarRating stars={starsEarned} size="sm" />
                          ) : stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                            <span className="inline-flex items-center gap-1 text-[11px] text-apple-yellow font-bold">
                              <Crown className="w-3.5 h-3.5" />
                              <span>اشتراک ویژه</span>
                            </span>
                          ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                            <span className="inline-flex items-center gap-1 text-[11px] text-purple-300 font-medium">
                              <UserCheck className="w-3.5 h-3.5" />
                              <span>ورود به حساب</span>
                            </span>
                          ) : null}
                        </div>
                      </div>

                      {/* Middle Row: Artwork Thumbnail + Stage Title & Briefing */}
                      <div className="flex items-center gap-3">
                        {/* Scene Artwork Preview Thumbnail */}
                        <div className="relative w-16 h-16 rounded-xl overflow-hidden bg-slate-900 shrink-0 border border-white/15">
                          {stage.backgroundUrl ? (
                            <img
                              src={stage.backgroundUrl}
                              alt={stage.titleFa}
                              className="w-full h-full object-cover"
                              onError={(e) => {
                                (e.target as HTMLElement).style.display = 'none';
                              }}
                            />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center text-apple-gray-500 font-mono text-xs">
                              {stage.orderIndex}
                            </div>
                          )}

                          {/* Scrim overlay with lock/crown icon if stage is locked */}
                          {isLocked && (
                            <div className="absolute inset-0 bg-black/60 backdrop-blur-[1px] flex items-center justify-center">
                              {stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                                <Crown className="w-5 h-5 text-apple-yellow drop-shadow-md" />
                              ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                                <UserCheck className="w-4 h-4 text-purple-300" />
                              ) : (
                                <Lock className="w-4 h-4 text-white/90" />
                              )}
                            </div>
                          )}
                        </div>

                        {/* Title and Briefing Text */}
                        <div className="flex-1 min-w-0 space-y-0.5">
                          <h4 className="text-sm sm:text-base font-bold text-white tracking-tight truncate">
                            {stage.titleFa}
                          </h4>
                          {stage.title && stage.title !== stage.titleFa && (
                            <span
                              className="text-[11px] text-apple-gray-400 font-mono italic truncate block"
                              dir="ltr"
                            >
                              {stage.title}
                            </span>
                          )}
                          <p className="text-xs text-slate-300 line-clamp-2 leading-relaxed">
                            {stage.briefingFa || stage.title}
                          </p>
                        </div>
                      </div>

                      {/* Bottom Row: Micro-Status Guidance + Apple CTA Button / Chip */}
                      <div className="flex items-center justify-between gap-2 pt-3 mt-2 border-t border-white/10">
                        {/* Status guidance text on Start (Right in RTL) */}
                        <span className="text-xs font-medium truncate flex-1 min-w-0">
                          {isActive ? (
                            <span className="text-sky-400">آماده مکالمه صوتی و متنی</span>
                          ) : isCompleted ? (
                            <span className="text-apple-yellow font-mono">
                              بهترین امتیاز: {toFa(stage.progress?.bestScore || 0)}
                            </span>
                          ) : stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                            <span className="text-amber-400/90">دسترسی با اشتراک ویژه</span>
                          ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                            <span className="text-purple-300">ورود به حساب لازم است</span>
                          ) : stage.lockStatus === 'LOCKED_PREVIOUS_STAGE' ? (
                            <span className="text-slate-400">نیاز به اتمام مرحله قبل</span>
                          ) : (
                            <span className="text-slate-400">مرحله آزاد</span>
                          )}
                        </span>

                        {/* CTA button or chip on End (Left in RTL) */}
                        <div className="shrink-0">
                          {isActive ? (
                            <div className="px-3.5 py-1.5 rounded-full bg-gradient-to-r from-apple-blue to-blue-600 text-white font-bold text-xs flex items-center gap-1.5 shadow-[0_4px_16px_rgba(0,122,255,0.4)] hover:brightness-110 active:scale-95 transition-all">
                              <Play className="w-3.5 h-3.5 fill-white" />
                              <span>ادامه مکالمه</span>
                            </div>
                          ) : isCompleted ? (
                            <div className="px-3 py-1 rounded-full bg-white/10 hover:bg-white/15 border border-white/20 text-sky-300 text-xs font-semibold flex items-center gap-1">
                              <RotateCcw className="w-3 h-3" />
                              <span>تکرار مرحله</span>
                            </div>
                          ) : stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                            <div className="px-3 py-1 rounded-full bg-amber-500/15 border border-amber-500/40 text-apple-yellow text-xs font-bold flex items-center gap-1">
                              <Crown className="w-3 h-3" />
                              <span>ویژه اشتراک</span>
                            </div>
                          ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                            <div className="px-3 py-1 rounded-full bg-purple-500/15 border border-purple-500/40 text-purple-300 text-xs font-bold flex items-center gap-1">
                              <span>ورود به حساب</span>
                            </div>
                          ) : (
                            <div className="px-3 py-1 rounded-full bg-white/5 border border-white/10 text-slate-400 text-xs flex items-center gap-1">
                              <Lock className="w-3 h-3" />
                              <span>قفل</span>
                            </div>
                          )}
                        </div>
                      </div>
                    </motion.div>
                  </div>
                );
              })}
            </div>
          </section>
        );
      })}
    </div>
  );
};
