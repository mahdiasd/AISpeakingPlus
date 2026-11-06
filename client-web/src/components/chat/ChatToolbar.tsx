import React from 'react';
import { ChevronRight, Target, Sparkles, RefreshCw } from 'lucide-react';
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
    <div className="w-full max-w-lg mx-auto px-3.5 pt-3 pb-2 space-y-2.5 z-30 select-none">
      {/* Row 1: Controls & Stage Identifier */}
      <div className="flex items-center justify-between gap-2.5">
        {/* Back Button (Start anchor in RTL: points right >) */}
        <button
          type="button"
          onClick={onBackClick}
          className="w-10 h-10 rounded-full apple-glass bg-white/10 hover:bg-white/15 border border-white/20 flex items-center justify-center text-white active:scale-95 transition-all shadow-apple-sm shrink-0"
          title="بازگشت"
        >
          <ChevronRight className="w-5 h-5" />
        </button>

        {/* Center Liquid Glass Stage & Character Info Capsule */}
        <div className="flex-1 min-w-0 apple-glass bg-[#1b1d2c]/85 border border-white/20 rounded-2xl py-1.5 px-3 text-center space-y-0.5 shadow-apple-sm backdrop-blur-2xl">
          <h2 className="text-xs font-bold text-white truncate leading-tight">
            {stage?.titleFa || 'مکالمه هوشمند'}
          </h2>

          <div className="flex items-center justify-center gap-2">
            {stage?.characterName && (
              <span className="text-[11px] text-apple-gray-300 font-medium truncate" dir="ltr">
                {stage.characterName}
              </span>
            )}
            <span className="text-[9px] font-bold px-1.5 py-0.5 rounded-md bg-apple-blue/20 text-apple-blue border border-apple-blue/30">
              سطح {userLevel}
            </span>
          </div>
        </div>

        {/* Finish Conversation CTA Pill (Gold style, active when turnsCount > 0) */}
        <button
          type="button"
          onClick={onFinishClick}
          disabled={turnsCount === 0 || isFinishing}
          className={`h-10 px-3.5 rounded-full text-xs font-bold flex items-center justify-center gap-1.5 transition-all active:scale-95 shrink-0 ${
            turnsCount > 0
              ? 'bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange text-black font-extrabold shadow-[0_2px_12px_rgba(255,204,0,0.3)]'
              : 'bg-white/10 text-apple-gray-400 border border-white/10 opacity-60 cursor-not-allowed'
          }`}
          title="پایان مکالمه و ثبت کارنامه"
        >
          {isFinishing ? (
            <RefreshCw className="w-3.5 h-3.5 animate-spin" />
          ) : (
            <Sparkles className="w-3.5 h-3.5 fill-current" />
          )}
          <span>{isFinishing ? 'بررسی...' : 'پایان'}</span>
        </button>
      </div>

      {/* Row 2: Objective Trigger & Dynamic Progress Meter */}
      <div className="flex items-center gap-3 px-0.5">
        {/* Objective Chip (Serene Cyan glow) */}
        <button
          type="button"
          onClick={onObjectiveClick}
          className="h-8 px-3 rounded-full apple-glass bg-apple-teal/15 border border-apple-teal/35 text-apple-teal text-[11px] font-bold flex items-center gap-1.5 active:scale-95 hover:bg-apple-teal/20 transition-all shrink-0"
        >
          <Target className="w-3.5 h-3.5 text-apple-teal" />
          <span>هدف مرحله</span>
        </button>

        {/* Progress Tracker HUD */}
        <div className="flex-1 space-y-1">
          <div className="flex items-center justify-between text-[10px] text-apple-gray-300">
            <span>
              پیشرفت گفتگو: {turnsCount.toLocaleString('fa-IR')} از {currentMax.toLocaleString('fa-IR')} پیام
            </span>
            <span className="font-mono font-bold text-apple-teal">{progressPercent}%</span>
          </div>

          {/* Luminous Apple Liquid Progress Bar */}
          <div className="w-full h-1.5 bg-white/10 rounded-full overflow-hidden p-0.5 border border-white/10">
            <div
              className="h-full bg-gradient-to-r from-apple-blue via-apple-teal to-emerald-400 rounded-full transition-all duration-300 shadow-[0_0_8px_rgba(52,199,89,0.5)]"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
