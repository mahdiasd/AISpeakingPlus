import React from 'react';
import { Target, CheckCircle2, User, HelpCircle, Star } from 'lucide-react';
import { CupertinoModal, CupertinoButton } from '../cupertino';
import { Stage } from '../../types/stage';

export interface ObjectiveModalProps {
  isOpen: boolean;
  onClose: () => void;
  stage: Stage | null;
}

export const ObjectiveModal: React.FC<ObjectiveModalProps> = ({
  isOpen,
  onClose,
  stage,
}) => {
  if (!stage) return null;

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={onClose}
      title="اهداف و سناریوی مرحله"
      subtitle={`مرحله ${stage.orderIndex} • ${stage.titleFa}`}
    >
      <div className="space-y-4">
        {/* Character Card */}
        <div className="flex items-center gap-3 p-3.5 rounded-2xl bg-white/5 border border-white/10">
          <div className="w-12 h-12 rounded-2xl overflow-hidden bg-apple-blue/20 flex items-center justify-center border border-white/15">
            {stage.characterAvatarUrl ? (
              <img
                src={stage.characterAvatarUrl}
                alt={stage.characterName}
                className="w-full h-full object-cover"
              />
            ) : (
              <User className="w-6 h-6 text-apple-blue" />
            )}
          </div>
          <div>
            <span className="text-xs text-apple-gray-400 block">هم‌صحبت شما:</span>
            <h4 className="text-sm font-bold text-white">{stage.characterName}</h4>
            <span className="text-[11px] text-apple-teal">شخصیت بومی انگلیسی‌زبان</span>
          </div>
        </div>

        {/* Mission Briefing */}
        <div className="p-4 rounded-2xl bg-white/5 border border-white/10 space-y-2">
          <div className="flex items-center gap-2 text-apple-blue text-xs font-bold">
            <HelpCircle className="w-4 h-4" />
            <span>شرح سناریو و موقعیت:</span>
          </div>
          <p className="text-xs text-slate-200 leading-relaxed font-sans">
            {stage.briefingFa}
          </p>
          {stage.briefing && (
            <p className="text-xs text-apple-gray-300 font-mono italic leading-relaxed pt-1" dir="ltr">
              {stage.briefing}
            </p>
          )}
        </div>

        {/* Target Objective */}
        <div className="p-4 rounded-2xl bg-gradient-to-br from-apple-blue/15 via-white/5 to-transparent border border-apple-blue/30 space-y-2">
          <div className="flex items-center gap-2 text-apple-teal text-xs font-bold">
            <Target className="w-4 h-4" />
            <span>هدف اصلی ماموریت (باید به این هدف برسید):</span>
          </div>
          <p className="text-xs text-white font-medium leading-relaxed font-sans">
            {stage.targetObjectiveFa || 'تکمیل مکالمه با رعایت نکات گرامری و پاسخ روان'}
          </p>
          {stage.targetObjective && (
            <p className="text-xs text-apple-teal font-mono leading-relaxed pt-1" dir="ltr">
              {stage.targetObjective}
            </p>
          )}
        </div>

        {/* Star Rubric Rules */}
        <div className="p-3.5 rounded-2xl bg-white/5 border border-white/10 space-y-2">
          <div className="flex items-center gap-2 text-apple-yellow text-xs font-bold">
            <Star className="w-4 h-4 fill-apple-yellow" />
            <span>شرایط کسب ۳ ستاره طلایی:</span>
          </div>
          <ul className="text-[11px] text-apple-gray-300 space-y-1.5 list-none pr-1">
            <li className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-apple-green shrink-0" />
              <span>پاسخگویی بدون خطای گرامری</span>
            </li>
            <li className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-apple-green shrink-0" />
              <span>عدم استفاده از دکمه راهنما (Hint)</span>
            </li>
            <li className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-apple-green shrink-0" />
              <span>رسیدن به هدف گفتگو در حداکثر {stage.maxTurns || 10} نوبت مکالمه</span>
            </li>
          </ul>
        </div>

        {/* Close Button */}
        <div className="pt-1">
          <CupertinoButton variant="filled" fullWidth size="lg" onClick={onClose}>
            متوجه شدم، ادامه مکالمه
          </CupertinoButton>
        </div>
      </div>
    </CupertinoModal>
  );
};
