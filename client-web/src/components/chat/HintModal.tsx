import React from 'react';
import { Lightbulb, Sparkles, ArrowRight, Volume2 } from 'lucide-react';
import { CupertinoModal, CupertinoButton } from '../cupertino';

export interface HintModalProps {
  isOpen: boolean;
  onClose: () => void;
  suggestion: string;
  explanationFa: string;
  onUseSuggestion: (text: string) => void;
  onPlayAudio?: (text: string) => void;
}

export const HintModal: React.FC<HintModalProps> = ({
  isOpen,
  onClose,
  suggestion,
  explanationFa,
  onUseSuggestion,
  onPlayAudio,
}) => {
  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={onClose}
      title="راهنمایی هوشمند مکالمه"
      subtitle="جمله پیشنهادی متناسب با شرایط گفتگو"
    >
      <div className="space-y-4">
        {/* Suggestion Card */}
        <div className="p-4 rounded-2xl bg-gradient-to-br from-apple-yellow/20 via-white/5 to-transparent border border-apple-yellow/30 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-apple-yellow text-xs font-bold">
              <Lightbulb className="w-4 h-4 fill-apple-yellow/30" />
              <span>جمله پیشنهادی انگلیسی:</span>
            </div>
            {onPlayAudio && (
              <button
                type="button"
                onClick={() => onPlayAudio(suggestion)}
                className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-white/10 text-white text-xs hover:bg-white/15"
              >
                <Volume2 className="w-3.5 h-3.5 text-apple-yellow" />
                <span>شنیدن تلفظ</span>
              </button>
            )}
          </div>

          <div
            className="p-3.5 rounded-xl bg-black/40 border border-white/10 text-white font-mono text-sm leading-relaxed select-text"
            dir="ltr"
          >
            {suggestion}
          </div>
        </div>

        {/* Explanation Card */}
        <div className="p-4 rounded-2xl bg-white/5 border border-white/10 space-y-1.5 text-right" dir="rtl">
          <span className="text-xs font-bold text-apple-gray-300 block">
            توضیح کاربرد و ترجمه فارسی:
          </span>
          <p className="text-xs text-slate-200 leading-relaxed font-sans">
            {explanationFa}
          </p>
        </div>

        <div className="p-3 rounded-2xl bg-apple-yellow/10 border border-apple-yellow/20 text-apple-yellow text-[11px] flex items-center gap-2">
          <Sparkles className="w-4 h-4 shrink-0" />
          <span>استفاده از راهنما نمره این نوبت را به صورت جزئی کاهش می‌دهد.</span>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2.5 pt-2">
          <CupertinoButton
            variant="filled"
            size="lg"
            className="flex-1"
            onClick={() => {
              onUseSuggestion(suggestion);
              onClose();
            }}
          >
            <span>استفاده از این جمله</span>
            <ArrowRight className="w-4 h-4 rotate-180 mr-1" />
          </CupertinoButton>

          <CupertinoButton
            variant="glass"
            size="lg"
            onClick={onClose}
          >
            بستن
          </CupertinoButton>
        </div>
      </div>
    </CupertinoModal>
  );
};
