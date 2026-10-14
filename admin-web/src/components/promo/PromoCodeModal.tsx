import React, { useState, useEffect } from 'react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { PromoCodeItem, PromoCodeUpsertPayload, DiscountType } from '../../types';
import {
  Sparkles,
  Percent,
  Coins,
  Layers,
  Users,
  AlertCircle,
} from 'lucide-react';

interface PromoCodeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (payload: PromoCodeUpsertPayload) => Promise<void>;
  initialData?: PromoCodeItem | null;
}

const AVAILABLE_PLANS = [
  { id: 'plan-1m', label: 'اشتراک ۱ ماهه' },
  { id: 'plan-3m', label: 'اشتراک ۳ ماهه' },
  { id: 'plan-6m', label: 'اشتراک ۶ ماهه' },
];

// Helper to format ISO to datetime-local input string
const toLocalInputString = (isoString?: string | null): string => {
  if (!isoString) return '';
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return '';
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  } catch {
    return '';
  }
};

export const PromoCodeModal: React.FC<PromoCodeModalProps> = ({
  isOpen,
  onClose,
  onSave,
  initialData,
}) => {
  const isEdit = Boolean(initialData);

  const [code, setCode] = useState('');
  const [title, setTitle] = useState('');
  const [discountType, setDiscountType] = useState<DiscountType>('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState<number>(20);
  const [maxDiscountCap, setMaxDiscountCap] = useState<string>('');
  const [minOrderAmount, setMinOrderAmount] = useState<string>('');
  const [allPlans, setAllPlans] = useState<boolean>(true);
  const [selectedPlans, setSelectedPlans] = useState<string[]>([]);
  const [isUnlimitedUses, setIsUnlimitedUses] = useState<boolean>(true);
  const [maxUses, setMaxUses] = useState<string>('');
  const [isOncePerUser, setIsOncePerUser] = useState<boolean>(true);
  const [startsAt, setStartsAt] = useState<string>('');
  const [expiresAt, setExpiresAt] = useState<string>('');
  const [isActive, setIsActive] = useState<boolean>(true);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    if (initialData) {
      setCode(initialData.code);
      setTitle(initialData.title || '');
      setDiscountType(initialData.discountType);
      setDiscountValue(initialData.discountValue);
      setMaxDiscountCap(initialData.maxDiscountCap ? initialData.maxDiscountCap.toString() : '');
      setMinOrderAmount(initialData.minOrderAmount ? initialData.minOrderAmount.toString() : '');
      const hasSpecificPlans = initialData.applicablePlans && initialData.applicablePlans.length > 0;
      setAllPlans(!hasSpecificPlans);
      setSelectedPlans(initialData.applicablePlans || []);
      setIsUnlimitedUses(initialData.maxUses == null);
      setMaxUses(initialData.maxUses != null ? initialData.maxUses.toString() : '');
      setIsOncePerUser(initialData.isOncePerUser ?? true);
      setStartsAt(toLocalInputString(initialData.startsAt));
      setExpiresAt(toLocalInputString(initialData.expiresAt));
      setIsActive(initialData.isActive);
    } else {
      // Defaults for new code
      setCode('');
      setTitle('');
      setDiscountType('PERCENTAGE');
      setDiscountValue(20);
      setMaxDiscountCap('');
      setMinOrderAmount('');
      setAllPlans(true);
      setSelectedPlans([]);
      setIsUnlimitedUses(true);
      setMaxUses('');
      setIsOncePerUser(true);
      setStartsAt('');
      // Default expiry: 30 days from now
      const in30Days = new Date();
      in30Days.setDate(in30Days.getDate() + 30);
      setExpiresAt(toLocalInputString(in30Days.toISOString()));
      setIsActive(true);
    }
    setErrorMsg(null);
  }, [initialData, isOpen]);

  // Random Code Generator
  const generateRandomCode = () => {
    const prefixes = ['VIP', 'GOLD', 'SPEAK', 'AI', 'NOWRUZ', 'SPRING', 'SPECIAL', 'GIFT'];
    const prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
    const val = discountType === 'PERCENTAGE' ? discountValue : Math.floor(Math.random() * 9 + 1) * 10;
    const randomSuffix = Math.floor(Math.random() * 900 + 100);
    setCode(`${prefix}${val}_${randomSuffix}`);
  };

  const handlePlanToggle = (planId: string) => {
    setAllPlans(false);
    setSelectedPlans((prev) =>
      prev.includes(planId) ? prev.filter((p) => p !== planId) : [...prev, planId]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    const cleanCode = code.trim().toUpperCase().replace(/[^A-Z0-9_-]/g, '');
    if (!cleanCode) {
      setErrorMsg('لطفاً عبارت کد تخفیف را وارد کنید (فقط حروف و اعداد انگلیسی).');
      return;
    }

    if (discountValue <= 0) {
      setErrorMsg('میزان تخفیف باید بزرگ‌تر از صفر باشد.');
      return;
    }

    if (discountType === 'PERCENTAGE' && discountValue > 100) {
      setErrorMsg('درصد تخفیف نمی‌تواند بیشتر از ۱۰۰٪ باشد.');
      return;
    }

    if (!allPlans && selectedPlans.length === 0) {
      setErrorMsg('لطفاً حداقل یک پلن اشتراک انتخاب کنید یا گزینه «همه پلن‌ها» را علامت بزنید.');
      return;
    }

    const payload: PromoCodeUpsertPayload = {
      code: cleanCode,
      title: title.trim() || undefined,
      discountType,
      discountValue: Number(discountValue),
      maxDiscountCap: discountType === 'PERCENTAGE' && maxDiscountCap ? Number(maxDiscountCap) : null,
      minOrderAmount: minOrderAmount ? Number(minOrderAmount) : null,
      applicablePlans: allPlans ? [] : selectedPlans,
      maxUses: isUnlimitedUses || !maxUses ? null : Number(maxUses),
      isOncePerUser,
      startsAt: startsAt ? new Date(startsAt).toISOString() : null,
      expiresAt: expiresAt ? new Date(expiresAt).toISOString() : null,
      isActive,
    };

    setIsSubmitting(true);
    try {
      await onSave(payload);
      onClose();
    } catch (err: any) {
      setErrorMsg(err.message || 'خطا در ثبت اطلاعات کد تخفیف');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEdit ? 'ویرایش کد تخفیف' : 'ثبت کد تخفیف جدید'}
      description="تعریف کد تخفیف، محدودیت‌های استفاده و تنظیمات پلن‌های اشتراک"
      maxWidth="2xl"
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        {errorMsg && (
          <div className="flex items-start gap-2.5 p-3.5 rounded-xl bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 text-red-700 dark:text-red-400 text-sm animate-in fade-in">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <div className="leading-relaxed">{errorMsg}</div>
          </div>
        )}

        {/* Code & Random Generator */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
            عبارت کد تخفیف <span className="text-red-500">*</span>
          </label>
          <div className="flex items-center gap-2">
            <div className="relative flex-1">
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value.toUpperCase())}
                placeholder="مثال: NOROOZ1404 یا VIP30"
                dir="ltr"
                required
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white font-mono font-bold tracking-wider text-base focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all uppercase placeholder:font-sans placeholder:font-normal placeholder:text-slate-400"
              />
            </div>
            <button
              type="button"
              onClick={generateRandomCode}
              className="inline-flex items-center gap-1.5 px-3.5 py-2.5 rounded-xl bg-indigo-50 hover:bg-indigo-100 dark:bg-indigo-500/15 dark:hover:bg-indigo-500/25 text-indigo-700 dark:text-indigo-300 font-medium text-xs border border-indigo-200 dark:border-indigo-500/30 transition-all active:scale-95 shrink-0"
              title="تولید کد رندم هوشمند"
            >
              <Sparkles className="w-4 h-4 text-indigo-500" />
              تولید تصادفی
            </button>
          </div>
          <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1">
            کدها به طور خودکار به حروف بزرگ انگلیسی تبدیل می‌شوند.
          </p>
        </div>

        {/* Title / Internal Note */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
            عنوان یا یادداشت داخلی (اختیاری)
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="مثال: کمپین شب عید، تخفیف ۳۰٪ بلاگرها"
            className="w-full px-3.5 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all placeholder:text-slate-400"
          />
        </div>

        {/* Discount Type Selector (Segmented Button) */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
            نوع تخفیف <span className="text-red-500">*</span>
          </label>
          <div className="grid grid-cols-2 gap-2 p-1 rounded-xl bg-slate-100 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-800">
            <button
              type="button"
              onClick={() => setDiscountType('PERCENTAGE')}
              className={`flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-xs font-medium transition-all ${
                discountType === 'PERCENTAGE'
                  ? 'bg-white dark:bg-[#111726] text-indigo-600 dark:text-indigo-400 shadow-xs font-bold'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              <Percent className="w-4 h-4" />
              درصدی (%)
            </button>
            <button
              type="button"
              onClick={() => setDiscountType('FIXED_AMOUNT')}
              className={`flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-xs font-medium transition-all ${
                discountType === 'FIXED_AMOUNT'
                  ? 'bg-white dark:bg-[#111726] text-indigo-600 dark:text-indigo-400 shadow-xs font-bold'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              <Coins className="w-4 h-4" />
              مبلغ ثابت (تومان)
            </button>
          </div>
        </div>

        {/* Discount Value & Max Cap */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              {discountType === 'PERCENTAGE' ? 'درصد تخفیف (%)' : 'مبلغ تخفیف (تومان)'}{' '}
              <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <input
                type="number"
                min="1"
                max={discountType === 'PERCENTAGE' ? '100' : undefined}
                value={discountValue}
                onChange={(e) => setDiscountValue(Number(e.target.value))}
                required
                className="w-full px-3.5 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-sm font-mono font-semibold focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
              />
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-xs text-slate-400">
                {discountType === 'PERCENTAGE' ? '٪' : 'تومان'}
              </span>
            </div>
          </div>

          {discountType === 'PERCENTAGE' ? (
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                حداکثر سقف تخفیف (تومان)
              </label>
              <div className="relative">
                <input
                  type="number"
                  min="0"
                  step="1000"
                  value={maxDiscountCap}
                  onChange={(e) => setMaxDiscountCap(e.target.value)}
                  placeholder="بدون سقف"
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all placeholder:font-sans placeholder:text-slate-400"
                />
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-xs text-slate-400">
                  تومان
                </span>
              </div>
            </div>
          ) : (
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                حداقل مبلغ خرید (تومان)
              </label>
              <div className="relative">
                <input
                  type="number"
                  min="0"
                  step="1000"
                  value={minOrderAmount}
                  onChange={(e) => setMinOrderAmount(e.target.value)}
                  placeholder="بدون شرط حداقل"
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all placeholder:font-sans placeholder:text-slate-400"
                />
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-xs text-slate-400">
                  تومان
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Plan Restrictions */}
        <div className="p-3.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/30 space-y-2.5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
              <Layers className="w-4 h-4 text-indigo-500" />
              محدودیت پلن‌های اشتراک
            </span>
            <label className="flex items-center gap-1.5 text-xs text-slate-600 dark:text-slate-400 cursor-pointer">
              <input
                type="checkbox"
                checked={allPlans}
                onChange={(e) => {
                  setAllPlans(e.target.checked);
                  if (e.target.checked) setSelectedPlans([]);
                }}
                className="w-4 h-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
              />
              قابل اعمال روی همه پلن‌ها
            </label>
          </div>

          {!allPlans && (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 pt-1 animate-in fade-in">
              {AVAILABLE_PLANS.map((plan) => (
                <label
                  key={plan.id}
                  className={`flex items-center gap-2 p-2 rounded-lg border text-xs cursor-pointer transition-all ${
                    selectedPlans.includes(plan.id)
                      ? 'bg-indigo-50/80 border-indigo-300 dark:bg-indigo-500/15 dark:border-indigo-500/40 text-indigo-900 dark:text-indigo-200 font-medium'
                      : 'border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800/40'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={selectedPlans.includes(plan.id)}
                    onChange={() => handlePlanToggle(plan.id)}
                    className="w-3.5 h-3.5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span>{plan.label}</span>
                </label>
              ))}
            </div>
          )}
        </div>

        {/* Usage Limits */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 p-3.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/30">
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-semibold text-slate-800 dark:text-slate-200">
                سقف کل دفعات استفاده
              </label>
              <label className="flex items-center gap-1 text-[11px] text-slate-500 dark:text-slate-400 cursor-pointer">
                <input
                  type="checkbox"
                  checked={isUnlimitedUses}
                  onChange={(e) => {
                    setIsUnlimitedUses(e.target.checked);
                    if (e.target.checked) setMaxUses('');
                  }}
                  className="w-3.5 h-3.5 rounded border-slate-300 text-indigo-600"
                />
                نامحدود
              </label>
            </div>
            <input
              type="number"
              min="1"
              disabled={isUnlimitedUses}
              value={maxUses}
              onChange={(e) => setMaxUses(e.target.value)}
              placeholder="مثال: ۱۰۰"
              className="w-full px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-sm font-mono disabled:opacity-50 disabled:bg-slate-100 dark:disabled:bg-slate-900/50"
            />
          </div>

          <div className="flex flex-col justify-center">
            <span className="text-xs font-semibold text-slate-800 dark:text-slate-200 mb-1.5 flex items-center gap-1">
              <Users className="w-3.5 h-3.5 text-indigo-500" />
              محدودیت هر کاربر
            </span>
            <label className="flex items-center gap-2 p-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] cursor-pointer hover:border-indigo-300 transition-colors">
              <input
                type="checkbox"
                checked={isOncePerUser}
                onChange={(e) => setIsOncePerUser(e.target.checked)}
                className="w-4 h-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
              />
              <span className="text-xs text-slate-700 dark:text-slate-300 font-medium">
                فقط ۱ بار برای هر کاربر (یک‌بار مصرف)
              </span>
            </label>
          </div>
        </div>

        {/* Date Window */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              تاریخ و زمان شروع اعتبار
            </label>
            <input
              type="datetime-local"
              value={startsAt}
              onChange={(e) => setStartsAt(e.target.value)}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-xs font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              تاریخ و زمان انقضا
            </label>
            <input
              type="datetime-local"
              value={expiresAt}
              onChange={(e) => setExpiresAt(e.target.value)}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-xs font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            />
          </div>
        </div>

        {/* Active Toggle Switch */}
        <div className="flex items-center justify-between p-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/30">
          <div>
            <div className="text-xs font-bold text-slate-900 dark:text-white">وضعیت فعالیت کد</div>
            <p className="text-[11px] text-slate-500 dark:text-slate-400">
              در صورت غیرفعال بودن، کاربران قادر به اعمال این کد نخواهند بود.
            </p>
          </div>
          <button
            type="button"
            onClick={() => setIsActive(!isActive)}
            className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${
              isActive ? 'bg-emerald-600' : 'bg-slate-300 dark:bg-slate-700'
            }`}
          >
            <span
              className={`inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out ${
                isActive ? 'translate-x-5' : 'translate-x-0'
              }`}
            />
          </button>
        </div>

        {/* Footer Actions */}
        <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-200 dark:border-slate-800">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            انصراف
          </Button>
          <Button type="submit" variant="primary" isLoading={isSubmitting}>
            {isEdit ? 'ذخیره تغییرات' : 'ثبت کد تخفیف'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
