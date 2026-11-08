import React from 'react';
import { ChevronRight, Lightbulb, RefreshCw } from 'lucide-react';
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
 * 100% matching Compose Multiplatform `ChatToolbar.kt`:
 * - Row 1: Back Navigation Button (Right, pointing >) | Floating Glass Stage & Character Capsule (Center) | Finish Action CTA (Left)
 * - Row 2: Objective Glass Chip with Lamp icon (Right, Serene Cyan glow) | Luminous Progress Bar & Counter (Left)
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
    <div className="w-full px-3 pt-2 pb-1.5 space-y-1.5 select-none" dir="rtl">
      {/* Row 1: Controls & Stage Identifier (RTL: Right: Back, Center: Stage Capsule, Left: Finish) */}
      <div className="flex items-center justify-between gap-2">
        {/* Right (Start anchor in RTL): Back Button with arrow pointing right ('>' / ic_back) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onBackClick}
          aria-label="بازگشت"
          className="w-9 h-9 rounded-full bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/20 backdrop-blur-md flex items-center justify-center text-white transition-all shadow-sm shrink-0"
          title="بازگشت به نقشه مراحل"
        >
          <ChevronRight className="w-5 h-5 stroke-[2.5]" />
        </motion.button>

        {/* Center: Liquid Glass Stage Info Capsule */}
        <div className="flex-1 min-w-0 h-9 px-3 rounded-full bg-[#121526]/60 backdrop-blur-md border border-white/20 shadow-sm flex items-center justify-center overflow-hidden">
          <div className="flex flex-col items-center justify-center leading-tight">
            <h2 className="text-[12.5px] font-bold text-white truncate max-w-[180px] sm:max-w-xs">
              {stage?.titleFa || 'مکالمه انگلیسی'}
            </h2>

            <div className="flex items-center justify-center gap-1.5 mt-0.5">
              {stage?.characterName && (
                <span className="text-[10px] text-slate-300 font-medium truncate max-w-[120px]" dir="ltr">
                  {stage.characterName}
                </span>
              )}
              {userLevel && (
                <span className="text-[8.5px] font-bold px-1 rounded bg-[#5e5ce6]/25 text-[#a5a3f6] border border-[#5e5ce6]/40 leading-none">
                  سطح {userLevel}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Left (End anchor in RTL): Finish Conversation Button (Yellow Gold Pill matching Compose) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onFinishClick}
          disabled={turnsCount === 0 || isFinishing}
          aria-label={isFinishing ? 'در حال بررسی...' : 'پایان مکالمه'}
          className={`h-8 px-3.5 rounded-[14px] text-xs font-black flex items-center justify-center transition-all shrink-0 ${
            turnsCount > 0 && !isFinishing
              ? 'bg-[#FFD60A] text-slate-950 shadow-[0_2px_12px_rgba(255,214,10,0.4)] hover:brightness-105 border border-yellow-200/60'
              : isFinishing
              ? 'bg-amber-400/20 text-amber-300 border border-amber-400/30 cursor-wait'
              : 'bg-white/10 text-apple-gray-400 border border-white/10 opacity-50 cursor-not-allowed'
          }`}
          title="پایان مکالمه و دریافت کارنامه"
        >
          {isFinishing ? (
            <div className="flex items-center gap-1">
              <RefreshCw className="w-3 h-3 animate-spin" />
              <span>در حال بررسی</span>
            </div>
          ) : (
            <span>پایان</span>
          )}
        </motion.button>
      </div>

      {/* Row 2: Objective Trigger & Dynamic Progress Meter */}
      <div className="flex items-center gap-2">
        {/* Right (Start anchor in RTL): Objective Chip (Lamp Icon with Serene Cyan glow matching Compose ic_lamp) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onObjectiveClick}
          className="h-7 px-2.5 rounded-full bg-[#06B6D4]/15 border border-[#06B6D4]/45 text-[#38BDF8] text-[11px] font-bold flex items-center gap-1 hover:bg-[#06B6D4]/25 transition-all shrink-0 shadow-[0_0_10px_rgba(6,182,212,0.15)]"
          title="مشاهده اهداف و داستان این مرحله"
        >
          <Lightbulb className="w-3 h-3 text-[#38BDF8]" />
          <span>هدف مرحله</span>
        </motion.button>

        {/* Left (End anchor in RTL): Dynamic Progress Tracker HUD */}
        <div className="flex-1 min-w-0 space-y-1">
          <div className="flex items-center justify-between text-[10px] text-slate-300">
            <span className="truncate">
              پیشرفت گفتگو: {clampedTurns.toLocaleString('fa-IR')} از {currentMax.toLocaleString('fa-IR')} پیام
            </span>
            <span className="font-mono font-bold text-[#38BDF8] mr-1">
              {progressPercent.toLocaleString('fa-IR')}%
            </span>
          </div>

          {/* Apple Luminous Liquid Progress Bar (Cyan to Azure gradient) */}
          <div className="w-full h-[5px] bg-white/15 rounded-full overflow-hidden p-[0.5px] border border-white/10">
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
