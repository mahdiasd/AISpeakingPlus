import React, { useEffect } from 'react';
import { SpellCheck, X, Lightbulb } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { CupertinoButton } from '../cupertino/CupertinoButton';

export interface LanguageLevelModalProps {
  isOpen: boolean;
  onClose: () => void;
  userLevel?: string;
}

/**
 * 2026 Apple Liquid Glass Language Level Explanation Bottom Sheet.
 *
 * Explains that this conversation is adapted to the user's selected CEFR level
 * and can be edited anytime in the profile screen.
 */
export const LanguageLevelModal: React.FC<LanguageLevelModalProps> = ({
  isOpen,
  onClose,
  userLevel = 'A1',
}) => {
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

  const displayLevel = userLevel?.trim() || 'A1';

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 select-none">
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
            initial={{ y: '100%', opacity: 0.5 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: '100%', opacity: 0 }}
            transition={{ type: 'spring', damping: 28, stiffness: 320 }}
            dir="rtl"
            className="relative z-10 w-full max-w-lg rounded-t-[28px] sm:rounded-[28px] bg-[#161826]/95 backdrop-blur-2xl border border-white/20 shadow-[0_16px_48px_rgba(0,0,0,0.5)] overflow-hidden flex flex-col max-h-[85vh]"
          >
            {/* Grabber handle (iOS style) */}
            <div className="w-full flex justify-center pt-3 pb-1">
              <div className="w-10 h-1.2 rounded-full bg-white/25" />
            </div>

            {/* Header with Close */}
            <div className="px-5 pt-2 pb-4 flex items-center justify-between border-b border-white/10">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-[#0a84ff]/20 border border-[#0a84ff]/40 flex items-center justify-center text-[#0a84ff]">
                  <SpellCheck className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">
                    سطح این مکالمه: سطح {displayLevel}
                  </h3>
                  <p className="text-xs text-slate-300">
                    هماهنگ با سطح دانش زبان شما
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={onClose}
                className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/15 flex items-center justify-center text-slate-300 hover:text-white transition-colors"
                aria-label="بستن"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Content Body */}
            <div className="p-5 space-y-4 overflow-y-auto">
              {/* Primary explanation card */}
              <div className="p-4 rounded-2xl bg-white/[0.05] border border-white/10 space-y-2.5">
                <p className="text-xs sm:text-sm text-slate-200 leading-relaxed font-sans text-justify">
                  این گفتگو و پاسخ‌های هوش مصنوعی دقیقاً مطابق با سطح زبانی «{displayLevel}» که انتخاب کرده‌اید تنظیم شده‌اند تا انتخاب کلمات، گرامر و سرعت پاسخ‌گویی متناسب با میزان تسلط شما باشد.
                </p>
              </div>

              {/* Note about profile change */}
              <div className="p-3.5 rounded-2xl bg-[#0a84ff]/10 border border-[#0a84ff]/25 flex items-start gap-2.5">
                <Lightbulb className="w-5 h-5 text-[#38bdf8] shrink-0 mt-0.5" />
                <p className="text-xs text-slate-200 leading-relaxed font-sans">
                  <strong className="text-[#38bdf8] ml-1">تغییر سطح:</strong>
                  در صورتی که می‌خواهید سطح مهارت خود را ساده‌تر یا پیشرفته‌تر کنید، می‌توانید در هر زمان به <span className="text-white font-bold underline decoration-white/40">صفحه پروفایل</span> مراجعه کرده و سطح زبان خود را ویرایش کنید.
                </p>
              </div>

              {/* Dismiss CTA */}
              <div className="pt-2">
                <CupertinoButton
                  variant="filled"
                  size="lg"
                  fullWidth
                  onClick={onClose}
                  className="bg-gradient-to-r from-[#0a84ff] to-[#5e5ce6] shadow-apple-md"
                >
                  <span>متوجه شدم</span>
                </CupertinoButton>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
