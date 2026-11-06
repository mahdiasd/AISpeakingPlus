import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, User, Check, AlertCircle } from 'lucide-react';

interface EditNickNameModalProps {
  isOpen: boolean;
  currentNickName: string;
  isSaving: boolean;
  onSave: (nickName: string) => Promise<void>;
  onClose: () => void;
}

export const EditNickNameModal: React.FC<EditNickNameModalProps> = ({
  isOpen,
  currentNickName,
  isSaving,
  onSave,
  onClose,
}) => {
  const [nickName, setNickName] = useState(currentNickName);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setNickName(currentNickName);
      setError(null);
    }
  }, [isOpen, currentNickName]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = nickName.trim();
    if (trimmed.length < 2) {
      setError('نام مستعار باید حداقل ۲ نویسه باشد');
      return;
    }
    if (trimmed.length > 50) {
      setError('نام مستعار نباید بیش از ۵۰ نویسه باشد');
      return;
    }

    try {
      await onSave(trimmed);
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'خطا در ذخیره نام مستعار');
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/60 backdrop-blur-md"
          />

          {/* Dialog */}
          <motion.div
            initial={{ opacity: 0, scale: 0.94, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.94, y: 20 }}
            transition={{ type: 'spring', damping: 26, stiffness: 320 }}
            className="relative w-full max-w-sm bg-apple-surface border border-apple-border rounded-3xl shadow-2xl p-5 z-10 space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white">ویرایش نام مستعار</h3>
              <button
                type="button"
                onClick={onClose}
                className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/15 flex items-center justify-center text-apple-gray-300 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-apple-gray-300">
                  نام یا عنوان نمایشی شما
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 right-0 pr-3.5 flex items-center pointer-events-none text-apple-gray-400">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    value={nickName}
                    onChange={(e) => {
                      setNickName(e.target.value);
                      if (error) setError(null);
                    }}
                    placeholder="مثال: رضا احمدی"
                    maxLength={50}
                    autoFocus
                    className="w-full pr-10 pl-3.5 py-2.5 rounded-2xl bg-white/5 border border-white/10 focus:border-apple-blue focus:ring-2 focus:ring-apple-blue/20 text-sm text-white placeholder-apple-gray-400 outline-none transition-all"
                  />
                </div>
                <div className="flex justify-between text-[11px] text-apple-gray-400 px-1">
                  <span>بین ۲ تا ۵۰ حرف</span>
                  <span>{nickName.length}/50</span>
                </div>
              </div>

              {error && (
                <div className="flex items-center gap-1.5 p-2 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-xs">
                  <AlertCircle className="w-3.5 h-3.5 flex-shrink-0" />
                  <span>{error}</span>
                </div>
              )}

              <div className="flex items-center justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-apple-gray-300 hover:text-white transition-colors"
                >
                  انصراف
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="px-5 py-2 rounded-xl bg-apple-blue hover:bg-blue-600 disabled:opacity-50 text-white text-xs font-semibold shadow-md active:scale-95 transition-all flex items-center gap-1.5"
                >
                  <Check className="w-3.5 h-3.5" />
                  <span>{isSaving ? 'در حال ذخیره…' : 'ذخیره تغییرات'}</span>
                </button>
              </div>
            </form>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
