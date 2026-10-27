import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { PromoCodeItem, PromoCodeUpsertPayload, PromoStats } from '../types';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { Switch } from '../components/ui/Switch';
import { PromoCodeModal } from '../components/promo/PromoCodeModal';
import { PromoRedemptionsModal } from '../components/promo/PromoRedemptionsModal';
import {
  TicketPercent,
  Plus,
  Search,
  Filter,
  Copy,
  Check,
  Edit3,
  Trash2,
  Users,
  CheckCircle2,
  Clock,
  Coins,
  AlertTriangle,
  Percent,
} from 'lucide-react';

export const PromoCodesPage: React.FC = () => {
  const { success, error, info } = useToast();

  const [promoCodes, setPromoCodes] = useState<PromoCodeItem[]>([]);
  const [stats, setStats] = useState<PromoStats>({
    totalCodes: 0,
    activeCodes: 0,
    totalRedemptions: 0,
    totalDiscountGiven: 0,
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Modals state
  const [isUpsertModalOpen, setIsUpsertModalOpen] = useState(false);
  const [editingPromo, setEditingPromo] = useState<PromoCodeItem | null>(null);

  const [isRedemptionsModalOpen, setIsRedemptionsModalOpen] = useState(false);
  const [selectedPromoForRedemptions, setSelectedPromoForRedemptions] = useState<PromoCodeItem | null>(null);

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [promoToDelete, setPromoToDelete] = useState<PromoCodeItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Copy feedback tracking
  const [copiedCodeId, setCopiedCodeId] = useState<string | null>(null);

  // Toggling status tracking
  const [togglingId, setTogglingId] = useState<string | null>(null);

  const fetchPromoCodes = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await api.getPromoCodes(searchQuery.trim(), statusFilter);
      setPromoCodes(res.items);
      setStats(res.stats);
    } catch (err: any) {
      error(err.message || 'خطا در دریافت لیست کدهای تخفیف');
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, statusFilter, error]);

  useEffect(() => {
    fetchPromoCodes();
  }, [fetchPromoCodes]);

  const handleCopyCode = (id: string, code: string) => {
    navigator.clipboard.writeText(code);
    setCopiedCodeId(id);
    info(`کد «${code}» در کلیپ‌بورد کپی شد`);
    setTimeout(() => {
      setCopiedCodeId(null);
    }, 2000);
  };

  const handleToggleStatus = async (item: PromoCodeItem) => {
    const newStatus = !item.isActive;
    setTogglingId(item.id);
    // Optimistic update
    setPromoCodes((prev) =>
      prev.map((p) => (p.id === item.id ? { ...p, isActive: newStatus } : p))
    );

    try {
      await api.togglePromoCodeStatus(item.id, newStatus);
      success(`وضعیت کد «${item.code}» با موفقیت به ${newStatus ? 'فعال' : 'غیرفعال'} تغییر یافت`);
      fetchPromoCodes();
    } catch (err: any) {
      // Revert on error
      setPromoCodes((prev) =>
        prev.map((p) => (p.id === item.id ? { ...p, isActive: item.isActive } : p))
      );
      error(err.message || 'خطا در تغییر وضعیت کد تخفیف');
    } finally {
      setTogglingId(null);
    }
  };

  const handleSavePromo = async (payload: PromoCodeUpsertPayload) => {
    if (editingPromo) {
      await api.updatePromoCode(editingPromo.id, payload);
      success(`کد تخفیف «${payload.code}» با موفقیت به‌روزرسانی شد`);
    } else {
      await api.createPromoCode(payload);
      success(`کد تخفیف جدید «${payload.code}» با موفقیت ایجاد شد`);
    }
    fetchPromoCodes();
  };

  const handleDeleteConfirm = async () => {
    if (!promoToDelete) return;
    setIsDeleting(true);
    try {
      await api.deletePromoCode(promoToDelete.id);
      success(`کد تخفیف «${promoToDelete.code}» با موفقیت بایگانی/حذف شد`);
      setIsDeleteModalOpen(false);
      setPromoToDelete(null);
      fetchPromoCodes();
    } catch (err: any) {
      error(err.message || 'خطا در حذف کد تخفیف');
    } finally {
      setIsDeleting(false);
    }
  };

  const getPlanName = (p: string) => {
    if (p === 'plan-1m') return '۱ ماهه';
    if (p === 'plan-3m') return '۳ ماهه';
    if (p === 'plan-6m') return '۶ ماهه';
    return p;
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 dark:text-[#f1f3f5] tracking-tight">
            کدهای تخفیف
          </h1>
          <Badge variant="purple" size="md">
            {stats.activeCodes} کد فعال
          </Badge>
        </div>

        <Button
          variant="primary"
          onClick={() => {
            setEditingPromo(null);
            setIsUpsertModalOpen(true);
          }}
          className="gap-2 shrink-0 shadow-2xs"
        >
          <Plus className="w-4 h-4" />
          کد تخفیف جدید
        </Button>
      </div>

      {/* Metric Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
        <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-4 shadow-2xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-[#9ca3af]">
              کل کدهای ثبت‌شده
            </span>
            <div className="w-8 h-8 rounded-xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-indigo-600 dark:text-indigo-400">
              <TicketPercent className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900 dark:text-[#f1f3f5] mt-2 font-mono">
            {stats.totalCodes.toLocaleString('fa-IR')}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">کدهای فعال و منقضی</div>
        </div>

        <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-4 shadow-2xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-[#9ca3af]">
              کدهای معتبر و فعال
            </span>
            <div className="w-8 h-8 rounded-xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-emerald-600 dark:text-emerald-400">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400 mt-2 font-mono">
            {stats.activeCodes.toLocaleString('fa-IR')}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">آماده اعمال در کلاینت</div>
        </div>

        <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-4 shadow-2xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-[#9ca3af]">
              کل دفعات استفاده
            </span>
            <div className="w-8 h-8 rounded-xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-blue-600 dark:text-blue-400">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900 dark:text-[#f1f3f5] mt-2 font-mono">
            {stats.totalRedemptions.toLocaleString('fa-IR')}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">اشتراک خریداری‌شده با تخفیف</div>
        </div>

        <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-4 shadow-2xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-[#9ca3af]">
              مجموع تخفیف اعطایی
            </span>
            <div className="w-8 h-8 rounded-xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-amber-600 dark:text-amber-400">
              <Coins className="w-4 h-4" />
            </div>
          </div>
          <div className="text-xl font-bold text-amber-600 dark:text-amber-400 mt-2 font-mono truncate">
            {stats.totalDiscountGiven.toLocaleString('fa-IR')}{' '}
            <span className="text-xs font-sans font-normal text-slate-400">تومان</span>
          </div>
          <div className="text-[11px] text-slate-400 mt-1">سود کاربران از تخفیف‌ها</div>
        </div>
      </div>

      {/* Filter and Search Toolbar */}
      <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-3 sm:p-4 shadow-2xs flex flex-col md:flex-row items-center justify-between gap-3">
        {/* Status Filter Tabs */}
        <div className="flex items-center gap-1.5 w-full md:w-auto overflow-x-auto pb-1 md:pb-0">
          <span className="text-xs font-semibold text-slate-400 dark:text-[#858a94] ml-1 hidden sm:inline-flex items-center gap-1 uppercase tracking-wider">
            <Filter className="w-3.5 h-3.5 text-indigo-500" /> فیلتر:
          </span>
          {[
            { id: 'ALL', label: 'همه کدها' },
            { id: 'ACTIVE', label: 'فعال' },
            { id: 'EXPIRED', label: 'منقضی‌شده' },
            { id: 'INACTIVE', label: 'غیرفعال' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setStatusFilter(tab.id)}
              className={`px-3 py-1.5 rounded-xl text-xs font-medium transition-all shrink-0 ${
                statusFilter === tab.id
                  ? 'bg-indigo-50 text-indigo-700 dark:bg-[#282a30] dark:text-[#f1f3f5] font-bold border border-indigo-200 dark:border-[#383b44] shadow-2xs'
                  : 'text-slate-600 dark:text-[#9ca3af] hover:bg-slate-100 dark:hover:bg-[#24262b]'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Search Input */}
        <div className="relative w-full md:w-72">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="جستجوی کد یا عنوان تخفیف..."
            className="w-full pl-3 pr-9 py-2 rounded-xl border border-slate-200 dark:border-[#363842] bg-slate-50 dark:bg-[#24262b] text-slate-900 dark:text-[#f1f3f5] text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all placeholder:text-slate-400 dark:placeholder-[#858a94]"
          />
          <Search className="w-4 h-4 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2" />
        </div>
      </div>

      {/* Main Table / List */}
      <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl shadow-2xs overflow-hidden">
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center gap-3 text-slate-400">
            <div className="w-9 h-9 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
            <span className="text-xs font-medium">در حال بارگذاری کدهای تخفیف...</span>
          </div>
        ) : promoCodes.length === 0 ? (
          <div className="py-20 text-center text-slate-400">
            <TicketPercent className="w-12 h-12 mx-auto text-slate-300 dark:text-slate-700 mb-3" />
            <h3 className="text-sm font-bold text-slate-700 dark:text-slate-300">
              هیچ کد تخفیفی یافت نشد
            </h3>
            <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
              {searchQuery || statusFilter !== 'ALL'
                ? 'کدی متناسب با فیلترها و جستجوی شما پیدا نشد. فیلترها را ریست کنید.'
                : 'هنوز هیچ کد تخفیفی ایجاد نشده است. با زدن دکمه «کد تخفیف جدید» اولین کد را تعریف کنید.'}
            </p>
            {!searchQuery && statusFilter === 'ALL' && (
              <Button
                variant="primary"
                size="sm"
                className="mt-4 gap-1.5"
                onClick={() => {
                  setEditingPromo(null);
                  setIsUpsertModalOpen(true);
                }}
              >
                <Plus className="w-4 h-4" />
                تعریف اولین کد تخفیف
              </Button>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-right text-xs">
              <thead className="bg-slate-50/80 dark:bg-[#1b1c1f] border-b border-slate-200 dark:border-[#2d3037] text-slate-500 dark:text-[#9ca3af] font-semibold select-none">
                <tr>
                  <th className="py-3.5 px-4">کد تخفیف</th>
                  <th className="py-3.5 px-4">نوع و میزان تخفیف</th>
                  <th className="py-3.5 px-4">پلن‌های مجاز</th>
                  <th className="py-3.5 px-4">میزان مصرف</th>
                  <th className="py-3.5 px-4">مهلت انقضا</th>
                  <th className="py-3.5 px-4 text-center">وضعیت</th>
                  <th className="py-3.5 px-4 text-left">عملیات</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-[#2d3037]">
                {promoCodes.map((item) => {
                  const isExpired = item.isExpired;
                  const isNearLimit = item.maxUses != null && item.usedCount >= item.maxUses;
                  const usagePercent =
                    item.maxUses && item.maxUses > 0
                      ? Math.min(100, Math.round((item.usedCount / item.maxUses) * 100))
                      : 0;

                  return (
                    <tr
                      key={item.id}
                      className="hover:bg-slate-50/70 dark:hover:bg-[#282a30] transition-colors"
                    >
                      {/* Code & Title */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() => handleCopyCode(item.id, item.code)}
                            className="group flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-100 dark:bg-slate-800/80 border border-slate-200/80 dark:border-slate-700/60 hover:border-indigo-400 dark:hover:border-indigo-500/50 transition-all font-mono font-bold text-slate-900 dark:text-white text-xs tracking-wider"
                            title="کلیک برای کپی کردن کد"
                          >
                            <span>{item.code}</span>
                            {copiedCodeId === item.id ? (
                              <Check className="w-3.5 h-3.5 text-emerald-500" />
                            ) : (
                              <Copy className="w-3.5 h-3.5 text-slate-400 group-hover:text-indigo-500 transition-colors" />
                            )}
                          </button>
                          {item.isOncePerUser && (
                            <span className="text-[10px] text-slate-400 bg-slate-100 dark:bg-slate-800 px-1.5 py-0.5 rounded border border-slate-200 dark:border-slate-700">
                              یک‌بار مصرف
                            </span>
                          )}
                        </div>
                        {item.title && (
                          <div className="text-[11px] text-slate-500 dark:text-slate-400 mt-1 max-w-xs truncate">
                            {item.title}
                          </div>
                        )}
                      </td>

                      {/* Discount Value & Rules */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-1.5">
                          {item.discountType === 'PERCENTAGE' ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md font-bold text-xs bg-indigo-50 text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-500/30">
                              <Percent className="w-3 h-3" />
                              {item.discountValue}٪ تخفیف
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md font-bold text-xs bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300 border border-amber-200 dark:border-amber-500/30">
                              <Coins className="w-3 h-3" />
                              {item.discountValue.toLocaleString('fa-IR')} تومان
                            </span>
                          )}
                        </div>
                        {item.discountType === 'PERCENTAGE' && item.maxDiscountCap && (
                          <div className="text-[10px] text-slate-400 mt-1">
                            سقف: {item.maxDiscountCap.toLocaleString('fa-IR')} ت
                          </div>
                        )}
                        {item.minOrderAmount && (
                          <div className="text-[10px] text-slate-400 mt-0.5">
                            حداقل سفارش: {item.minOrderAmount.toLocaleString('fa-IR')} ت
                          </div>
                        )}
                      </td>

                      {/* Applicable Plans */}
                      <td className="py-3.5 px-4">
                        {item.applicablePlans.length === 0 ? (
                          <span className="text-[11px] text-slate-600 dark:text-slate-400 bg-slate-100 dark:bg-slate-800/60 px-2 py-0.5 rounded-md">
                            همه پلن‌ها
                          </span>
                        ) : (
                          <div className="flex flex-wrap gap-1 max-w-[180px]">
                            {item.applicablePlans.map((p) => (
                              <span
                                key={p}
                                className="text-[10px] bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 px-1.5 py-0.5 rounded border border-slate-200 dark:border-slate-700"
                              >
                                {getPlanName(p)}
                              </span>
                            ))}
                          </div>
                        )}
                      </td>

                      {/* Usage Progress */}
                      <td className="py-3.5 px-4">
                        <div className="space-y-1">
                          <div className="flex items-center justify-between text-[11px]">
                            <span className="font-semibold text-slate-900 dark:text-white font-mono">
                              {item.usedCount.toLocaleString('fa-IR')}
                            </span>
                            <span className="text-slate-400">
                              از {item.maxUses != null ? item.maxUses.toLocaleString('fa-IR') : 'نامحدود'}
                            </span>
                          </div>
                          {item.maxUses != null && (
                            <div className="w-28 h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                              <div
                                className={`h-full rounded-full transition-all duration-300 ${
                                  isNearLimit ? 'bg-red-500' : 'bg-indigo-500'
                                }`}
                                style={{ width: `${usagePercent}%` }}
                              />
                            </div>
                          )}
                        </div>
                      </td>

                      {/* Expiration Date */}
                      <td className="py-3.5 px-4">
                        {item.expiresAt ? (
                          <div className="space-y-0.5">
                            <div
                              className={`flex items-center gap-1 text-[11px] font-medium ${
                                isExpired
                                  ? 'text-red-600 dark:text-red-400'
                                  : 'text-slate-700 dark:text-slate-300'
                              }`}
                            >
                              <Clock className="w-3.5 h-3.5" />
                              <span>{new Date(item.expiresAt).toLocaleDateString('fa-IR')}</span>
                            </div>
                            {isExpired && (
                              <span className="inline-block text-[10px] text-red-500 font-medium">
                                منقضی شده
                              </span>
                            )}
                          </div>
                        ) : (
                          <span className="text-[11px] text-slate-400">نامحدود (دائمی)</span>
                        )}
                      </td>

                      <td className="py-3.5 px-4 text-center">
                        <div className="flex justify-center items-center">
                          <Switch
                            size="sm"
                            checked={item.isActive}
                            disabled={togglingId === item.id}
                            onChange={() => handleToggleStatus(item)}
                            title={
                              isExpired
                                ? (item.isActive ? 'کد منقضی است (کلیک برای غیرفعال‌سازی)' : 'کد منقضی و غیرفعال است (کلیک برای فعال‌سازی)')
                                : (item.isActive ? 'کلیک برای غیرفعال‌سازی' : 'کلیک برای فعال‌سازی')
                            }
                          />
                        </div>
                      </td>

                      {/* Actions */}
                      <td className="py-3.5 px-4 text-left">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            type="button"
                            onClick={() => {
                              setSelectedPromoForRedemptions(item);
                              setIsRedemptionsModalOpen(true);
                            }}
                            className="p-1.5 rounded-lg text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 dark:hover:bg-indigo-500/15 dark:hover:text-indigo-300 transition-colors"
                            title="مشاهده کاربران استفاده‌کننده"
                          >
                            <Users className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setEditingPromo(item);
                              setIsUpsertModalOpen(true);
                            }}
                            className="p-1.5 rounded-lg text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 dark:hover:bg-indigo-500/15 dark:hover:text-indigo-300 transition-colors"
                            title="ویرایش کد تخفیف"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setPromoToDelete(item);
                              setIsDeleteModalOpen(true);
                            }}
                            className="p-1.5 rounded-lg text-slate-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-500/15 dark:hover:text-red-400 transition-colors"
                            title="بایگانی / حذف کد"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create / Edit Modal */}
      <PromoCodeModal
        isOpen={isUpsertModalOpen}
        onClose={() => {
          setIsUpsertModalOpen(false);
          setEditingPromo(null);
        }}
        onSave={handleSavePromo}
        initialData={editingPromo}
      />

      {/* Redemptions History Modal */}
      <PromoRedemptionsModal
        isOpen={isRedemptionsModalOpen}
        onClose={() => {
          setIsRedemptionsModalOpen(false);
          setSelectedPromoForRedemptions(null);
        }}
        promoCode={selectedPromoForRedemptions}
      />

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => {
          if (!isDeleting) {
            setIsDeleteModalOpen(false);
            setPromoToDelete(null);
          }
        }}
        title="تایید بایگانی و حذف کد تخفیف"
        maxWidth="md"
      >
        <div className="space-y-4">
          <div className="flex items-start gap-3 p-3.5 rounded-xl bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/20 text-amber-800 dark:text-amber-300 text-xs">
            <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5 text-amber-600 dark:text-amber-400" />
            <div className="leading-relaxed">
              آیا از حذف کد تخفیف <strong className="font-mono text-sm">{promoToDelete?.code}</strong> مطمئن هستید؟
              این عملیات به صورت حذف نرم انجام شده و سوابق خریدهای قبلی کاربران حفظ خواهد شد.
            </div>
          </div>

          <div className="flex justify-end gap-2.5 pt-2">
            <Button
              variant="outline"
              disabled={isDeleting}
              onClick={() => {
                setIsDeleteModalOpen(false);
                setPromoToDelete(null);
              }}
            >
              انصراف
            </Button>
            <Button
              variant="danger"
              isLoading={isDeleting}
              onClick={handleDeleteConfirm}
            >
              بایگانی و حذف کد
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
