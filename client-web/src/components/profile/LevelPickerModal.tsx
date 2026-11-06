import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, Check, Sparkles } from 'lucide-react';
import { CEFR_LEVELS, CefrLevel } from '../../utils/cefr';

interface LevelPickerModalProps {
  isOpen: boolean;
  currentLevel?: string;
  isSaving: boolean;
  onSelect: (levelCode: string) => Promise<void>;
  onClose: () => void;
}

export const LevelPickerModal: React.FC<LevelPickerModalProps> = ({
  isOpen,
  currentLevel,
  isSaving,
  onSelect,
  onClose,
}) => {
  const selectedCode = (currentLevel || 'A1').toUpperCase();

  const handleLevelClick = async (code: string) => {
    if (code === selectedCode) {
      onClose();
      return;
    }
    await onSelect(code);
    onClose();
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/60 backdrop-blur-md"
          />

          {/* Sheet / Dialog */}
          <motion.div
            initial={{ opacity: 0, scale: 0.94, y: 40 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.94, y: 40 }}
            transition={{ type: 'spring', damping: 26, stiffness: 320 }}
            className="relative w-full max-w-md max-h-[90vh] bg-apple-surface border border-apple-border rounded-t-3xl sm:rounded-3xl shadow-2xl overflow-hidden flex flex-col z-10"
          >
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-white/5">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-lg bg-purple-500/15 border border-purple-500/30 flex items-center justify-center text-purple-400">
                  <Sparkles className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">سطح زبان انگلیسی (CEFR)</h3>
                  <p className="text-[11px] text-apple-gray-400">انتخاب چارچوب استاندارد اروپایی</p>
                </div>
              </div>

              <button
                type="button"
                onClick={onClose}
                className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/15 flex items-center justify-center text-apple-gray-300 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* List of Levels */}
            <div className="p-4 overflow-y-auto space-y-2.5 max-h-[60vh]">
              {CEFR_LEVELS.map((level: CefrLevel) => {
                const isSelected = selectedCode === level.code;

                return (
                  <motion.button
                    key={level.code}
                    type="button"
                    disabled={isSaving}
                    onClick={() => handleLevelClick(level.code)}
                    whileHover={{ scale: 1.01 }}
                    whileTap={{ scale: 0.98 }}
                    className={`w-full text-right p-3.5 rounded-2xl border transition-all flex items-start gap-3 ${
                      isSelected
                        ? 'bg-purple-500/15 border-purple-500 shadow-apple-glow ring-1 ring-purple-500/40'
                        : 'bg-white/5 border-white/10 hover:border-white/20 hover:bg-white/10'
                    }`}
                  >
                    {/* Level Code Badge */}
                    <div
                      className={`w-11 h-11 rounded-xl flex-shrink-0 flex items-center justify-center font-bold font-mono text-base ${level.badgeBg} border ${level.badgeBorder} ${level.badgeText}`}
                    >
                      {level.code}
                    </div>

                    {/* Level Info */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-1.5">
                          <span className="text-sm font-bold text-white">{level.titleFa}</span>
                          <span className="text-xs text-apple-gray-400 font-sans">({level.titleEn})</span>
                        </div>
                        {isSelected && (
                          <div className="w-6 h-6 rounded-full bg-purple-500 text-white flex items-center justify-center shadow">
                            <Check className="w-3.5 h-3.5 stroke-[3]" />
                          </div>
                        )}
                      </div>
                      <p className="text-xs text-apple-gray-300 leading-relaxed mt-1">
                        {level.descriptionFa}
                      </p>
                    </div>
                  </motion.button>
                );
              })}
            </div>

            {/* Footer */}
            <div className="p-3 border-t border-white/5 bg-black/20 text-center">
              <span className="text-[11px] text-apple-gray-400">
                سطح انتخابی برای شخصی‌سازی سرعت مکالمه و واژگان استفاده می‌شود
              </span>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
