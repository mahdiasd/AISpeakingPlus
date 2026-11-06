import React from 'react';
import confetti from 'canvas-confetti';
import {
  Play,
  RotateCcw,
  Sparkles,
  Target,
  Volume2,
  Lock,
  Crown,
  UserCheck,
  Flame,
} from 'lucide-react';
import {
  CupertinoCard,
  CupertinoButton,
  CupertinoPill,
  CupertinoStarRating,
} from '../cupertino';
import { Stage } from '../../types/stage';

export interface ActiveStageHeroProps {
  stage: Stage | null;
  onStartStage: (stage: Stage) => void;
  onOpenDetails: (stage: Stage) => void;
  onRequireAuth?: () => void;
  onRequireSubscription?: () => void;
  className?: string;
}

export const ActiveStageHero: React.FC<ActiveStageHeroProps> = ({
  stage,
  onStartStage,
  onOpenDetails,
  onRequireAuth,
  onRequireSubscription,
  className,
}) => {
  if (!stage) return null;

  const isCompleted = (stage.progress?.stars || 0) > 0;
  const isLocked = stage.lockStatus !== 'UNLOCKED';

  const handleCelebrate = () => {
    confetti({
      particleCount: 50,
      spread: 60,
      origin: { y: 0.7 },
      colors: ['#007aff', '#34c759', '#ffcc00', '#5856d6'],
    });
  };

  const handlePrimaryClick = () => {
    if (stage.lockStatus === 'LOCKED_REGISTRATION') {
      onRequireAuth?.();
      return;
    }
    if (stage.lockStatus === 'LOCKED_SUBSCRIPTION') {
      onRequireSubscription?.();
      return;
    }
    onStartStage(stage);
  };

  return (
    <CupertinoCard
      variant="glass"
      withSpecularHighlight
      className={`relative overflow-hidden group ${className || ''}`}
    >
      {/* Background artwork with gentle ambient blend */}
      {stage.backgroundUrl && (
        <div className="absolute inset-0 z-0 overflow-hidden opacity-30 group-hover:opacity-40 transition-opacity duration-500">
          <img
            src={stage.backgroundUrl}
            alt={stage.titleFa}
            className="w-full h-full object-cover filter blur-[2px] scale-105"
            loading="eager"
            onError={(e) => {
              // Hide broken image quietly
              (e.target as HTMLElement).style.display = 'none';
            }}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#0c0c0e] via-[#0c0c0e]/85 to-transparent" />
        </div>
      )}

      {/* Ambient Lighting Orbs */}
      <div className="absolute -top-12 -right-12 w-48 h-48 bg-apple-blue/25 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-10 -left-10 w-48 h-48 bg-apple-indigo/20 rounded-full blur-3xl pointer-events-none" />

      <div className="relative z-10 space-y-4">
        {/* Top Badges Row */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <CupertinoPill variant="blue" icon={<Sparkles className="w-3.5 h-3.5" />}>
              مرحله فعال {stage.orderIndex}
            </CupertinoPill>
            <span className="text-xs text-apple-gray-400 font-medium">{stage.phaseTitleFa}</span>
          </div>

          <div className="flex items-center gap-1.5 bg-white/10 px-2.5 py-1 rounded-full text-xs text-apple-yellow font-semibold select-none">
            <Flame className="w-3.5 h-3.5 fill-apple-yellow" />
            <span>تمرین امروز</span>
          </div>
        </div>

        {/* Character Avatar & Identity */}
        <div className="flex items-start gap-3.5 pt-1">
          {/* Avatar Disc with Apple Halo */}
          <div className="relative shrink-0">
            <div className="w-14 h-14 rounded-full p-0.5 bg-gradient-to-br from-apple-blue via-apple-teal to-transparent shadow-apple-md">
              <div className="w-full h-full rounded-full overflow-hidden bg-apple-gray-800 flex items-center justify-center">
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
                  <span className="text-lg font-bold text-white uppercase">
                    {stage.characterName.charAt(0)}
                  </span>
                )}
              </div>
            </div>

            {/* Active pulsing dot */}
            <span className="absolute bottom-0 right-0 w-3.5 h-3.5 rounded-full bg-apple-green border-2 border-[#121215]" />
          </div>

          {/* Titles & Character Info */}
          <div className="flex-1 min-w-0 space-y-0.5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-apple-gray-400 font-mono tracking-wide">
                {stage.characterName}
              </span>

              {isCompleted && (
                <CupertinoStarRating stars={stage.progress?.stars || 0} size="sm" />
              )}
            </div>

            <h3 className="text-xl font-bold text-white tracking-tight truncate">
              {stage.titleFa}
            </h3>

            <p className="text-xs text-apple-gray-400 font-mono italic truncate">
              {stage.title}
            </p>
          </div>
        </div>

        {/* Briefing Text */}
        <p className="text-xs text-apple-gray-300 leading-relaxed line-clamp-3">
          {stage.briefingFa}
        </p>

        {/* Target Objective Capsule */}
        {stage.targetObjectiveFa && (
          <div className="flex items-start gap-2 p-2.5 rounded-2xl bg-white/5 border border-white/10 text-xs text-apple-gray-200">
            <Target className="w-4 h-4 text-apple-blue shrink-0 mt-0.5" />
            <div className="space-y-0.5">
              <span className="text-[11px] font-bold text-apple-blue block">هدف مکالمه:</span>
              <p className="text-[11px] text-apple-gray-300 leading-normal">
                {stage.targetObjectiveFa}
              </p>
            </div>
          </div>
        )}

        {/* Action Buttons Row */}
        <div className="pt-2 flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5">
          <CupertinoButton
            variant="filled"
            size="md"
            fullWidth
            icon={
              isLocked ? (
                stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                  <Crown className="w-4 h-4 text-apple-yellow" />
                ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                  <UserCheck className="w-4 h-4" />
                ) : (
                  <Lock className="w-4 h-4" />
                )
              ) : isCompleted ? (
                <RotateCcw className="w-4 h-4" />
              ) : (
                <Play className="w-4 h-4 fill-white" />
              )
            }
            onClick={handlePrimaryClick}
          >
            {isLocked
              ? stage.lockStatus === 'LOCKED_SUBSCRIPTION'
                ? 'ارتقا به ویژه برای ورود'
                : stage.lockStatus === 'LOCKED_REGISTRATION'
                ? 'ثبت‌نام و ورود برای شروع'
                : 'قفل مرحله قبل'
              : isCompleted
              ? 'تکرار مکالمه هوشمند'
              : 'شروع مکالمه هوشمند'}
          </CupertinoButton>

          <div className="flex items-center gap-2">
            <CupertinoButton
              variant="glass"
              size="md"
              className="flex-1 sm:flex-none"
              icon={<Volume2 className="w-4 h-4 text-apple-blue" />}
              onClick={handleCelebrate}
            >
              نمونه صدا
            </CupertinoButton>

            <CupertinoButton
              variant="tinted"
              size="md"
              className="flex-1 sm:flex-none"
              onClick={() => onOpenDetails(stage)}
            >
              جزئیات سناریو
            </CupertinoButton>
          </div>
        </div>
      </div>
    </CupertinoCard>
  );
};
