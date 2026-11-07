import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X } from 'lucide-react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  subtitle?: string;
  children: React.ReactNode;
  className?: string;
}

export const CupertinoModal: React.FC<CupertinoModalProps> = ({
  isOpen,
  onClose,
  title,
  subtitle,
  children,
  className,
}) => {
  // Lock body scroll when modal is open
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

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          {/* Frosted Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/60 backdrop-blur-md"
          />

          {/* Modal Container */}
          <motion.div
            initial={{ y: '100%', opacity: 0.8 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: '100%', opacity: 0.5 }}
            transition={{ type: 'spring', damping: 28, stiffness: 350 }}
            className={twMerge(
              clsx(
                'relative w-full max-w-lg max-h-[90vh] flex flex-col rounded-t-[32px] sm:rounded-[32px] overflow-hidden apple-glass-card border border-white/25 shadow-apple-lg text-white select-none z-10 backdrop-blur-3xl',
                className
              )
            )}
          >
            {/* Specular Top Edge Light Bar */}
            <div
              aria-hidden="true"
              className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
            />

            {/* iOS Drag Handle */}
            <div className="w-full pt-3 pb-1 flex justify-center shrink-0">
              <div className="w-10 h-1 rounded-full bg-white/20" />
            </div>

            {/* Header */}
            {(title || subtitle) && (
              <div className="relative px-6 py-3 flex items-center justify-between border-b border-white/10 shrink-0">
                <div className="space-y-0.5 pr-2">
                  {title && <h2 className="text-base font-bold text-white">{title}</h2>}
                  {subtitle && <p className="text-xs text-apple-gray-400">{subtitle}</p>}
                </div>

                <button
                  type="button"
                  onClick={onClose}
                  className="p-1.5 rounded-full bg-white/10 hover:bg-white/20 active:scale-95 transition-all text-white/70 hover:text-white touch-manipulation"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            )}

            {/* Scrollable Body */}
            <div className="flex-1 overflow-y-auto overscroll-contain p-6 space-y-4">
              {children}
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
