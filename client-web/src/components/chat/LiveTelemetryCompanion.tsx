import React from 'react';
import { Activity, AlertCircle, CheckCircle2, Lightbulb, Sparkles } from 'lucide-react';
import { motion } from 'motion/react';
import { GrammarErrorItem } from '../../api/chatApi';

export interface LiveTelemetryCompanionProps {
  turnsCount: number;
  maxTurns?: number;
  hintsUsedCount: number;
  grammarErrors: GrammarErrorItem[];
  onRequestHint?: () => void;
}

export const LiveTelemetryCompanion: React.FC<LiveTelemetryCompanionProps> = ({
  turnsCount,
  maxTurns = 10,
  hintsUsedCount,
  grammarErrors,
  onRequestHint,
}) => {
  const progressPercent = Math.min(100, Math.round((turnsCount / maxTurns) * 100));

  return (
    <div className="w-80 xl:w-96 space-y-4 select-none text-right" dir="rtl">
      {/* 1. Live Telemetry & Progress Card */}
      <div className="relative rounded-[32px] p-5 apple-glass-card bg-[#141624]/85 border border-white/15 backdrop-blur-2xl shadow-apple-lg space-y-4">
        <div className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none rounded-t-[32px]" />

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-white font-bold text-xs">
            <Activity className="w-4 h-4 text-apple-blue" />
            <span>شاخص تعامل و پیشرفت</span>
          </div>

          <span className="font-mono text-xs font-bold text-apple-teal">
            {progressPercent}%
          </span>
        </div>

        {/* Dynamic Luminous Meter */}
        <div className="space-y-1.5">
          <div className="w-full h-2 bg-white/10 rounded-full overflow-hidden p-[1px] border border-white/10">
            <motion.div
              animate={{ width: `${progressPercent}%` }}
              transition={{ type: 'spring', stiffness: 300, damping: 25 }}
              className="h-full bg-gradient-to-r from-apple-blue via-apple-teal to-apple-green rounded-full shadow-[0_0_10px_rgba(90,200,250,0.7)]"
            />
          </div>

          <div className="flex items-center justify-between text-[11px] text-apple-gray-300">
            <span>
              نوبت‌ها: {turnsCount} از {maxTurns}
            </span>
            <span>
              راهنماهای استفاده شده: {hintsUsedCount}
            </span>
          </div>
        </div>

        {/* Quick Hint Action Pill */}
        {onRequestHint && (
          <motion.button
            type="button"
            whileTap={{ scale: 0.96 }}
            onClick={onRequestHint}
            className="w-full py-2.5 px-4 rounded-2xl bg-apple-yellow/15 hover:bg-apple-yellow/20 border border-apple-yellow/30 text-apple-yellow text-xs font-bold flex items-center justify-center gap-2 transition-all shadow-apple-sm"
          >
            <Lightbulb className="w-4 h-4 fill-apple-yellow/25" />
            <span>دریافت پیشنهاد جمله بعدی</span>
          </motion.button>
        )}
      </div>

      {/* 2. Real-time Grammar Feedback Hub */}
      <div className="relative rounded-[32px] p-5 apple-glass-card bg-[#141624]/85 border border-white/15 backdrop-blur-2xl shadow-apple-lg space-y-3.5">
        <div className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none rounded-t-[32px]" />

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-white font-bold text-xs">
            <AlertCircle className="w-4 h-4 text-apple-yellow" />
            <span>بازخورد لحظه‌ای گرامر</span>
          </div>

          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-white/10 text-white">
            {grammarErrors.length} مورد
          </span>
        </div>

        {grammarErrors.length === 0 ? (
          <div className="p-4 rounded-2xl bg-apple-green/10 border border-apple-green/20 text-center space-y-1">
            <CheckCircle2 className="w-5 h-5 text-apple-green mx-auto" />
            <p className="text-xs font-bold text-slate-100">گرامر بی‌نقص!</p>
            <p className="text-[10px] text-apple-gray-300">
              هنوز هیچ خطای ساختاری ثبت نشده است.
            </p>
          </div>
        ) : (
          <div className="space-y-2.5 max-h-56 overflow-y-auto pr-1">
            {grammarErrors.map((err, idx) => (
              <div
                key={idx}
                className="p-3 rounded-2xl bg-white/[0.04] border border-white/10 text-xs space-y-1.5"
              >
                <div className="text-[11px] text-apple-red font-mono line-through text-left opacity-80" dir="ltr">
                  {err.original}
                </div>
                <div className="text-[11px] text-apple-green font-mono font-bold text-left" dir="ltr">
                  {err.correction}
                </div>
                {err.explanationFa && (
                  <p className="text-[10px] text-slate-300 leading-relaxed font-sans">
                    {err.explanationFa}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 3. Audio & Apple Shortcuts Card */}
      <div className="relative rounded-[32px] p-4 apple-glass bg-[#141624]/75 border border-white/10 backdrop-blur-xl text-xs text-apple-gray-400 space-y-2">
        <div className="flex items-center gap-1.5 text-slate-200 font-bold text-[11px]">
          <Sparkles className="w-3.5 h-3.5 text-apple-teal" />
          <span>نکات مکالمه طبیعی:</span>
        </div>
        <p className="text-[11px] leading-relaxed text-slate-300">
          دکمه میکروفون را لمس کنید و شفاف به زبان انگلیسی صحبت کنید. در صورت نیاز به بازبینی، با دکمه کیبورد به متن تبدیل کنید.
        </p>
      </div>
    </div>
  );
};
