import React from 'react';
import { Check, Sparkles } from 'lucide-react';
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
      width="20"
      height="26"
      viewBox="0 0 20 26"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={clsx('shrink-0 select-none pointer-events-none', className)}
    >
      <path
        d="M 19 0 C 19 14, 8 25, 0 25"
        stroke={color}
        strokeWidth="1.5"
        strokeDasharray="3 3"
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
      initial={{ opacity: 0, y: 4, scale: 0.97 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ type: 'spring', stiffness: 420, damping: 26 }}
      className="rounded-2xl p-3 space-y-1.5 border border-amber-400/40 bg-gradient-to-b from-[#221e1a]/80 to-[#181614]/85 backdrop-blur-md shadow-apple-sm max-w-[280px] text-right relative overflow-hidden"
      dir="rtl"
    >
      {/* Specular Rim Light */}
      <div
        aria-hidden="true"
        className="absolute inset-x-0 top-0 h-[1px] bg-gradient-to-r from-transparent via-amber-300/40 to-transparent pointer-events-none rounded-t-2xl"
      />

      {/* Header */}
      <div className="flex items-center gap-1.5 text-amber-400 font-bold text-[11px] select-none">
        <Sparkles className="w-3 h-3 text-amber-400 shrink-0" />
        <span>نکته گرامری</span>
      </div>

      {/* Corrected English Sentence in LTR */}
      {correctedSentence && (
        <div
          className="text-left font-mono font-bold text-[11.5px] text-white bg-white/10 px-2.5 py-1 rounded-lg border border-white/15 tracking-wide select-text"
          dir="ltr"
        >
          {correctedSentence}
        </div>
      )}

      {/* Persian Feedback in RTL */}
      {feedback ? (
        <p className="text-[11.5px] text-slate-200 leading-[1.6] text-justify font-sans">
          {feedback}
        </p>
      ) : (
        !correctedSentence && (
          <p className="text-[11.5px] text-slate-200 leading-[1.6] font-sans">
            جمله شما از نظر گرامری نیاز به اصلاح دارد.
          </p>
        )
      )}
    </motion.div>
  );
};

/**
 * 2026 Apple Liquid Glass User Chat Message Item.
 * Exactly matches Compose Multiplatform `UserChatItem.kt`:
 * - Right-aligned in RTL layout.
 * - Bubble shape: rounded-[18px] rounded-tr-[5px].
 * - Translucent Royal Azure & Electric Indigo glass gradient letting stage background shine through.
 * - Straddling grammar status badge (-bottom-2 right-2).
 * - Compact and snug fit.
 */
export const UserMessageItem: React.FC<UserMessageItemProps> = ({
  message,
  hasGrammarError = false,
  correctedSentence,
  grammarFeedbackFa,
  isEvaluatingGrammar = false,
  isSending = false,
}) => {
  const isEvaluating = isSending || isEvaluatingGrammar;

  return (
    <div className="flex flex-col items-end my-1.5 ml-auto self-end max-w-[90%] sm:max-w-[82%] select-none" dir="ltr">
      {/* 1. User Message Bubble Container with clearance for the bottom-right badge */}
      <div className="relative pb-2.5">
        {/* Royal Azure & Indigo Liquid Glass Bubble */}
        <div className="apple-glass-user-bubble relative rounded-[18px] rounded-tr-[5px] px-3.5 py-2 text-white overflow-hidden">
          {/* Specular Top Rim Light */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 top-0 h-[1px] bg-gradient-to-r from-white/80 via-white/30 to-transparent pointer-events-none"
          />

          <p className="text-[14.5px] sm:text-[15px] leading-[1.45] font-normal tracking-wide text-white select-text text-left">
            {message}
          </p>
        </div>

        {/* 2. Apple Liquid Glass Grammar Status Badge (straddling bottom-right) */}
        <div className="absolute -bottom-1.5 right-2 z-10 select-none" dir="rtl">
          {isEvaluating ? (
            <div className="h-5 px-2 rounded-full bg-[#121624]/90 border border-cyan-400/40 flex items-center gap-1 text-[10px] text-cyan-200 shadow-sm backdrop-blur-md">
              <span className="w-2 h-2 rounded-full border-[1.5px] border-cyan-400 border-t-transparent animate-spin" />
              <span>بررسی گرامر...</span>
            </div>
          ) : hasGrammarError ? (
            <div className="h-5 px-2 rounded-full bg-amber-400/25 border border-amber-400/60 flex items-center gap-1 text-[10px] font-bold text-amber-300 shadow-sm backdrop-blur-md">
              <Sparkles className="w-2.5 h-2.5 text-amber-400 shrink-0" />
              <span>نکته گرامری</span>
            </div>
          ) : (
            <div className="h-5 px-2 rounded-full bg-[#0a1b2d]/85 border border-cyan-400/50 flex items-center gap-1 text-[10px] font-bold text-cyan-300 shadow-sm backdrop-blur-md">
              <Check className="w-2.5 h-2.5 text-cyan-400 stroke-[2.5]" />
              <span>گرامر درست بود</span>
            </div>
          )}
        </div>
      </div>

      {/* 3. Grammar Tip Expansion Card connected via playful curved dash line */}
      {hasGrammarError && !isEvaluating && (
        <div className="flex items-start gap-1 mr-2 mt-0.5" dir="rtl">
          <CurvedDashLine color="#f59e0b" className="mt-1" />
          <GrammarTipCard
            feedback={grammarFeedbackFa}
            correctedSentence={correctedSentence}
          />
        </div>
      )}
    </div>
  );
};
