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
  const progressPercent = Math.min(100, Math.round((turnsCount / currentMax) * 100));

  return (
    <div className="w-full px-4 pt-3 pb-2.5 space-y-2 select-none">
      {/* Row 1: Apple Navigation Bar (Start Anchor: Back, Center: Title, End: Finish Action) */}
      <div className="flex items-center justify-between gap-2.5">
        {/* Back Button (Start anchor in RTL: points right >) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onBackClick}
          className="w-10 h-10 rounded-2xl apple-glass-pill bg-white/10 hover:bg-white/15 border border-white/20 flex items-center justify-center text-white active:bg-white/20 transition-all shadow-apple-sm shrink-0"
          title="بازگشت به نقشه مراحل"
        >
          {/* Chevron pointing right (>) in RTL Persian */}
          <ChevronRight className="w-5 h-5 stroke-[2.5]" />
        </motion.button>

        {/* Center Liquid Glass Capsule */}
        <div className="flex-1 min-w-0 apple-glass-pill rounded-2xl py-1 px-3 text-center space-y-0.5 border border-white/25 shadow-apple-sm">
          <h2 className="text-xs sm:text-sm font-bold text-white truncate leading-tight">
            {stage?.titleFa || 'مکالمه هوشمند'}
          </h2>

          <div className="flex items-center justify-center gap-2">
            {stage?.characterName && (
              <span className="text-[11px] text-apple-gray-300 font-medium truncate" dir="ltr">
                {stage.characterName}
              </span>
            )}
            <span className="text-[9px] font-bold px-1.5 py-0.2 rounded-md bg-apple-blue/20 text-apple-blue border border-apple-blue/30">
              سطح {userLevel}
            </span>
          </div>
        </div>

        {/* Finish CTA Action Pill (Gold style when turnsCount > 0) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onFinishClick}
          disabled={turnsCount === 0 || isFinishing}
          className={`h-10 px-3.5 rounded-2xl text-xs font-bold flex items-center justify-center gap-1.5 transition-all shrink-0 ${
            turnsCount > 0
              ? 'bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange text-black font-extrabold shadow-[0_2px_14px_rgba(255,204,0,0.35)] hover:brightness-105 border border-yellow-200/50'
              : 'bg-white/10 text-apple-gray-400 border border-white/10 opacity-50 cursor-not-allowed'
          }`}
          title="پایان مکالمه و ثبت کارنامه"
        >
          {isFinishing ? (
            <RefreshCw className="w-3.5 h-3.5 animate-spin" />
          ) : (
            <Sparkles className="w-3.5 h-3.5 fill-current" />
          )}
          <span>{isFinishing ? 'بررسی...' : 'پایان'}</span>
        </motion.button>
      </div>

      {/* Row 2: Objective Trigger & Dynamic Progress Meter */}
      <div className="flex items-center gap-3">
        {/* Objective Chip (Apple Teal Glass) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onObjectiveClick}
          className="h-8 px-3 rounded-full apple-glass bg-apple-teal/15 border border-apple-teal/35 text-apple-teal text-[11px] font-bold flex items-center gap-1.5 hover:bg-apple-teal/20 transition-all shrink-0"
        >
          <Target className="w-3.5 h-3.5 text-apple-teal" />
          <span>هدف مرحله</span>
        </motion.button>

        {/* Progress Tracker HUD */}
        <div className="flex-1 space-y-1">
          <div className="flex items-center justify-between text-[10px] text-apple-gray-300">
            <span>
              پیشرفت: {turnsCount.toLocaleString('fa-IR')} از {currentMax.toLocaleString('fa-IR')} پیام
            </span>
            <span className="font-mono font-bold text-apple-teal">{progressPercent}%</span>
          </div>

          {/* Luminous Apple Liquid Progress Bar */}
          <div className="w-full h-1.5 bg-white/10 rounded-full overflow-hidden p-[1px] border border-white/10">
            <div
              className="h-full bg-gradient-to-r from-apple-blue via-apple-teal to-apple-green rounded-full transition-all duration-300 shadow-[0_0_8px_rgba(90,200,250,0.6)]"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
