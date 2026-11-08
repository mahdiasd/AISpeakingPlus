import React, { useState, useEffect } from 'react';
import { Lightbulb, Volume2, ArrowRight, X, Sparkles, Check, Copy } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

export interface HintModalProps {
  isOpen: boolean;
  onClose: () => void;
  hintsUsedCount?: number;
  onConfirm?: () => void;
  isLoading?: boolean;
  suggestion?: string | null;
  explanationFa?: string | null;
  onUseSuggestion?: (text: string) => void;
  onPlayAudio?: (text: string) => void;
}

/**
 * 2026 Apple Liquid Glass Hint Confirmation & Suggestion Modal.
 *
 * Exact match of Compose HintConfirmDialog.kt & screenshot 05_chat_hint_modal.png:
 * - Luminous Champagne Gold Circular Icon (68px) with specular light rim
 * - Title: «راهنما می‌خوای؟»
 * - Body: «یک جمله مناسب برای ادامه گفتگو بهت پیشنهاد می‌دهیم تا گیر نکنی.»
 * - Penalty warning card with amber border:
 *     - Header: «نکته مهم»
 *     - Body: «هر بار گرفتن راهنما از ستاره‌های این مرحله کم می‌کند. برای گرفتن ۳ ستاره، بدون راهنما و بدون خطا جلو برو.»
 *     - Hints count note: «تا الان X بار راهنما گرفته‌ای.»
 * - Primary Golden CTA: «بله، راهنما بده»
 * - Secondary Glass CTA: «فعلاً نه»
 *
 * When confirmed, seamlessly transitions to the suggested sentence with pronunciation and auto-use.
 */
export const HintModal: React.FC<HintModalProps> = ({
  isOpen,
  onClose,
  hintsUsedCount = 0,
  onConfirm,
  isLoading = false,
  suggestion,
  explanationFa,
  onUseSuggestion,
  onPlayAudio,
}) => {
  // If suggestion is already present, default to showing confirmation first unless already confirmed
  const [hasConfirmed, setHasConfirmed] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (!isOpen) {
      setHasConfirmed(false);
      setCopied(false);
    }
  }, [isOpen]);

  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm();
    }
    setHasConfirmed(true);
  };

  const handleCopy = (text: string) => {
    navigator.clipboard?.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const showSuggestionView = hasConfirmed && Boolean(suggestion);

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Frosted Glass Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/65 backdrop-blur-[14px]"
          />

          {/* Modal Container */}
          <motion.div
            initial={{ scale: 0.92, opacity: 0, y: 16 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.92, opacity: 0, y: 16 }}
            transition={{ type: 'spring', damping: 28, stiffness: 380 }}
            className="relative w-full max-w-[360px] sm:max-w-[400px] rounded-[28px] apple-glass-card border border-white/20 shadow-[0_20px_50px_rgba(0,0,0,0.7)] p-6 text-white select-none z-10 backdrop-blur-[16px] overflow-hidden"
            dir="rtl"
          >
            {/* Top Specular Edge Highlight */}
            <div
              aria-hidden="true"
              className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
            />

            {/* Close cross button */}
            <button
              type="button"
              onClick={onClose}
              className="absolute top-4 left-4 p-1.5 rounded-full bg-white/10 hover:bg-white/20 active:scale-95 text-white/70 hover:text-white transition-all touch-manipulation z-20"
              title="بستن"
            >
              <X className="w-4 h-4" />
            </button>

            {!showSuggestionView ? (
              /* ================= MODE 1: Hint Confirm Dialog ================= */
              <div className="flex flex-col items-center text-center space-y-4 pt-1">
                {/* Luminous Champagne Gold Circular Icon */}
                <div className="relative">
                  <div className="w-[68px] h-[68px] rounded-full flex items-center justify-center bg-amber-400/15 border-[1.5px] border-amber-400/65 shadow-[0_0_24px_rgba(251,191,36,0.30)]">
                    <Lightbulb className="w-8 h-8 text-amber-400 fill-amber-400/30" />
                  </div>
                </div>

                {/* Title & Body */}
                <div className="space-y-1.5 px-2">
                  <h2 className="text-[20px] font-bold text-white tracking-tight">
                    راهنما می‌خوای؟
                  </h2>
                  <p className="text-[13.5px] text-slate-300 leading-[1.65] font-sans">
                    یک جمله مناسب برای ادامه گفتگو بهت پیشنهاد می‌دهیم تا گیر نکنی.
                  </p>
                </div>

                {/* Warning / Star Penalty Guidance Card */}
                <div className="w-full rounded-[18px] p-3.5 bg-amber-400/10 border border-amber-400/35 text-right space-y-1.5">
                  <div className="flex items-center gap-1.5 text-amber-400 font-bold text-xs">
                    <Sparkles className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                    <span>نکته مهم</span>
                  </div>
                  <p className="text-[12.5px] text-slate-200 leading-[1.65] text-justify font-sans">
                    هر بار گرفتن راهنما از ستاره‌های این مرحله کم می‌کند. برای گرفتن ۳ ستاره، بدون راهنما و بدون خطا جلو برو.
                  </p>
                  {hintsUsedCount > 0 && (
                    <p className="text-[11.5px] font-semibold text-cyan-300 pt-0.5">
                      تا الان {hintsUsedCount} بار راهنما گرفته‌ای.
                    </p>
                  )}
                </div>

                {/* Action Buttons */}
                <div className="w-full space-y-2.5 pt-2">
                  <motion.button
                    type="button"
                    whileTap={{ scale: 0.97 }}
                    onClick={handleConfirm}
                    disabled={isLoading}
                    className="w-full h-12 rounded-full font-bold text-[14.5px] bg-gradient-to-r from-amber-500 via-amber-400 to-yellow-400 text-slate-950 shadow-[0_4px_20px_rgba(245,158,11,0.45)] border border-amber-300/40 flex items-center justify-center gap-2 active:brightness-95 transition-all touch-manipulation disabled:opacity-50"
                  >
                    {isLoading ? (
                      <span className="inline-block w-4 h-4 rounded-full border-2 border-slate-950 border-t-transparent animate-spin" />
                    ) : (
                      <span>بله، راهنما بده</span>
                    )}
                  </motion.button>

                  <motion.button
                    type="button"
                    whileTap={{ scale: 0.97 }}
                    onClick={onClose}
                    className="w-full h-11 rounded-full text-sm font-semibold text-slate-200 apple-glass-pill border border-white/20 hover:bg-white/15 active:scale-95 transition-all touch-manipulation"
                  >
                    فعلاً نه
                  </motion.button>
                </div>
              </div>
            ) : (
              /* ================= MODE 2: Hint Suggestion Display ================= */
              <div className="space-y-4 pt-1 text-right">
                <div className="flex items-center gap-2 text-amber-400 font-bold text-sm">
                  <Lightbulb className="w-4 h-4 fill-amber-400/30" />
                  <span>جمله پیشنهادی انگلیسی:</span>
                </div>

                {/* Suggestion Monospace Bubble */}
                <div className="p-4 rounded-2xl bg-amber-400/10 border border-amber-400/30 space-y-3">
                  <div
                    className="p-3.5 rounded-xl bg-black/45 border border-white/15 text-white font-mono text-[14px] leading-relaxed select-text text-left tracking-wide"
                    dir="ltr"
                  >
                    {suggestion}
                  </div>

                  <div className="flex items-center justify-between pt-1">
                    {onPlayAudio && suggestion && (
                      <motion.button
                        type="button"
                        whileTap={{ scale: 0.95 }}
                        onClick={() => onPlayAudio(suggestion)}
                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-full apple-glass-pill text-cyan-300 border-cyan-400/35 text-xs font-semibold hover:bg-white/10"
                      >
                        <Volume2 className="w-3.5 h-3.5 text-cyan-400" />
                        <span>شنیدن تلفظ</span>
                      </motion.button>
                    )}

                    {suggestion && (
                      <motion.button
                        type="button"
                        whileTap={{ scale: 0.95 }}
                        onClick={() => handleCopy(suggestion)}
                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-full apple-glass-pill text-slate-300 border-white/20 text-xs hover:bg-white/10"
                      >
                        {copied ? (
                          <>
                            <Check className="w-3.5 h-3.5 text-cyan-400" />
                            <span>کپی شد</span>
                          </>
                        ) : (
                          <>
                            <Copy className="w-3.5 h-3.5 text-slate-400" />
                            <span>کپی متن</span>
                          </>
                        )}
                      </motion.button>
                    )}
                  </div>
                </div>

                {/* Explanation Card */}
                {explanationFa && (
                  <div className="p-3.5 rounded-2xl bg-white/5 border border-white/10 space-y-1">
                    <span className="text-xs font-bold text-slate-300 block">
                      توضیح و ترجمه فارسی:
                    </span>
                    <p className="text-[12.5px] text-slate-200 leading-relaxed font-sans text-justify">
                      {explanationFa}
                    </p>
                  </div>
                )}

                {/* Action CTA */}
                <div className="flex items-center gap-2.5 pt-2">
                  {onUseSuggestion && suggestion && (
                    <motion.button
                      type="button"
                      whileTap={{ scale: 0.97 }}
                      onClick={() => {
                        onUseSuggestion(suggestion);
                        onClose();
                      }}
                      className="flex-1 h-12 rounded-full font-bold text-sm bg-gradient-to-r from-cyan-500 via-blue-500 to-[#5856d6] text-white shadow-[0_4px_16px_rgba(0,122,255,0.35)] border border-white/20 flex items-center justify-center gap-1.5"
                    >
                      <span>استفاده از این جمله</span>
                      <ArrowRight className="w-4 h-4 rotate-180" />
                    </motion.button>
                  )}

                  <motion.button
                    type="button"
                    whileTap={{ scale: 0.97 }}
                    onClick={onClose}
                    className="h-12 px-5 rounded-full text-sm font-semibold text-slate-200 apple-glass-pill border border-white/20 hover:bg-white/15"
                  >
                    بستن
                  </motion.button>
                </div>
              </div>
            )}
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
