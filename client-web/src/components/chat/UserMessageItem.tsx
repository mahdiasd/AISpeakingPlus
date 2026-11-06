import React from 'react';
import { AlertCircle, CheckCircle2, User } from 'lucide-react';
import { motion } from 'motion/react';

export interface UserMessageItemProps {
  id: string;
  message: string;
  hasGrammarError?: boolean;
  correctedSentence?: string | null;
  grammarFeedbackFa?: string | null;
  isSending?: boolean;
}

export const UserMessageItem: React.FC<UserMessageItemProps> = ({
  message,
  hasGrammarError = false,
  correctedSentence,
  grammarFeedbackFa,
  isSending = false,
}) => {
  return (
    <div className="flex items-start gap-3 justify-end max-w-[90%] sm:max-w-[82%] ml-auto my-3" dir="ltr">
      {/* 1. User Message Bubble (Positioned to the left of user avatar) */}
      <div className="space-y-1.5 flex-1 min-w-0">
        <div className="flex items-center justify-end gap-1.5 px-1" dir="rtl">
          <span className="text-[11px] font-semibold text-apple-gray-400">شما</span>
          {isSending && (
            <span className="text-[9px] px-1.5 py-0.5 rounded-full bg-white/10 text-apple-gray-300">
              در حال ارسال...
            </span>
          )}
        </div>

        {/* User Message Bubble with Top-Right Squircle Tail */}
        <div className="rounded-3xl rounded-tr-md p-4 text-white space-y-2.5 bg-gradient-to-r from-apple-blue to-[#5856D6] shadow-apple-md border border-white/20">
          <p className="text-[15px] leading-relaxed font-normal tracking-wide text-white select-text text-left" dir="ltr">
            {message}
          </p>

          {/* Grammar Feedback Alert */}
          {hasGrammarError && (
            <motion.div
              initial={{ opacity: 0, y: 4 }}
              animate={{ opacity: 1, y: 0 }}
              className="p-3 rounded-2xl bg-black/40 backdrop-blur-xl border border-white/20 text-xs space-y-1.5 text-right font-sans"
              dir="rtl"
            >
              <div className="flex items-center gap-1.5 text-apple-yellow font-bold text-[11px]">
                <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                <span>نکته و اصلاح گرامری:</span>
              </div>

              {correctedSentence && (
                <div className="text-left font-mono text-xs text-apple-teal bg-white/10 px-2.5 py-1.5 rounded-xl border border-white/10" dir="ltr">
                  {correctedSentence}
                </div>
              )}

              {grammarFeedbackFa && (
                <p className="text-[11px] text-slate-200 leading-relaxed font-sans">
                  {grammarFeedbackFa}
                </p>
              )}
            </motion.div>
          )}

          {!hasGrammarError && !isSending && (
            <div className="flex items-center justify-end gap-1 text-[10px] text-white/80 pt-0.5" dir="rtl">
              <CheckCircle2 className="w-3 h-3 text-apple-green" />
              <span>ارسال شد</span>
            </div>
          )}
        </div>
      </div>

      {/* 2. User Avatar (Anchored to the right) */}
      <div className="w-10 h-10 rounded-2xl shrink-0 overflow-hidden apple-glass border border-white/20 shadow-apple-sm flex items-center justify-center bg-gradient-to-br from-apple-blue to-[#5856D6] text-white">
        <User className="w-5 h-5" />
      </div>
    </div>
  );
};
