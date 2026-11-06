import React from 'react';
import {
  Play,
  RotateCcw,
  Lock,
  Crown,
  UserCheck,
  Target,
  Sparkles,
  Award,
} from 'lucide-react';
import {
  CupertinoModal,
  CupertinoButton,
  CupertinoPill,
  CupertinoStarRating,
} from '../cupertino';
import { Stage } from '../../types/stage';

export interface StageBriefingModalProps {
  stage: Stage | null;
  isOpen: boolean;
  onClose: () => void;
  onStartStage: (stage: Stage) => void;
  onRequireAuth: () => void;
  onRequireSubscription: () => void;
}

export const StageBriefingModal: React.FC<StageBriefingModalProps> = ({
  stage,
  isOpen,
  onClose,
  onStartStage,
  onRequireAuth,
  onRequireSubscription,
}) => {
  if (!stage) return null;

  const isCompleted = (stage.progress?.stars || 0) > 0;
  const isLocked = stage.lockStatus !== 'UNLOCKED';

  const handleAction = () => {
    onClose();
    if (stage.lockStatus === 'LOCKED_REGISTRATION') {
      onRequireAuth();
    } else if (stage.lockStatus === 'LOCKED_SUBSCRIPTION') {
      onRequireSubscription();
    } else if (!isLocked) {
      onStartStage(stage);
    }
  };

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={onClose}
      title={`مرحله ${stage.orderIndex}: ${stage.titleFa}`}
      subtitle={stage.title}
    >
      <div className="space-y-4">
        {/* Full Scene Artwork Preview */}
        <div className="relative h-44 w-full rounded-2xl overflow-hidden bg-apple-gray-800 border border-white/10 select-none shadow-apple-md">
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
            <div className="w-full h-full flex items-center justify-center text-apple-gray-500">
              تصویر مرحله
            </div>
          )}

          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />

          {/* Badges on Artwork */}
          <div className="absolute bottom-3 inset-x-3 flex items-center justify-between">
            <CupertinoPill variant="blue" icon={<Sparkles className="w-3 h-3" />}>
              {stage.phaseTitleFa}
            </CupertinoPill>

            {isCompleted && (
              <div className="px-2.5 py-1 rounded-full bg-black/60 backdrop-blur-md border border-white/15">
                <CupertinoStarRating stars={stage.progress?.stars || 0} size="sm" />
              </div>
            )}
          </div>
        </div>

        {/* Character Bio */}
        <div className="flex items-center gap-3 p-3 rounded-2xl bg-white/5 border border-white/10">
          <div className="w-12 h-12 rounded-full overflow-hidden bg-apple-gray-700 shrink-0 border border-apple-blue/50">
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
              <div className="w-full h-full flex items-center justify-center text-white font-bold text-base">
                {stage.characterName.charAt(0)}
              </div>
            )}
          </div>

          <div className="space-y-0.5 min-w-0 flex-1">
            <span className="text-[11px] text-apple-gray-400 font-semibold">هم‌صحبت شما در این مرحله:</span>
            <h4 className="text-sm font-bold text-white font-mono">{stage.characterName}</h4>
            <span className="text-[11px] text-apple-gray-400 font-mono">
              جنسیت: {stage.characterGender === 'Woman' ? 'خانم' : 'آقا'} · حداکثر دور مکالمه: {stage.maxTurns || 12}
            </span>
          </div>
        </div>

        {/* Mission Briefing */}
        <div className="space-y-1.5">
          <h5 className="text-xs font-bold text-apple-gray-300">داستان و شرایط سناریو:</h5>
          <p className="text-xs text-apple-gray-300 leading-relaxed bg-white/[0.03] p-3 rounded-xl border border-white/5">
            {stage.briefingFa}
          </p>
        </div>

        {/* Target Objective */}
        {stage.targetObjectiveFa && (
          <div className="space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-bold text-apple-blue">
              <Target className="w-3.5 h-3.5" />
              <span>ماموریت اصلی مکالمه:</span>
            </div>
            <p className="text-xs text-white/90 leading-relaxed bg-apple-blue/10 p-3 rounded-xl border border-apple-blue/20">
              {stage.targetObjectiveFa}
            </p>
          </div>
        )}

        {/* Previous Best Score (if played) */}
        {isCompleted && stage.progress && (
          <div className="flex items-center justify-between p-3 rounded-xl bg-apple-yellow/10 border border-apple-yellow/20 text-xs">
            <div className="flex items-center gap-2 text-apple-yellow font-semibold">
              <Award className="w-4 h-4" />
              <span>بهترین امتیاز کسب شده:</span>
            </div>
            <span className="font-bold font-mono text-apple-yellow text-sm">
              {stage.progress.bestScore || 95} از ۱۰۰
            </span>
          </div>
        )}

        {/* Primary CTA */}
        <div className="pt-2">
          <CupertinoButton
            variant="filled"
            size="lg"
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
            onClick={handleAction}
          >
            {stage.lockStatus === 'LOCKED_REGISTRATION'
              ? 'ورود / ثبت‌نام رایگان برای شروع'
              : stage.lockStatus === 'LOCKED_SUBSCRIPTION'
              ? 'ارتقا به اشتراک ویژه'
              : stage.lockStatus === 'LOCKED_PREVIOUS_STAGE'
              ? 'تکمیل مرحله قبلی برای باز شدن'
              : isCompleted
              ? 'تکرار و تمرین مجدد'
              : 'ورود به اتاق مکالمه'}
          </CupertinoButton>
        </div>
      </div>
    </CupertinoModal>
  );
};
