import React from 'react';
import { AlertTriangle, LogOut } from 'lucide-react';
import { CupertinoModal, CupertinoButton } from '../cupertino';

export interface ExitConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirmExit: () => void;
}

export const ExitConfirmModal: React.FC<ExitConfirmModalProps> = ({
  isOpen,
  onClose,
  onConfirmExit,
}) => {
  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={onClose}
      title="خروج از مکالمه؟"
      subtitle="آیا مطمئن هستید که می‌خواهید از اتاق مکالمه خارج شوید؟"
    >
      <div className="space-y-4">
        <div className="p-4 rounded-2xl bg-apple-red/10 border border-apple-red/20 flex items-start gap-3">
          <AlertTriangle className="w-5 h-5 text-apple-red shrink-0 mt-0.5" />
          <p className="text-xs text-slate-200 leading-relaxed font-sans">
            در صورت خروج پیش از رسیدن به هدف و ثبت پایان مکالمه، امتیاز و پیشرفت این دور مکالمه ثبت نخواهد شد.
          </p>
        </div>

        <div className="flex items-center gap-2.5 pt-2">
          <CupertinoButton
            variant="filled"
            size="lg"
            className="flex-1 bg-apple-red hover:bg-apple-red/90"
            onClick={onConfirmExit}
          >
            <LogOut className="w-4 h-4 ml-1.5" />
            <span>بله، خروج</span>
          </CupertinoButton>

          <CupertinoButton
            variant="glass"
            size="lg"
            className="flex-1"
            onClick={onClose}
          >
            ادامه مکالمه
          </CupertinoButton>
        </div>
      </div>
    </CupertinoModal>
  );
};
