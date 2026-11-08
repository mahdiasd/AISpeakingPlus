import React from 'react';
import { Check, Sparkles, RotateCcw } from 'lucide-react';
import { motion } from 'motion/react';
import { clsx } from 'clsx';

export interface UserMessageItemProps {
  id: string;
  message: string;
  hasGrammarError?: boolean;
  correctedSentence?: string | null;
  grammarFeedbackFa?: string | null;
  isEvaluatingGrammar?: boolean;
  isSending?: boolean;
  onRetry?: () => void;
}

/**
 * Playful Curved Dash Line connecting the status badge to the GrammarTipCard below.
 * Exactly matches Compose CurvedDashLine cubic bezier curve.
 */
const CurvedDashLine: React.FC<{ color?: string; className?: string }> = ({
  color = '#f59e0b',
  className,
}) => {
  return (
    <svg
      width="24"
      height="32"
      viewBox="0 0 24 32"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={clsx('shrink-0 select-none pointer-events-none', className)}
    >
      <path
        d="M 23 0 C 23 18, 11 31, 0 31"
        stroke={color}
        strokeWidth="2"
        strokeDasharray="4 4"
        strokeLinecap="round"
      />
    </svg>
  );
};

/**
 * Champagne Gold Frosted Glass Grammar Tip Card.
 * Exactly matches Compose GrammarTipCard layout & Apple Liquid Glass aesthetic.
 */
const GrammarTipCard: React.FC<{
  feedback?: string | null;
  correctedSentence?: string | null;
}> = ({ feedback, correctedSentence }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 6, scale: 0.96 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ type: 'spring', stiffness: 420, damping: 26 }}
      className="apple-glass rounded-2xl p-3.5 space-y-2 border border-amber-400/40 bg-[#1e1c1a]/85 backdrop-blur-xl shadow-apple-md max-w-[290px] text-right relative overflow-hidden"
      dir="rtl"
    >
      {/* Specular Rim Light */}
      <div
        aria-hidden="true"
        className="absolute inset-x-0 top-0 h-[1.5px] bg-gradient-to-r from-transparent via-amber-300/40 to-transparent pointer-events-none rounded-t-2xl"
      />

      {/* Header */}
      <div className="flex items-center gap-1.5 text-amber-400 font-bold text-xs select-none">
        <Sparkles className="w-3.5 h-3.5 text-amber-400 shrink-0" />
        <span>نکته گرامری</span>
      </div>

      {/* Corrected English Sentence in LTR */}
      {correctedSentence && (
        <div
          className="text-left font-mono font-bold text-xs text-white bg-white/10 px-3 py-1.5 rounded-xl border border-white/15 tracking-wide select-text"
          dir="ltr"
        >
          {correctedSentence}
        </div>
      )}

      {/* Persian Feedback in RTL */}
      {feedback ? (
        <p className="text-[12px] text-slate-200 leading-relaxed text-justify font-sans">
          {feedback}
        </p>
      ) : (
        !correctedSentence && (
          <p className="text-[12px] text-slate-200 leading-relaxed font-sans">
            جمله شما از نظر گرامری نیاز به اصلاح دارد.
          </p>
        )
      )}
    </motion.div>
  );
};

/**
 * 2026 Apple Liquid Glass User Chat Message Item.
 *
 * Characteristics:
 * - Right-aligned in RTL layout.
 * - Bubble shape: rounded-[20px] rounded-tr-[6px].
 * - Translucent Royal Azure & Electric Indigo glass gradient with specular light rim.
 * - Protruding bottom-right status badge (50% in, 50% out):
 *   - Evaluating: micro spinner + "بررسی گرامر..."
 *   - Correct: Serene Cyan pill + checkmark + "گرامر درست بود" (strictly NO green)
 *   - Error: Champagne Gold pill + "نکته گرامری"
 * - Playful CurvedDashLine connecting badge to GrammarTipCard.
 */
export const UserMessageItem: React.FC<UserMessageItemProps> = ({
  message,
  hasGrammarError = false,
  correctedSentence,
  grammarFeedbackFa,
  isEvaluatingGrammar = false,
  isSending = false,
  onRetry,
}) => {
  const isEvaluating = isSending || isEvaluatingGrammar;

  return (
    <div className="flex flex-col items-end my-3 ml-auto self-end max-w-[88%] sm:max-w-[80%]" dir="ltr">
      {/* 1. User Message Bubble Container with clearance for the bottom-right badge */}
      <div className="relative pb-3.5">
        {/* Royal Azure & Indigo Liquid Glass Bubble */}
        <div className="relative rounded-[20px] rounded-tr-[6px] px-4 py-3 bg-gradient-to-br from-[#007aff]/60 via-[#1450b4]/60 to-[#5856d6]/55 backdrop-blur-[14px] saturate-[180%] shadow-[0_8px_28px_-4px_rgba(0,122,255,0.35)] border border-white/25 overflow-hidden">
          {/* Specular Top Rim Light */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 top-0 h-[1.5px] bg-gradient-to-r from-white/70 via-white/30 to-transparent pointer-events-none"
          />

          <p className="text-[15px] sm:text-[16px] leading-[1.65] font-normal tracking-wide text-white select-text text-left">
            {message}
          </p>
        </div>

        {/* 2. Apple Liquid Glass Grammar Status Badge (anchored to bottom-right 50% in, 50% out) */}
        <div className="absolute -bottom-2 right-2.5 z-10 select-none" dir="rtl">
          {isEvaluating ? (
            /* Evaluating: Micro-spinner arc + "بررسی گرامر..." */
            <div className="h-6 px-2.5 rounded-full apple-glass-pill bg-[#151928]/95 border border-cyan-400/40 flex items-center gap-1.5 text-[10.5px] text-white shadow-apple-sm">
              <span className="w-2.5 h-2.5 rounded-full border-[1.5px] border-cyan-400 border-t-transparent animate-spin" />
              <span>بررسی گرامر...</span>
            </div>
          ) : hasGrammarError ? (
            /* Grammar Error: Champagne Gold pill */
            <div className="h-6 px-2.5 rounded-full apple-glass-gold flex items-center gap-1.5 text-[11px] font-bold text-amber-300 border border-amber-400/50 shadow-apple-sm">
              <Sparkles className="w-3 h-3 text-amber-400 shrink-0" />
              <span>نکته گرامری</span>
            </div>
          ) : (
            /* Grammar Correct: Serene Cyan pill (Strictly NO green as per Compose spec) */
            <div className="h-6 px-2.5 rounded-full apple-glass-pill bg-[#0f1826]/90 border border-cyan-400/50 flex items-center gap-1 text-[11px] font-bold text-cyan-300 shadow-apple-sm">
              <Check className="w-3 h-3 text-cyan-400 stroke-[2.5]" />
              <span>گرامر درست بود</span>
            </div>
          )}
        </div>
      </div>

      {/* 3. If grammar error, connect badge to GrammarTipCard via CurvedDashLine */}
      {hasGrammarError && !isEvaluating && (
        <div className="flex items-start justify-end gap-0 pt-0.5" dir="ltr">
          <GrammarTipCard
            feedback={grammarFeedbackFa}
            correctedSentence={correctedSentence}
          />
          <CurvedDashLine className="mr-3 shrink-0" color="#f59e0b" />
        </div>
      )}

      {/* 4. Retry HUD for failed messages */}
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-2 h-7 px-3 rounded-full apple-glass-pill text-rose-300 border border-rose-400/40 text-[11px] font-medium flex items-center gap-1.5 active:scale-95 transition-all select-none"
          dir="rtl"
        >
          <RotateCcw className="w-3 h-3 text-rose-400" />
          <span>ارسال نشد؛ دوباره تلاش کن</span>
        </button>
      )}
    </div>
  );
};
