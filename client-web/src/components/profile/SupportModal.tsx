import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, Send, MessageSquare, ExternalLink } from 'lucide-react';

interface SupportModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const TELEGRAM_URL = 'https://t.me/ai_speaking_support';
const RUBIKA_URL = 'https://rubika.ir/ai_speaking_support';

export const SupportModal: React.FC<SupportModalProps> = ({ isOpen, onClose }) => {
  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/60 backdrop-blur-md"
          />

          <motion.div
            initial={{ opacity: 0, scale: 0.94, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.94, y: 20 }}
            transition={{ type: 'spring', damping: 26, stiffness: 320 }}
            className="relative w-full max-w-sm bg-apple-surface border border-apple-border rounded-3xl shadow-2xl p-5 z-10 space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white">ارتباط با پشتیبانی</h3>
              <button
                type="button"
                onClick={onClose}
                className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/15 flex items-center justify-center text-apple-gray-300 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-apple-gray-300 leading-relaxed">
              تیم پشتیبانی AI Speaking Plus در پیام‌رسان‌های زیر آماده پاسخگویی به سوالات، پیشنهادات و رفع مشکلات شماست:
            </p>

            <div className="space-y-2.5 pt-1">
              {/* Telegram */}
              <a
                href={TELEGRAM_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center justify-between p-3.5 rounded-2xl bg-[#0088cc]/15 hover:bg-[#0088cc]/25 border border-[#0088cc]/30 text-white transition-all group"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-[#0088cc] text-white flex items-center justify-center shadow-md">
                    <Send className="w-4 h-4 ml-0.5" />
                  </div>
                  <div>
                    <span className="text-xs font-bold block">پشتیبانی تلگرام</span>
                    <span className="text-[11px] text-apple-gray-400 font-mono" dir="ltr">@ai_speaking_support</span>
                  </div>
                </div>
                <ExternalLink className="w-4 h-4 text-apple-gray-400 group-hover:text-white transition-colors" />
              </a>

              {/* Rubika */}
              <a
                href={RUBIKA_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center justify-between p-3.5 rounded-2xl bg-purple-600/15 hover:bg-purple-600/25 border border-purple-600/30 text-white transition-all group"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-purple-600 text-white flex items-center justify-center shadow-md">
                    <MessageSquare className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-xs font-bold block">پشتیبانی روبیکا</span>
                    <span className="text-[11px] text-apple-gray-400 font-mono" dir="ltr">ai_speaking_support</span>
                  </div>
                </div>
                <ExternalLink className="w-4 h-4 text-apple-gray-400 group-hover:text-white transition-colors" />
              </a>
            </div>

            <div className="pt-2 text-center">
              <button
                type="button"
                onClick={onClose}
                className="text-xs text-apple-gray-400 hover:text-white transition-colors"
              >
                بستن پنجره
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
