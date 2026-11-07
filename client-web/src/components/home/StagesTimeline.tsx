import React from 'react';
import { motion } from 'motion/react';
import {
  Play,
  Lock,
  Crown,
  UserCheck,
  Star,
  CheckCircle2,
  ChevronLeft,
} from 'lucide-react';
import { clsx } from 'clsx';
import { PhaseGroup, Stage } from '../../types/stage';
import { CupertinoStarRating, CupertinoPill } from '../cupertino';

export interface StagesTimelineProps {
  phaseGroups: PhaseGroup[];
  currentActiveStageId: string | null;
  onSelectStage: (stage: Stage) => void;
  className?: string;
}

export const StagesTimeline: React.FC<StagesTimelineProps> = ({
  phaseGroups,
  currentActiveStageId,
  onSelectStage,
  className,
}) => {
  return (
    <div className={clsx('space-y-6', className)}>
      {phaseGroups.map((group) => {
        const isPhaseFinished = group.isCompleted;

        return (
          <section key={group.phaseNumber} className="space-y-3">
            {/* Phase Section Header Card */}
            <div className="relative rounded-2xl p-3.5 bg-white/5 border border-white/10 backdrop-blur-md select-none">
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2.5">
                  <div
                    className={clsx(
                      'w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shadow-apple-sm select-none',
                      isPhaseFinished
                        ? 'bg-gradient-to-br from-apple-yellow to-apple-orange text-black'
                        : 'bg-gradient-to-br from-apple-blue to-apple-indigo text-white'
                    )}
                  >
                    {isPhaseFinished ? <CheckCircle2 className="w-4 h-4" /> : group.phaseNumber}
                  </div>

                  <div>
                    <h3 className="text-sm font-bold text-white">{group.phaseTitleFa}</h3>
                    <span className="text-[11px] text-apple-gray-400 font-mono">
                      {group.completedStages} از {group.totalStages} مرحله تکمیل شده
                    </span>
                  </div>
                </div>

                {isPhaseFinished ? (
                  <CupertinoPill variant="gold" icon={<Star className="w-3 h-3 fill-apple-yellow" />}>
                    تکمیل شده
                  </CupertinoPill>
                ) : (
                  <div className="w-20 h-1.5 rounded-full bg-white/10 overflow-hidden">
                    <div
                      className="h-full bg-apple-blue rounded-full transition-all duration-300"
                      style={{ width: `${group.progressPercentage * 100}%` }}
                    />
                  </div>
                )}
              </div>
            </div>

            {/* Stages Track & Cards */}
            <div className="relative pl-1 pr-1 space-y-2.5">
              {group.stages.map((stage, idx) => {
                const isActive = stage.id === currentActiveStageId;
                const isCompleted = (stage.progress?.stars || 0) > 0;
                const isLocked = stage.lockStatus !== 'UNLOCKED';
                const isLastInPhase = idx === group.stages.length - 1;

                return (
                  <div key={stage.id} className="relative flex items-stretch gap-3">
                    {/* Vertical Timeline Track Column */}
                    <div className="relative flex flex-col items-center w-8 shrink-0 select-none">
                      {/* Connecting Line */}
                      {!isLastInPhase && (
                        <div
                          className={clsx(
                            'absolute top-8 bottom-0 w-0.5 z-0',
                            isCompleted ? 'bg-apple-yellow/50' : 'bg-white/10'
                          )}
                        />
                      )}

                      {/* Timeline Node Circle */}
                      <div className="relative z-10 mt-3">
                        {isActive && (
                          <motion.div
                            animate={{ scale: [1, 1.35, 1], opacity: [0.6, 0.15, 0.6] }}
                            transition={{ repeat: Infinity, duration: 1.8, ease: 'easeInOut' }}
                            className="absolute -inset-1.5 rounded-full bg-apple-blue pointer-events-none"
                          />
                        )}

                        <div
                          className={clsx(
                            'w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs transition-transform shadow-apple-sm',
                            isActive
                              ? 'bg-apple-blue text-white ring-2 ring-white/80'
                              : isCompleted
                              ? 'bg-gradient-to-br from-apple-yellow to-apple-orange text-black'
                              : isLocked
                              ? 'bg-apple-gray-800 text-apple-gray-400 border border-white/10'
                              : 'bg-white/15 text-white border border-white/20'
                          )}
                        >
                          {isActive ? (
                            <Play className="w-3.5 h-3.5 fill-white" />
                          ) : isCompleted ? (
                            <Star className="w-3.5 h-3.5 fill-black text-black" />
                          ) : stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                            <Crown className="w-3 h-3 text-apple-yellow" />
                          ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                            <UserCheck className="w-3 h-3 text-apple-gray-300" />
                          ) : isLocked ? (
                            <Lock className="w-3 h-3 text-apple-gray-400" />
                          ) : (
                            stage.orderIndex
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Stage Card */}
                    <motion.div
                      whileTap={{ scale: 0.98 }}
                      transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                      onClick={() => onSelectStage(stage)}
                      className={clsx(
                        'flex-1 relative rounded-2xl p-3.5 transition-all duration-200 cursor-pointer select-none touch-manipulation',
                        isActive
                          ? 'apple-glass-blue border border-apple-blue/60 shadow-[0_0_24px_rgba(0,122,255,0.35)]'
                          : isCompleted
                          ? 'apple-glass border border-apple-yellow/30 shadow-apple-sm hover:border-apple-yellow/50'
                          : isLocked
                          ? 'apple-glass-pill opacity-60 border border-white/10 hover:opacity-80'
                          : 'apple-glass-pill border border-white/20 hover:border-white/30 shadow-apple-sm'
                      )}
                    >
                      {/* Top Row: Character name + status badges */}
                      <div className="flex items-center justify-between gap-2 pb-1.5">
                        <div className="flex items-center gap-2">
                          {/* Character Avatar */}
                          <div className="w-5 h-5 rounded-full overflow-hidden bg-apple-gray-700 flex items-center justify-center shrink-0">
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
                              <span className="text-[10px] font-bold text-white uppercase">
                                {stage.characterName.charAt(0)}
                              </span>
                            )}
                          </div>
                          <span className="text-[11px] font-semibold text-apple-gray-400 font-mono truncate max-w-[190px]">
                            {stage.characterName}
                          </span>
                        </div>

                        {isActive ? (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-apple-blue/20 text-apple-blue border border-apple-blue/30">
                            مرحله فعال شما
                          </span>
                        ) : isCompleted ? (
                          <CupertinoStarRating stars={stage.progress?.stars || 0} size="sm" />
                        ) : stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                          <span className="inline-flex items-center gap-1 text-[10px] text-apple-yellow font-bold">
                            <Crown className="w-3 h-3" />
                            <span>اشتراک ویژه</span>
                          </span>
                        ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                          <span className="inline-flex items-center gap-1 text-[10px] text-apple-gray-400 font-medium">
                            <UserCheck className="w-3 h-3" />
                            <span>نیاز به ثبت‌نام</span>
                          </span>
                        ) : null}
                      </div>

                      {/* Middle: Thumbnail + Title + Briefing */}
                      <div className="flex items-center gap-3">
                        {/* Thumbnail Artwork Preview */}
                        <div className="relative w-14 h-14 rounded-xl overflow-hidden bg-apple-gray-800 shrink-0 border border-white/10">
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
                            <div className="w-full h-full flex items-center justify-center text-apple-gray-500 text-xs">
                              {stage.orderIndex}
                            </div>
                          )}

                          {isLocked && (
                            <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
                              {stage.lockStatus === 'LOCKED_SUBSCRIPTION' ? (
                                <Crown className="w-4 h-4 text-apple-yellow" />
                              ) : stage.lockStatus === 'LOCKED_REGISTRATION' ? (
                                <UserCheck className="w-4 h-4 text-white" />
                              ) : (
                                <Lock className="w-3.5 h-3.5 text-white/80" />
                              )}
                            </div>
                          )}
                        </div>

                        {/* Title & Briefing text */}
                        <div className="flex-1 min-w-0 space-y-0.5">
                          <h4 className="text-sm font-bold text-white tracking-tight truncate">
                            {stage.titleFa}
                          </h4>
                          <span className="text-[11px] text-apple-gray-400 font-mono italic truncate block">
                            {stage.title}
                          </span>
                          <p className="text-[11px] text-apple-gray-300 line-clamp-1">
                            {stage.briefingFa}
                          </p>
                        </div>

                        {/* Right arrow in RTL points Left (towards next view) */}
                        <ChevronLeft className="w-4 h-4 text-apple-gray-500 shrink-0 self-center" />
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
