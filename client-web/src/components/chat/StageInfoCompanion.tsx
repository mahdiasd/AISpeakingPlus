import React from 'react';
import { Target, Compass, Volume2 } from 'lucide-react';
import { motion } from 'motion/react';
import { Stage } from '../../types/stage';

export interface StageInfoCompanionProps {
  stage: Stage | null;
  userLevel?: string;
  isObjectiveCompleted?: boolean;
  onOpenObjectiveModal: () => void;
  onPlayBriefingAudio?: () => void;
}

export const StageInfoCompanion: React.FC<StageInfoCompanionProps> = ({
  stage,
  userLevel = 'A1',
  isObjectiveCompleted = false,
  onOpenObjectiveModal,
  onPlayBriefingAudio,
}) => {
  if (!stage) return null;

  return (
    <div className="w-80 xl:w-96 space-y-4 select-none text-right" dir="rtl">
      {/* 1. Stage Story Briefing Card */}
      <div className="relative rounded-[32px] p-5 apple-glass-card bg-[#141624]/85 border border-white/15 backdrop-blur-2xl shadow-apple-lg space-y-4">
        {/* Specular Edge */}
        <div className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none rounded-t-[32px]" />

        {/* Level & Stage Index Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-1 rounded-full bg-apple-blue/20 text-apple-blue border border-apple-blue/30 text-[11px] font-bold">
              مرحله {stage.orderIndex}
            </span>
            <span className="px-2.5 py-1 rounded-full bg-white/10 text-white border border-white/15 text-[11px] font-bold">
              سطح {userLevel}
            </span>
          </div>

          <span className="text-[11px] text-apple-gray-400 font-medium flex items-center gap-1">
            <Compass className="w-3.5 h-3.5 text-apple-teal" />
            <span>سناریوی تعاملی</span>
          </span>
        </div>

        {/* Title */}
        <div className="space-y-1">
          <h2 className="text-lg font-bold text-white tracking-tight leading-snug">
            {stage.titleFa}
          </h2>
          <p className="text-xs font-medium text-apple-gray-300" dir="ltr">
            {stage.title}
          </p>
        </div>

        {/* Briefing Narrative */}
        {stage.briefingFa && (
          <div className="p-3.5 rounded-2xl bg-white/[0.04] border border-white/10 text-xs text-slate-300 leading-relaxed space-y-2">
            <p>{stage.briefingFa}</p>
            {onPlayBriefingAudio && (
              <button
                type="button"
                onClick={onPlayBriefingAudio}
                className="inline-flex items-center gap-1.5 text-[11px] text-apple-teal font-medium hover:underline pt-1"
              >
                <Volume2 className="w-3.5 h-3.5" />
                <span>شنیدن تلفظ سناریو</span>
              </button>
            )}
          </div>
        )}
      </div>

      {/* 2. Character Dossier Card */}
      <div className="relative rounded-[32px] p-5 apple-glass-card bg-[#141624]/85 border border-white/15 backdrop-blur-2xl shadow-apple-lg space-y-3.5">
        <div className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none rounded-t-[32px]" />

        <div className="text-[11px] font-bold text-apple-gray-400 uppercase tracking-wider">
          شخصیت داستان و هم‌صحبت شما
        </div>

        <div className="flex items-center gap-3.5">
          {/* Avatar with Apple Specular Rim */}
          <div className="relative w-14 h-14 rounded-2xl overflow-hidden border border-white/20 shadow-apple-md bg-gradient-to-br from-[#2c2d3e] to-[#141624] shrink-0">
            {stage.characterAvatarUrl ? (
              <img
                src={stage.characterAvatarUrl}
                alt={stage.characterName}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-apple-blue font-bold text-lg">
                {stage.characterName.slice(0, 2).toUpperCase()}
              </div>
            )}
            <div className="absolute inset-0 rounded-2xl border border-white/20 pointer-events-none" />
          </div>

          <div className="space-y-1 min-w-0">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-bold text-white truncate" dir="ltr">
                {stage.characterName}
              </h3>
            </div>
            <p className="text-xs text-apple-gray-300 truncate">
              مکالمه طبیعی و ارزیابی زنده لهجه
            </p>
          </div>
        </div>
      </div>

      {/* 3. Mission Objective Target Card */}
      <div className="relative rounded-[32px] p-5 apple-glass-card bg-[#141624]/85 border border-white/15 backdrop-blur-2xl shadow-apple-lg space-y-3">
        <div className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none rounded-t-[32px]" />

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-apple-teal font-bold text-xs">
            <Target className="w-4 h-4 text-apple-teal" />
            <span>هدف ماموریت مکالمه</span>
          </div>

          <span
            className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
              isObjectiveCompleted
                ? 'bg-apple-green/20 text-apple-green border-apple-green/30'
                : 'bg-apple-yellow/20 text-apple-yellow border-apple-yellow/30'
            }`}
          >
            {isObjectiveCompleted ? 'تکمیل شد' : 'در حال انجام'}
          </span>
        </div>

        <p className="text-xs text-slate-200 leading-relaxed">
          {stage.targetObjectiveFa || stage.targetObjective || 'تکمیل مکالمه با رعایت نکات گرامری'}
        </p>

        <motion.button
          type="button"
          whileTap={{ scale: 0.96 }}
          onClick={onOpenObjectiveModal}
          className="w-full py-2 rounded-xl bg-white/5 hover:bg-white/10 text-apple-teal text-xs font-bold transition-all border border-white/10 text-center"
        >
          مشاهده کامل ماموریت
        </motion.button>
      </div>
    </div>
  );
};
