import React, { useState } from 'react';
import { api } from '../../api/client';
import { useToast } from '../../context/ToastContext';
import { UserItem, UserDetail } from '../../types';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Award, Calendar, Check, Gift } from 'lucide-react';

interface GrantSubscriptionModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: UserItem | UserDetail | null;
  onSuccess: () => void;
}

const PRESETS = [
  { label: '۷ روزه (تست/هدیه)', days: 7, planType: 'TRIAL' },
  { label: '۱ ماهه (۳۰ روز)', days: 30, planType: '1_MONTH' },
  { label: '۳ ماهه (۹۰ روز)', days: 90, planType: '3_MONTHS' },
  { label: '۶ ماهه (۱۸۰ روز)', days: 180, planType: '6_MONTHS' },
  { label: '۱ ساله (۳۶۵ روز)', days: 365, planType: '1_YEAR' },
  { label: 'مدت دلخواه...', days: 0, planType: 'CUSTOM' },
];

export const GrantSubscriptionModal: React.FC<GrantSubscriptionModalProps> = ({
  isOpen,
  onClose,
  user,
  onSuccess,
}) => {
  const { success, error } = useToast();
  const [selectedPreset, setSelectedPreset] = useState<number>(30);
  const [planType, setPlanType] = useState<string>('1_MONTH');
  const [customDays, setCustomDays] = useState<number>(14);
  const [reason, setReason] = useState<string>('اهدای دستی توسط ادمین');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  const handlePresetSelect = (days: number, type: string) => {
    setSelectedPreset(days);
    setPlanType(type);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    const finalDays = selectedPreset === 0 ? customDays : selectedPreset;
    if (finalDays <= 0) {
      error('تعداد روزهای اشتراک باید حداقل ۱ روز باشد');
      return;
    }

    if (!reason.trim()) {
      error('لطفاً دلیل اعطای دستی اشتراک را وارد کنید');
      return;
    }

    setIsSubmitting(true);
    try {
      await api.grantSubscription(user.id, planType, finalDays, reason.trim());
      success(`اشتراک ${finalDays} روزه برای کاربر با موفقیت ثبت شد`);
      onSuccess();
      onClose();
    } catch (err: any) {
      error(err.message || 'خطا در اعطای اشتراک');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!user) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="اعطای دستی اشتراک بدون پرداخت"
      description={`اهدای اشتراک به ${user.nickName || 'کاربر'} (${user.mobile})`}
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Banner */}
        <div className="p-3.5 rounded-xl bg-indigo-50 dark:bg-indigo-950/30 border border-indigo-200 dark:border-indigo-500/20 text-xs sm:text-sm text-indigo-800 dark:text-indigo-200 flex items-start gap-2.5">
          <Gift className="w-5 h-5 text-indigo-600 dark:text-indigo-400 shrink-0 mt-0.5" />
          <p className="leading-relaxed">
            این اشتراک به عنوان <span className="font-mono font-bold">MANUAL_ADMIN</span> ثبت شده و در صورت داشتن اشتراک فعال، به انتهای مهلت قبلی اضافه می‌گردد.
          </p>
        </div>

        {/* Preset Period Buttons */}
        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
            انتخاب دوره زمانی اشتراک
          </label>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
            {PRESETS.map((p) => {
              const isSelected = selectedPreset === p.days;
              return (
                <button
                  key={p.label}
                  type="button"
                  onClick={() => handlePresetSelect(p.days, p.planType)}
                  className={`p-3 rounded-xl text-xs sm:text-sm font-medium border text-center transition-all flex items-center justify-between active:scale-[0.98] ${
                    isSelected
                      ? 'bg-indigo-50 border-indigo-300 text-indigo-700 shadow-xs dark:bg-indigo-950/40 dark:border-indigo-500/60 dark:text-indigo-300'
                      : 'bg-slate-50 dark:bg-[#0E1422] border-slate-200 dark:border-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-[#151D30]'
                  }`}
                >
                  <span>{p.label}</span>
                  {isSelected && <Check className="w-4 h-4 text-indigo-600 dark:text-indigo-400 shrink-0" />}
                </button>
              );
            })}
          </div>
        </div>

        {/* Custom Days Input if selected */}
        {selectedPreset === 0 && (
          <div>
            <Input
              label="تعداد روزهای دلخواه"
              type="number"
              min={1}
              max={3650}
              value={customDays}
              onChange={(e) => setCustomDays(parseInt(e.target.value) || 1)}
              dir="ltr"
              leftIcon={<Calendar className="w-4 h-4" />}
            />
          </div>
        )}

        {/* Reason Input */}
        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            علت اعطای اشتراک (جهت درج در لاگ حسابرسی)
          </label>
          <textarea
            rows={2}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3.5 focus:outline-none focus:border-indigo-600 dark:focus:border-indigo-500 focus:ring-1 focus:ring-indigo-100 dark:focus:ring-indigo-500/20 placeholder-slate-400 dark:placeholder-slate-500 transition-colors leading-relaxed"
            placeholder="مثال: جایزه مسابقه برترین مکالمه، عذرخواهی قطعی سرور یا تستر رسمی"
          />
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200 dark:border-slate-800">
          <Button variant="ghost" type="button" onClick={onClose} disabled={isSubmitting}>
            انصراف
          </Button>
          <Button
            type="submit"
            isLoading={isSubmitting}
            icon={<Award className="w-4 h-4" />}
          >
            تأیید و اعطای اشتراک
          </Button>
        </div>
      </form>
    </Modal>
  );
};
