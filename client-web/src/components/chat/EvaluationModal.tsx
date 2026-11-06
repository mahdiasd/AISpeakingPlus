import React, { useEffect } from 'react';
import { Star, Trophy, RotateCcw, ArrowRight, AlertTriangle, Map } from 'lucide-react';
import confetti from 'canvas-confetti';
import { CupertinoModal, CupertinoButton } from '../cupertino';
import { EvaluationResponse } from '../../api/chatApi';

export interface EvaluationModalProps {
  isOpen: boolean;
  onClose: () => void;
  evaluation: EvaluationResponse | null;
  stageTitle?: string;
  onNextStage?: () => void;
  onRetryStage?: () => void;
  onBackToMap?: () => void;
}

export const EvaluationModal: React.FC<EvaluationModalProps> = ({
  isOpen,
  onClose,
  evaluation,
  stageTitle,
  onNextStage,
  onRetryStage,
  onBackToMap,
}) => {
  useEffect(() => {
    if (isOpen && evaluation && evaluation.starsEarned > 0) {
      // Fire celebration confetti
      confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.5 },
      });
      setTimeout(() => {
        confetti({
          particleCount: 50,
          angle: 60,
          spread: 55,
          origin: { x: 0 },
        });
        confetti({
          particleCount: 50,
          angle: 120,
          spread: 55,
          origin: { x: 1 },
        });
      }, 250);
    }
  }, [isOpen, evaluation]);

  if (!evaluation) return null;

  const starsEarned = evaluation.starsEarned;

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={onClose}
      title="کارنامه پایان مرحله"
      subtitle={stageTitle || 'نتیجه مکالمه هوشمند'}
    >
      <div className="space-y-4 text-center">
        {/* Star Rating Section */}
        <div className="py-2 space-y-2">
          <div className="flex items-center justify-center gap-2">
            {[1, 2, 3].map((starIndex) => (
              <div
                key={starIndex}
                className={`transition-all duration-500 transform ${
                  starIndex <= starsEarned
                    ? 'scale-110 drop-shadow-[0_0_12px_rgba(255,204,0,0.8)]'
                    : 'opacity-30 scale-90'
                }`}
              >
                <Star
                  className={`w-10 h-10 ${
                    starIndex <= starsEarned
                      ? 'fill-apple-yellow text-apple-yellow'
                      : 'text-apple-gray-400'
                  }`}
                />
              </div>
            ))}
          </div>

          <div className="text-sm font-bold text-white">
            {starsEarned === 3 && 'عالی! ۳ ستاره کامل کسب شد!'}
            {starsEarned === 2 && 'بسیار خوب! ۲ ستاره کسب کردید.'}
            {starsEarned === 1 && 'خوب! مرحله با ۱ ستاره تکمیل شد.'}
            {starsEarned === 0 && 'مرحله بدون ستاره به پایان رسید.'}
          </div>
        </div>

        {/* Score & Metrics Grid */}
        <div className="grid grid-cols-3 gap-2 text-center">
          <div className="p-3 rounded-2xl bg-white/5 border border-white/10">
            <span className="text-[10px] text-apple-gray-400 block mb-0.5">نمره کلی:</span>
            <span className="text-base font-extrabold text-apple-yellow font-mono">
              {evaluation.score} / ۱۰۰
            </span>
          </div>

          <div className="p-3 rounded-2xl bg-white/5 border border-white/10">
            <span className="text-[10px] text-apple-gray-400 block mb-0.5">خطای گرامر:</span>
            <span
              className={`text-base font-extrabold font-mono ${
                evaluation.grammarErrorsCount === 0 ? 'text-apple-green' : 'text-apple-red'
              }`}
            >
              {evaluation.grammarErrorsCount} مورد
            </span>
          </div>

          <div className="p-3 rounded-2xl bg-white/5 border border-white/10">
            <span className="text-[10px] text-apple-gray-400 block mb-0.5">راهنماهای استفاده شده:</span>
            <span className="text-base font-extrabold text-apple-blue font-mono">
              {evaluation.hintsUsedCount} بار
            </span>
          </div>
        </div>

        {/* AI Feedback Card */}
        <div className="p-4 rounded-2xl bg-white/5 border border-white/10 text-right space-y-1.5" dir="rtl">
          <div className="flex items-center gap-1.5 text-xs font-bold text-apple-blue">
            <Trophy className="w-4 h-4 text-apple-yellow shrink-0" />
            <span>تحلیل عملکرد و ارزیابی هوش مصنوعی:</span>
          </div>
          <p className="text-xs text-slate-200 leading-relaxed font-sans">
            {evaluation.feedbackFa}
          </p>
        </div>

        {/* Grammar Errors Review (if any) */}
        {evaluation.grammarErrors && evaluation.grammarErrors.length > 0 && (
          <div className="p-3.5 rounded-2xl bg-apple-red/10 border border-apple-red/20 text-right space-y-2.5" dir="rtl">
            <div className="flex items-center gap-1.5 text-xs font-bold text-apple-red">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>نکات و اصلاحات گرامری این گفتگو:</span>
            </div>

            <div className="space-y-2">
              {evaluation.grammarErrors.map((err, idx) => (
                <div key={idx} className="p-2.5 rounded-xl bg-black/40 border border-white/10 text-xs space-y-1">
                  <div className="text-apple-red line-through font-mono text-[11px] text-left" dir="ltr">
                    {err.original}
                  </div>
                  <div className="text-apple-green font-mono text-[11px] text-left font-bold" dir="ltr">
                    {err.correction}
                  </div>
                  <p className="text-[11px] text-slate-300 font-sans pt-0.5">
                    {err.explanationFa}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="space-y-2 pt-2">
          {evaluation.unlockedNextStage && onNextStage && (
            <CupertinoButton
              variant="filled"
              size="lg"
              fullWidth
              onClick={onNextStage}
              className="bg-gradient-to-r from-apple-green to-emerald-600 shadow-apple-md"
            >
              <span>رفتن به مرحله بعدی</span>
              <ArrowRight className="w-4 h-4 rotate-180 mr-1.5" />
            </CupertinoButton>
          )}

          <div className="flex items-center gap-2">
            {onRetryStage && (
              <CupertinoButton
                variant="glass"
                size="md"
                className="flex-1"
                onClick={onRetryStage}
              >
                <RotateCcw className="w-4 h-4 ml-1" />
                <span>تکرار مرحله</span>
              </CupertinoButton>
            )}

            {onBackToMap && (
              <CupertinoButton
                variant="glass"
                size="md"
                className="flex-1"
                onClick={onBackToMap}
              >
                <Map className="w-4 h-4 ml-1" />
                <span>لیست مراحل</span>
              </CupertinoButton>
            )}
          </div>
        </div>
      </div>
    </CupertinoModal>
  );
};
