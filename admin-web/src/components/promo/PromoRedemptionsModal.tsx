import React, { useEffect, useState } from 'react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { api } from '../../api/client';
import { PromoCodeItem, PromoRedemptionItem } from '../../types';
import {
  Users,
  Clock,
  Search,
  AlertCircle,
} from 'lucide-react';

interface PromoRedemptionsModalProps {
  isOpen: boolean;
  onClose: () => void;
  promoCode: PromoCodeItem | null;
}

const PLAN_LABELS: Record<string, string> = {
  'plan-1m': 'اشتراک ۱ ماهه',
  'plan-3m': 'اشتراک ۳ ماهه',
  'plan-6m': 'اشتراک ۶ ماهه',
  '1_MONTH': 'اشتراک ۱ ماهه',
  '3_MONTHS': 'اشتراک ۳ ماهه',
  '6_MONTHS': 'اشتراک ۶ ماهه',
};

export const PromoRedemptionsModal: React.FC<PromoRedemptionsModalProps> = ({
  isOpen,
  onClose,
  promoCode,
}) => {
  const [redemptions, setRedemptions] = useState<PromoRedemptionItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    if (isOpen && promoCode) {
      fetchRedemptions(promoCode.id);
    } else {
      setRedemptions([]);
      setSearchQuery('');
      setErrorMsg(null);
    }
  }, [isOpen, promoCode]);

  const fetchRedemptions = async (id: string) => {
    setIsLoading(true);
    setErrorMsg(null);
    try {
      const data = await api.getPromoRedemptions(id);
      setRedemptions(data);
    } catch (err: any) {
      setErrorMsg(err.message || 'خطا در دریافت لیست سوابق استفاده');
    } finally {
      setIsLoading(false);
    }
  };

  if (!promoCode) return null;

  const filtered = redemptions.filter((r) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      r.phoneNumber.toLowerCase().includes(q) ||
      (r.nickName && r.nickName.toLowerCase().includes(q))
    );
  });

  const totalDiscount = redemptions.reduce((acc, curr) => acc + curr.discountAmount, 0);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`سوابق استفاده از کد «${promoCode.code}»`}
      description="مشاهده لیست کاربرانی که از این کد تخفیف برای خرید اشتراک استفاده کرده‌اند"
      maxWidth="4xl"
    >
      <div className="space-y-4">
        {/* Top Summary Chips */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-900/40 border border-slate-200 dark:border-slate-800">
          <div>
            <span className="text-[11px] text-slate-500 dark:text-slate-400">کد تخفیف:</span>
            <div className="font-mono font-bold text-slate-900 dark:text-white text-base mt-0.5">
              {promoCode.code}
            </div>
          </div>
          <div>
            <span className="text-[11px] text-slate-500 dark:text-slate-400">کل دفعات استفاده‌شده:</span>
            <div className="font-semibold text-slate-900 dark:text-white text-base mt-0.5">
              {redemptions.length} مرتبه
            </div>
          </div>
          <div>
            <span className="text-[11px] text-slate-500 dark:text-slate-400">مجموع تخفیف اعطاشده:</span>
            <div className="font-semibold text-emerald-600 dark:text-emerald-400 text-base mt-0.5">
              {totalDiscount.toLocaleString('fa-IR')} تومان
            </div>
          </div>
        </div>

        {/* Search */}
        <div className="relative">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="جستجو بر اساس شماره موبایل یا نام کاربر..."
            className="w-full pl-3 pr-9 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all placeholder:text-slate-400"
          />
          <Search className="w-4 h-4 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2" />
        </div>

        {/* Content Area */}
        {isLoading ? (
          <div className="py-12 flex flex-col items-center justify-center gap-3 text-slate-400">
            <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
            <span className="text-xs">در حال بارگذاری سوابق...</span>
          </div>
        ) : errorMsg ? (
          <div className="p-4 rounded-xl bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        ) : filtered.length === 0 ? (
          <div className="py-12 text-center text-slate-400 border border-dashed border-slate-200 dark:border-slate-800 rounded-2xl">
            <Users className="w-10 h-10 mx-auto text-slate-300 dark:text-slate-600 mb-2" />
            <p className="text-sm font-medium text-slate-600 dark:text-slate-400">
              هنوز هیچ کاربری از این کد تخفیف استفاده نکرده است.
            </p>
            <p className="text-xs text-slate-400 mt-1">
              پس از اولین خرید موفق با این کد، مشخصات خریدار در این جدول ثبت خواهد شد.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
            <table className="w-full text-right text-xs">
              <thead className="bg-slate-50 dark:bg-[#0E1422] border-b border-slate-200 dark:border-slate-800 text-slate-500 dark:text-slate-400 font-semibold select-none">
                <tr>
                  <th className="py-3 px-4">کاربر خریدار</th>
                  <th className="py-3 px-4">پلن خریداری‌شده</th>
                  <th className="py-3 px-4">مبلغ اولیه</th>
                  <th className="py-3 px-4">تخفیف کسرشده</th>
                  <th className="py-3 px-4">مبلغ نهایی پرداختی</th>
                  <th className="py-3 px-4">زمان ثبت استفاده</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60">
                {filtered.map((item) => (
                  <tr
                    key={item.id}
                    className="hover:bg-slate-50/80 dark:hover:bg-slate-800/30 transition-colors"
                  >
                    <td className="py-3 px-4">
                      <div className="font-medium text-slate-900 dark:text-white">
                        {item.nickName || 'کاربر زبان‌آموز'}
                      </div>
                      <div className="font-mono text-[11px] text-slate-400 mt-0.5" dir="ltr">
                        {item.phoneNumber}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-medium bg-indigo-50 text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-300 border border-indigo-200/60 dark:border-indigo-500/30">
                        {PLAN_LABELS[item.planId] || item.planId}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-500">
                      {item.originalPrice.toLocaleString('fa-IR')} ت
                    </td>
                    <td className="py-3 px-4 font-mono text-red-600 dark:text-red-400 font-medium">
                      -{item.discountAmount.toLocaleString('fa-IR')} ت
                    </td>
                    <td className="py-3 px-4 font-mono text-emerald-600 dark:text-emerald-400 font-bold">
                      {item.finalPrice.toLocaleString('fa-IR')} ت
                    </td>
                    <td className="py-3 px-4 text-slate-400">
                      <div className="flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-slate-400" />
                        <span>{new Date(item.redeemedAt).toLocaleDateString('fa-IR')}</span>
                        <span className="text-[10px] text-slate-400 font-mono">
                          {new Date(item.redeemedAt).toLocaleTimeString('fa-IR', {
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Footer */}
        <div className="flex justify-end pt-2">
          <Button variant="outline" onClick={onClose}>
            بستن
          </Button>
        </div>
      </div>
    </Modal>
  );
};
