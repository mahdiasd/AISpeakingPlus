import React from 'react';
import { ChevronRight, Target, Sparkles, RefreshCw } from 'lucide-react';
import { motion } from 'motion/react';
import { Stage } from '../../types/stage';

export interface ChatToolbarProps {
  stage: Stage | null;
  turnsCount: number;
  maxTurns?: number;
  userLevel?: string;
  isFinishing?: boolean;
  onBackClick: () => void;
  onObjectiveClick: () => void;
  onFinishClick: () => void;
}

/**
 * 2026 Apple Liquid Glass HUD Toolbar for conversation.
 * 100% matching Compose Multiplatform `ChatToolbar.kt` in layout and aesthetics.
 *
 * Structure in RTL:
 * - Row 1: Back Navigation Button (Right, pointing >) | Floating Glass Stage & Character Capsule (Center) | Finish Action CTA (Left)
 * - Row 2: Objective Glass Chip (Right, Serene Cyan glow) | Luminous Progress Bar & Counter (Left, Cyan/Azure gradient)
 */
export const ChatToolbar: React.FC<ChatToolbarProps> = ({
  stage,
  turnsCount,
  maxTurns = 10,
  userLevel = 'A1',
  isFinishing = false,
  onBackClick,
  onObjectiveClick,
  onFinishClick,
}) => {
  const currentMax = stage?.maxTurns || maxTurns;
  const clampedTurns = Math.min(turnsCount, currentMax);
  const progressPercent = Math.min(100, Math.round((clampedTurns / currentMax) * 100));

  return (
    <div className="w-full px-3.5 pt-2.5 pb-2 space-y-2 select-none" dir="rtl">
      {/* Row 1: Controls & Stage Identifier (RTL: Right: Back, Center: Stage Capsule, Left: Finish) */}
      <div className="flex items-center justify-between gap-2.5">
        {/* Right (Start anchor in RTL): Back Button with arrow pointing right ('>' / ic_back) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onBackClick}
          aria-label="بازگشت"
          className="w-[42px] h-[42px] rounded-[18px] apple-glass bg-white/10 hover:bg-white/15 border border-white/20 flex items-center justify-center text-white active:bg-white/20 transition-all shadow-apple-sm shrink-0"
          title="بازگشت به نقشه مراحل"
        >
          {/* Chevron pointing right (>) in RTL Persian according to project constitution */}
          <ChevronRight className="w-5 h-5 stroke-[2.5]" />
        </motion.button>

        {/* Center: Liquid Glass Stage Info Capsule */}
        <div className="flex-1 min-w-0 apple-glass rounded-[22px] py-1.5 px-3.5 text-center flex flex-col items-center justify-center border border-white/25 shadow-apple-sm backdrop-blur-[14px]">
          <h2 className="text-xs sm:text-[13px] font-bold text-white truncate leading-tight w-full max-w-[200px] sm:max-w-xs">
            {stage?.titleFa || 'مکالمه انگلیسی'}
          </h2>

          <div className="flex items-center justify-center gap-1.5 mt-0.5">
            {stage?.characterName && (
              <span className="text-[11px] text-apple-gray-300 font-medium truncate max-w-[130px]" dir="ltr">
                {stage.characterName}
              </span>
            )}
            {userLevel && (
              <span className="text-[9px] font-bold px-1.5 py-0.5 rounded-[6px] bg-[#6366F1]/20 text-[#A5A3F6] border border-[#6366F1]/40 leading-none">
                سطح {userLevel}
              </span>
            )}
          </div>
        </div>

        {/* Left (End anchor in RTL): Finish Conversation Button (Tactile Apple Pill) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onFinishClick}
          disabled={turnsCount === 0 || isFinishing}
          aria-label={isFinishing ? 'در حال بررسی...' : 'پایان مکالمه'}
          className={`h-[42px] px-3.5 rounded-[18px] text-xs font-bold flex items-center justify-center gap-1.5 transition-all shrink-0 ${
            isFinishing ? 'w-[110px]' : 'w-[84px]'
          } ${
            turnsCount > 0 && !isFinishing
              ? 'bg-gradient-to-r from-amber-400 via-yellow-400 to-amber-500 text-slate-950 font-extrabold shadow-[0_2px_14px_rgba(251,191,36,0.35)] hover:brightness-105 border border-yellow-200/60'
              : isFinishing
              ? 'bg-amber-400/20 text-amber-300 border border-amber-400/30 cursor-wait'
              : 'bg-white/10 text-apple-gray-400 border border-white/10 opacity-50 cursor-not-allowed'
          }`}
          title="پایان مکالمه و دریافت کارنامه"
        >
          {isFinishing ? (
            <RefreshCw className="w-3.5 h-3.5 animate-spin" />
          ) : (
            <Sparkles className="w-3.5 h-3.5 fill-current" />
          )}
          <span>{isFinishing ? 'در حال بررسی' : 'پایان'}</span>
        </motion.button>
      </div>

      {/* Row 2: Objective Trigger & Dynamic Progress Meter */}
      <div className="flex items-center gap-2.5">
        {/* Right (Start anchor in RTL): Objective Chip (Serene Cyan glow) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onObjectiveClick}
          className="h-8 px-3 rounded-full apple-glass bg-[#06B6D4]/15 border border-[#06B6D4]/45 text-[#38BDF8] text-[11px] font-bold flex items-center gap-1.5 hover:bg-[#06B6D4]/25 transition-all shrink-0 shadow-[0_0_12px_rgba(6,182,212,0.18)]"
          title="مشاهده اهداف و داستان این مرحله"
        >
          <Target className="w-3.5 h-3.5 text-[#38BDF8]" />
          <span>هدف مرحله</span>
        </motion.button>

        {/* Left (End anchor in RTL): Dynamic Progress Tracker HUD */}
        <div className="flex-1 min-w-0 space-y-1">
          <div className="flex items-center justify-between text-[10px] text-apple-gray-300">
            <span className="truncate">
              پیشرفت گفتگو: {clampedTurns.toLocaleString('fa-IR')} از {currentMax.toLocaleString('fa-IR')} پیام
            </span>
            <span className="font-mono font-bold text-[#38BDF8] mr-1">
              {progressPercent.toLocaleString('fa-IR')}%
            </span>
          </div>

          {/* Apple Luminous Liquid Progress Bar (Cyan to Azure gradient) */}
          <div className="w-full h-[6px] bg-white/15 rounded-full overflow-hidden p-[0.5px] border border-white/10">
            <div
              className="h-full bg-gradient-to-l from-[#06B6D4] to-[#007AFF] rounded-full transition-all duration-300 shadow-[0_0_8px_rgba(6,182,212,0.6)]"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
