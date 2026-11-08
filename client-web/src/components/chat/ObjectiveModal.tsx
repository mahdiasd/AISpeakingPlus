import React, { useEffect } from 'react';
import { Target, BookOpen, X, Sparkles, User } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Stage } from '../../types/stage';

export interface ObjectiveModalProps {
  isOpen: boolean;
  onClose: () => void;
  stage: Stage | null;
}

/**
 * 2026 Apple Liquid Glass Objective Bottom Sheet.
 *
 * Exact match of Compose ObjectiveSheet.kt & screenshot 06_chat_stage_goal_sheet.png:
 * - Apple Cupertino Bottom Sheet with grabber pill on top
 * - Smooth spring slide transition from bottom
 * - Stage title with CEFR level & character info
 * - Mission Objective container glowing in Serene Cyan & Royal Azure
 * - Scenario narrative context ("ماجرا چیست؟") in Warm Champagne Gold
 * - Tactile Apple Primary Button ("متوجه شدم")
 */
export const ObjectiveModal: React.FC<ObjectiveModalProps> = ({
  isOpen,
  onClose,
  stage,
}) => {
  // Lock body scroll when sheet is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      const handleKeyDown = (e: KeyboardEvent) => {
        if (e.key === 'Escape') onClose();
      };
      window.addEventListener('keydown', handleKeyDown);
      return () => {
        document.body.style.overflow = '';
        window.removeEventListener('keydown', handleKeyDown);
      };
    }
  }, [isOpen, onClose]);

  if (!stage) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          {/* Frosted Glass Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/65 backdrop-blur-[14px]"
          />

          {/* Bottom Sheet Container */}
          <motion.div
            initial={{ y: '100%', opacity: 0.8 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: '100%', opacity: 0.5 }}
            transition={{ type: 'spring', damping: 28, stiffness: 350 }}
            className="relative w-full max-w-lg max-h-[90vh] flex flex-col rounded-t-[32px] sm:rounded-[32px] apple-glass-card border border-white/20 shadow-[0_25px_60px_rgba(0,0,0,0.75)] text-white select-none z-10 backdrop-blur-[16px] overflow-hidden"
            dir="rtl"
          >
            {/* Top Specular Edge Highlight */}
            <div
              aria-hidden="true"
              className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
            />

            {/* Apple iOS Grabber Pill */}
            <div className="w-full pt-3 pb-1 flex justify-center shrink-0">
              <div className="w-12 h-1.5 rounded-full bg-white/25" />
            </div>

            {/* Header */}
            <div className="relative px-6 py-2.5 flex items-center justify-between border-b border-white/10 shrink-0">
              <div className="space-y-0.5 pr-1">
                <h2 className="text-[18px] font-bold text-white tracking-tight">
                  {stage.titleFa}
                </h2>
                <div className="flex items-center gap-2 text-xs text-slate-300">
                  <span>مرحله {stage.orderIndex}</span>
                  <span>•</span>
                  <span className="text-cyan-300 font-semibold">{stage.characterName}</span>
                </div>
              </div>

              <button
                type="button"
                onClick={onClose}
                className="p-1.5 rounded-full bg-white/10 hover:bg-white/20 active:scale-95 text-white/70 hover:text-white transition-all touch-manipulation"
                title="بستن"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Scrollable Content Body */}
            <div className="flex-1 overflow-y-auto overscroll-contain p-6 space-y-4">
              {/* 1. Character Companion Chip */}
              <div className="flex items-center gap-3 p-3 rounded-2xl apple-glass border border-white/12 bg-white/5">
                <div className="w-11 h-11 rounded-[14px] overflow-hidden bg-cyan-500/15 border border-cyan-400/30 flex items-center justify-center shrink-0">
                  {stage.characterAvatarUrl ? (
                    <img
                      src={stage.characterAvatarUrl}
                      alt={stage.characterName}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <User className="w-5 h-5 text-cyan-300" />
                  )}
                </div>
                <div className="space-y-0.5">
                  <span className="text-[11px] text-slate-400 block font-medium">هم‌صحبت شما:</span>
                  <h4 className="text-sm font-bold text-white">{stage.characterName}</h4>
                  <span className="text-[11px] text-cyan-300 font-medium">شخصیت بومی انگلیسی‌زبان</span>
                </div>
              </div>

              {/* 2. Mission Objective Container (Serene Cyan & Royal Azure glow) */}
              <div className="rounded-[20px] p-4.5 bg-[#007aff]/15 border border-cyan-400/40 space-y-2.5 text-right shadow-[0_4px_20px_rgba(0,122,255,0.18)]">
                <div className="flex items-center gap-1.5 text-cyan-300 font-bold text-xs">
                  <Target className="w-4 h-4 text-cyan-400 shrink-0" />
                  <span>هدف تو در این مرحله</span>
                </div>

                {stage.targetObjectiveFa && (
                  <p className="text-[13.5px] font-semibold text-white leading-[1.65] text-justify font-sans">
                    {stage.targetObjectiveFa}
                  </p>
                )}

                {stage.targetObjective && (
                  <div
                    className="p-3 rounded-xl bg-black/40 border border-white/10 text-xs text-slate-200 font-mono tracking-wide leading-relaxed text-left"
                    dir="ltr"
                  >
                    {stage.targetObjective}
                  </div>
                )}
              </div>

              {/* 3. Scenario Narrative Container («ماجرا چیست؟») */}
              {stage.briefingFa && (
                <div className="rounded-[20px] p-4.5 apple-glass border border-white/15 bg-white/5 space-y-2.5 text-right">
                  <div className="flex items-center gap-1.5 text-amber-400 font-bold text-xs">
                    <BookOpen className="w-4 h-4 text-amber-400 shrink-0" />
                    <span>ماجرا چیست؟</span>
                  </div>

                  <p className="text-[13px] text-slate-200 leading-[1.7] text-justify font-sans">
                    {stage.briefingFa}
                  </p>

                  {stage.briefing && (
                    <div
                      className="text-xs text-slate-300 font-mono italic leading-relaxed text-left pt-2 border-t border-white/10"
                      dir="ltr"
                    >
                      {stage.briefing}
                    </div>
                  )}
                </div>
              )}

              {/* 4. Three-Star Mastery Tips */}
              <div className="p-3.5 rounded-2xl bg-amber-400/10 border border-amber-400/25 space-y-1.5">
                <div className="flex items-center gap-1.5 text-amber-400 font-bold text-xs">
                  <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                  <span>شرایط کسب ۳ ستاره طلایی:</span>
                </div>
                <p className="text-[12px] text-slate-300 leading-relaxed font-sans">
                  برای کسب امتیاز کامل، بدون استفاده از دکمه راهنما و با کمترین خطای گرامری گفتگو را تکمیل کن.
                </p>
              </div>

              {/* 5. Tactile Apple Primary Button */}
              <div className="pt-2">
                <motion.button
                  type="button"
                  whileTap={{ scale: 0.97 }}
                  onClick={onClose}
                  className="w-full h-12 rounded-full font-bold text-[15px] bg-gradient-to-r from-apple-blue via-blue-600 to-[#5856D6] text-white shadow-[0_4px_20px_rgba(0,122,255,0.38)] border border-white/20 active:brightness-95 transition-all touch-manipulation flex items-center justify-center"
                >
                  متوجه شدم
                </motion.button>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
