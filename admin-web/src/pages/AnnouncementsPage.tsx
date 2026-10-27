import React, { useState, useEffect } from 'react';
import {
  Megaphone,
  Plus,
  RefreshCw,
  Edit2,
  Trash2,
  Info,
  AlertTriangle,
  AlertOctagon,
  Sparkles,
  X,
  CheckCircle2,
  ShieldAlert,
  Image as ImageIcon,
} from 'lucide-react';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import {
  AnnouncementItem,
  AnnouncementType,
  AnnouncementActionType,
  AnnouncementUpsertPayload,
} from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { Switch } from '../components/ui/Switch';

export const AnnouncementsPage: React.FC = () => {
  const { error: toastError, success: toastSuccess } = useToast();

  const [announcements, setAnnouncements] = useState<AnnouncementItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);

  // Form State
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [type, setType] = useState<AnnouncementType>('INFO');
  const [imageUrl, setImageUrl] = useState('');
  const [isBlocking, setIsBlocking] = useState(false);
  const [isActive, setIsActive] = useState(false);

  // Primary Button
  const [primaryText, setPrimaryText] = useState('فهمیدم');
  const [primaryAction, setPrimaryAction] = useState<AnnouncementActionType>('CLOSE');
  const [primaryValue, setPrimaryValue] = useState('');

  // Secondary Button
  const [hasSecondary, setHasSecondary] = useState(false);
  const [secondaryText, setSecondaryText] = useState('');
  const [secondaryAction, setSecondaryAction] = useState<AnnouncementActionType>('CLOSE');
  const [secondaryValue, setSecondaryValue] = useState('');

  const fetchAnnouncements = async () => {
    setIsLoading(true);
    try {
      const res = await api.getAnnouncements();
      setAnnouncements(res.announcements || []);
    } catch (e: any) {
      toastError(e.message || 'خطا در بارگذاری اطلاعیه‌ها');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAnnouncements();
  }, []);

  const resetForm = () => {
    setEditingId(null);
    setTitle('');
    setMessage('');
    setType('INFO');
    setImageUrl('');
    setIsBlocking(false);
    setIsActive(false);

    setPrimaryText('فهمیدم');
    setPrimaryAction('CLOSE');
    setPrimaryValue('');

    setHasSecondary(false);
    setSecondaryText('');
    setSecondaryAction('CLOSE');
    setSecondaryValue('');
  };

  const handleOpenCreate = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const handleOpenEdit = (item: AnnouncementItem) => {
    setEditingId(item.id);
    setTitle(item.title);
    setMessage(item.message);
    setType(item.type);
    setImageUrl(item.imageUrl || '');
    setIsBlocking(item.isBlocking);
    setIsActive(item.isActive);

    setPrimaryText(item.primaryButtonText || 'فهمیدم');
    setPrimaryAction(item.primaryButtonAction || 'CLOSE');
    setPrimaryValue(item.primaryButtonValue || '');

    if (item.secondaryButtonText) {
      setHasSecondary(true);
      setSecondaryText(item.secondaryButtonText);
      setSecondaryAction(item.secondaryButtonAction || 'CLOSE');
      setSecondaryValue(item.secondaryButtonValue || '');
    } else {
      setHasSecondary(false);
      setSecondaryText('');
      setSecondaryAction('CLOSE');
      setSecondaryValue('');
    }

    setIsModalOpen(true);
  };

  const handleToggleActive = async (id: string) => {
    try {
      const updated = await api.toggleAnnouncementActive(id);
      toastSuccess(updated.isActive ? 'اطلاعیه فعال شد' : 'اطلاعیه غیرفعال شد');
      await fetchAnnouncements();
    } catch (e: any) {
      toastError(e.message || 'خطا در تغییر وضعیت اطلاعیه');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('آیا از حذف این اطلاعیه مطمئن هستید؟')) return;
    try {
      await api.deleteAnnouncement(id);
      toastSuccess('اطلاعیه با موفقیت حذف شد');
      await fetchAnnouncements();
    } catch (e: any) {
      toastError(e.message || 'خطا در حذف اطلاعیه');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !message.trim()) {
      toastError('لطفاً عنوان و متن اطلاعیه را وارد کنید');
      return;
    }

    const payload: AnnouncementUpsertPayload = {
      title: title.trim(),
      message: message.trim(),
      type,
      imageUrl: imageUrl.trim() || null,
      isBlocking,
      isActive,
      primaryButtonText: primaryText.trim() || 'فهمیدم',
      primaryButtonAction: primaryAction,
      primaryButtonValue: primaryValue.trim() || null,
      secondaryButtonText: hasSecondary && secondaryText.trim() ? secondaryText.trim() : null,
      secondaryButtonAction: hasSecondary && secondaryText.trim() ? secondaryAction : null,
      secondaryButtonValue: hasSecondary && secondaryText.trim() ? secondaryValue.trim() || null : null,
    };

    setIsSaving(true);
    try {
      if (editingId) {
        await api.updateAnnouncement(editingId, payload);
        toastSuccess('اطلاعیه با موفقیت ویرایش شد');
      } else {
        await api.createAnnouncement(payload);
        toastSuccess('اطلاعیه جدید با موفقیت ایجاد شد');
      }
      setIsModalOpen(false);
      await fetchAnnouncements();
    } catch (e: any) {
      toastError(e.message || 'خطا در ذخیره اطلاعیه');
    } finally {
      setIsSaving(false);
    }
  };

  const getTypeColor = (t: AnnouncementType) => {
    switch (t) {
      case 'WARNING':
        return {
          border: 'border-amber-500/30',
          bg: 'bg-amber-500/10',
          text: 'text-amber-400',
          icon: AlertTriangle,
          label: 'هشدار / اخطار',
          glow: 'from-amber-500/20 to-transparent',
        };
      case 'ERROR':
        return {
          border: 'border-rose-500/30',
          bg: 'bg-rose-500/10',
          text: 'text-rose-400',
          icon: AlertOctagon,
          label: 'اختلال / قطعی',
          glow: 'from-rose-500/20 to-transparent',
        };
      case 'SUCCESS':
        return {
          border: 'border-emerald-500/30',
          bg: 'bg-emerald-500/10',
          text: 'text-emerald-400',
          icon: Sparkles,
          label: 'تخفیف / مژده',
          glow: 'from-emerald-500/20 to-transparent',
        };
      case 'INFO':
      default:
        return {
          border: 'border-cyan-500/30',
          bg: 'bg-cyan-500/10',
          text: 'text-cyan-400',
          icon: Info,
          label: 'اطلاع‌رسانی',
          glow: 'from-cyan-500/20 to-transparent',
        };
    }
  };

  const getActionLabel = (act: AnnouncementActionType) => {
    switch (act) {
      case 'OPEN_URL':
        return 'باز کردن لینک خارجی';
      case 'NAVIGATE':
        return 'هدایت به صفحه اپ';
      case 'RELOAD':
        return 'ریفرش برنامه';
      case 'CLOSE':
      default:
        return 'بستن دیالوگ';
    }
  };

  const activeAnnouncement = announcements.find((a) => a.isActive);

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12 font-sans" dir="rtl">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-slate-900/60 p-6 rounded-2xl border border-slate-800/80 backdrop-blur-sm">
        <div>
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-gradient-to-br from-cyan-500/20 to-blue-500/20 border border-cyan-500/30 text-cyan-400 shadow-lg shadow-cyan-500/10">
              <Megaphone className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white tracking-tight">پیام‌ها و اطلاعیه‌های درون‌برنامه‌ای</h1>
              <p className="text-sm text-slate-400 mt-0.5">
                تنظیم دیالوگ‌های سراسری، هشدارهای اختلال، اطلاعیه‌ها و پیام‌های نگهداری در بدو ورود کاربران
              </p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto">
          <Button
            variant="outline"
            onClick={fetchAnnouncements}
            disabled={isLoading}
            className="flex-1 sm:flex-initial border-slate-700 hover:bg-slate-800 text-slate-300"
          >
            <RefreshCw className={`w-4 h-4 ml-2 ${isLoading ? 'animate-spin' : ''}`} />
            بروزرسانی
          </Button>
          <Button
            onClick={handleOpenCreate}
            className="flex-1 sm:flex-initial bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-medium shadow-lg shadow-cyan-500/20"
          >
            <Plus className="w-4 h-4 ml-2" />
            ایجاد اطلاعیه جدید
          </Button>
        </div>
      </div>

      {/* Active Announcement Highlight */}
      {activeAnnouncement ? (
        <div className="relative overflow-hidden rounded-2xl border border-emerald-500/30 bg-gradient-to-r from-emerald-950/40 via-slate-900/80 to-slate-900 p-6 shadow-xl">
          <div className="absolute top-0 right-0 w-64 h-64 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
            <div className="flex items-start gap-4">
              <div className="p-3 rounded-2xl bg-emerald-500/20 border border-emerald-500/40 text-emerald-400 mt-1">
                <CheckCircle2 className="w-6 h-6 animate-pulse" />
              </div>
              <div className="space-y-1.5">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    هم‌اکنون فعال در اپلیکیشن
                  </span>
                  {activeAnnouncement.isBlocking && (
                    <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-rose-500/20 text-rose-300 border border-rose-500/30 flex items-center gap-1">
                      <ShieldAlert className="w-3 h-3" />
                      مسدودکننده (اجباری)
                    </span>
                  )}
                  <span className="text-xs text-slate-400">
                    نوع: {getTypeColor(activeAnnouncement.type).label}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-white">{activeAnnouncement.title}</h3>
                <p className="text-sm text-slate-300 line-clamp-2 max-w-3xl leading-relaxed">
                  {activeAnnouncement.message}
                </p>
                <div className="flex items-center gap-4 text-xs text-slate-400 pt-1">
                  <span>دکمه اصلی: {activeAnnouncement.primaryButtonText} ({getActionLabel(activeAnnouncement.primaryButtonAction)})</span>
                  {activeAnnouncement.secondaryButtonText && (
                    <span>دکمه فرعی: {activeAnnouncement.secondaryButtonText} ({getActionLabel(activeAnnouncement.secondaryButtonAction || 'CLOSE')})</span>
                  )}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 self-end md:self-center w-full md:w-auto">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleOpenEdit(activeAnnouncement)}
                className="border-slate-700 hover:bg-slate-800 text-slate-200"
              >
                <Edit2 className="w-4 h-4 ml-1.5" />
                ویرایش
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleToggleActive(activeAnnouncement.id)}
                className="border-amber-500/30 hover:bg-amber-500/10 text-amber-300"
              >
                غیرفعال‌سازی
              </Button>
            </div>
          </div>
        </div>
      ) : (
        <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-5 flex items-center justify-between text-slate-400">
          <div className="flex items-center gap-3">
            <Info className="w-5 h-5 text-slate-500" />
            <span className="text-sm">در حال حاضر هیچ اطلاعیه‌ای در اپلیکیشن فعال نیست (کاربران بدون دیالوگ وارد می‌شوند).</span>
          </div>
        </div>
      )}

      {/* Announcements List */}
      <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 overflow-hidden shadow-xl backdrop-blur-sm">
        <div className="p-4 sm:p-6 border-b border-slate-800/80 flex items-center justify-between">
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <span>تاریخچه و لیست اطلاعیه‌ها</span>
            <span className="text-xs bg-slate-800 text-slate-400 px-2 py-0.5 rounded-full">
              {announcements.length} مورد
            </span>
          </h2>
        </div>

        {announcements.length === 0 ? (
          <div className="p-12 text-center text-slate-400">
            <Megaphone className="w-12 h-12 mx-auto text-slate-600 mb-3" />
            <p className="text-base font-medium text-slate-300">هنوز هیچ اطلاعیه‌ای ثبت نشده است</p>
            <p className="text-sm text-slate-500 mt-1">با کلیک روی دکمه «ایجاد اطلاعیه جدید» اولین پیام را ثبت کنید.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-right text-sm">
              <thead className="bg-slate-800/40 text-slate-400 text-xs border-b border-slate-800/60 uppercase">
                <tr>
                  <th className="py-3.5 px-4 font-medium">وضعیت</th>
                  <th className="py-3.5 px-4 font-medium">عنوان و متن</th>
                  <th className="py-3.5 px-4 font-medium">نوع و رفتار</th>
                  <th className="py-3.5 px-4 font-medium">دکمه‌ها</th>
                  <th className="py-3.5 px-4 font-medium">تاریخ ایجاد</th>
                  <th className="py-3.5 px-4 font-medium text-left">عملیات</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/50">
                {announcements.map((item) => {
                  const typeConf = getTypeColor(item.type);
                  const Icon = typeConf.icon;

                  return (
                    <tr
                      key={item.id}
                      className={`hover:bg-slate-800/30 transition-colors ${
                        item.isActive ? 'bg-emerald-500/[0.03]' : ''
                      }`}
                    >
                      {/* Active Status */}
                      <td className="py-4 px-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          <Switch
                            checked={item.isActive}
                            onChange={() => handleToggleActive(item.id)}
                            size="sm"
                          />
                          <span
                            className={`text-xs font-semibold ${
                              item.isActive ? 'text-emerald-400' : 'text-slate-500'
                            }`}
                          >
                            {item.isActive ? 'فعال' : 'خاموش'}
                          </span>
                        </div>
                      </td>

                      {/* Title & Preview */}
                      <td className="py-4 px-4 max-w-xs">
                        <div className="font-semibold text-white flex items-center gap-2">
                          {item.imageUrl && (
                            <span title="دارای تصویر بنر" className="flex-shrink-0">
                              <ImageIcon className="w-3.5 h-3.5 text-cyan-400" />
                            </span>
                          )}
                          <span className="truncate">{item.title}</span>
                        </div>
                        <p className="text-xs text-slate-400 mt-1 line-clamp-1">{item.message}</p>
                      </td>

                      {/* Type & Blocking */}
                      <td className="py-4 px-4 whitespace-nowrap">
                        <div className="flex flex-col gap-1 items-start">
                          <span
                            className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-md text-xs font-medium border ${typeConf.bg} ${typeConf.text} ${typeConf.border}`}
                          >
                            <Icon className="w-3.5 h-3.5" />
                            {typeConf.label}
                          </span>
                          {item.isBlocking ? (
                            <span className="inline-flex items-center gap-1 text-[11px] text-rose-400">
                              <ShieldAlert className="w-3 h-3" />
                              مسدودکننده (اجباری)
                            </span>
                          ) : (
                            <span className="text-[11px] text-slate-500">عادی (بسته‌شدنی)</span>
                          )}
                        </div>
                      </td>

                      {/* Buttons */}
                      <td className="py-4 px-4 whitespace-nowrap text-xs text-slate-300">
                        <div className="space-y-0.5">
                          <div>
                            <span className="text-cyan-400 font-medium">اصلی:</span> {item.primaryButtonText}
                          </div>
                          {item.secondaryButtonText && (
                            <div className="text-slate-400">
                              <span className="text-slate-300 font-medium">فرعی:</span> {item.secondaryButtonText}
                            </div>
                          )}
                        </div>
                      </td>

                      {/* Date */}
                      <td className="py-4 px-4 whitespace-nowrap text-xs text-slate-400">
                        {item.createdAt ? new Date(item.createdAt).toLocaleDateString('fa-IR') : '—'}
                      </td>

                      {/* Actions */}
                      <td className="py-4 px-4 whitespace-nowrap text-left">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => handleOpenEdit(item)}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-400 hover:bg-slate-800 transition-colors"
                            title="ویرایش"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(item.id)}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                            title="حذف"
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

      {/* Create / Edit Modal with Live Preview */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingId ? 'ویرایش اطلاعیه' : 'ایجاد اطلاعیه جدید درون‌برنامه‌ای'}
        maxWidth="6xl"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
            {/* Form Fields (7 Columns) */}
            <div className="lg:col-span-7 space-y-5">
              {/* Title */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  عنوان اطلاعیه <span className="text-rose-400">*</span>
                </label>
                <Input
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="مثال: اختلال موقت در سرویس یا آپدیت جدید"
                  required
                />
              </div>

              {/* Message */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  متن پیام اطلاعیه <span className="text-rose-400">*</span>
                </label>
                <textarea
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  rows={4}
                  className="w-full bg-slate-950/80 border border-slate-700/80 rounded-xl px-3.5 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500/80 focus:ring-1 focus:ring-cyan-500/50 transition-all resize-y leading-relaxed"
                  placeholder="متن کامل پیام که کاربر پس از باز کردن برنامه در دیالوگ مشاهده می‌کند..."
                  required
                />
              </div>

              {/* Type Selection */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-2">نوع و تم بصری پیام</label>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                  {(['INFO', 'WARNING', 'ERROR', 'SUCCESS'] as AnnouncementType[]).map((t) => {
                    const conf = getTypeColor(t);
                    const Icon = conf.icon;
                    const isSelected = type === t;
                    return (
                      <button
                        key={t}
                        type="button"
                        onClick={() => setType(t)}
                        className={`flex flex-col items-center justify-center p-3 rounded-xl border text-xs font-semibold transition-all ${
                          isSelected
                            ? `${conf.border} ${conf.bg} ${conf.text} ring-2 ring-cyan-500/40 shadow-lg`
                            : 'border-slate-800 bg-slate-900/60 text-slate-400 hover:border-slate-700'
                        }`}
                      >
                        <Icon className="w-5 h-5 mb-1.5" />
                        <span>{conf.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Banner Image URL */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  آدرس تصویر یا بنر بالای دیالوگ (اختیاری)
                </label>
                <Input
                  value={imageUrl}
                  onChange={(e) => setImageUrl(e.target.value)}
                  placeholder="https://... یا /resources/images/..."
                />
              </div>

              {/* Switches: Blocking & Active */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 p-4 rounded-xl bg-slate-950/60 border border-slate-800">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-xs font-bold text-white">حالت مسدودکننده (Blocking)</div>
                    <div className="text-[11px] text-slate-400 mt-0.5">کاربر نمی‌تواند دیالوگ را ببندد</div>
                  </div>
                  <Switch checked={isBlocking} onChange={setIsBlocking} />
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-xs font-bold text-white">وضعیت فعال در اپ</div>
                    <div className="text-[11px] text-slate-400 mt-0.5">نمایش به کاربران به محض باز شدن</div>
                  </div>
                  <Switch checked={isActive} onChange={setIsActive} />
                </div>
              </div>

              {/* Primary Button Settings */}
              <div className="p-4 rounded-xl bg-slate-950/40 border border-slate-800 space-y-3">
                <div className="text-xs font-bold text-cyan-400 flex items-center gap-1.5">
                  <span>تنظیمات دکمه اصلی (الزامی)</span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">متن دکمه</label>
                    <Input
                      value={primaryText}
                      onChange={(e) => setPrimaryText(e.target.value)}
                      placeholder="فهمیدم"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">نوع اکشن دکمه</label>
                    <select
                      value={primaryAction}
                      onChange={(e) => setPrimaryAction(e.target.value as AnnouncementActionType)}
                      className="w-full bg-slate-900 border border-slate-700/80 rounded-xl px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-cyan-500"
                    >
                      <option value="CLOSE">بستن دیالوگ</option>
                      <option value="OPEN_URL">باز کردن لینک خارجی (URL)</option>
                      <option value="NAVIGATE">هدایت به صفحه‌ای از اپ</option>
                      <option value="RELOAD">ریفرش مجدد برنامه</option>
                    </select>
                  </div>
                </div>
                {primaryAction === 'OPEN_URL' && (
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">آدرس اینترنتی (URL)</label>
                    <Input
                      value={primaryValue}
                      onChange={(e) => setPrimaryValue(e.target.value)}
                      placeholder="https://example.com"
                      required
                    />
                  </div>
                )}
                {primaryAction === 'NAVIGATE' && (
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">مقصد درون اپلیکیشن</label>
                    <select
                      value={primaryValue}
                      onChange={(e) => setPrimaryValue(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-700/80 rounded-xl px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-cyan-500"
                    >
                      <option value="">انتخاب صفحه مقصد...</option>
                      <option value="subscription">صفحه خرید اشتراک</option>
                      <option value="profile">صفحه پروفایل کاربر</option>
                      <option value="stages">لیست مراحل مکالمه</option>
                    </select>
                  </div>
                )}
              </div>

              {/* Secondary Button Settings */}
              <div className="p-4 rounded-xl bg-slate-950/40 border border-slate-800 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="text-xs font-bold text-slate-300">دکمه فرعی (اختیاری)</div>
                  <Switch checked={hasSecondary} onChange={setHasSecondary} size="sm" />
                </div>
                {hasSecondary && (
                  <div className="space-y-3 pt-2">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-[11px] text-slate-400 mb-1">متن دکمه فرعی</label>
                        <Input
                          value={secondaryText}
                          onChange={(e) => setSecondaryText(e.target.value)}
                          placeholder="مثلاً: ورود به پشتیبانی"
                          required={hasSecondary}
                        />
                      </div>
                      <div>
                        <label className="block text-[11px] text-slate-400 mb-1">نوع اکشن</label>
                        <select
                          value={secondaryAction}
                          onChange={(e) => setSecondaryAction(e.target.value as AnnouncementActionType)}
                          className="w-full bg-slate-900 border border-slate-700/80 rounded-xl px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-cyan-500"
                        >
                          <option value="CLOSE">بستن دیالوگ</option>
                          <option value="OPEN_URL">باز کردن لینک خارجی (URL)</option>
                          <option value="NAVIGATE">هدایت به صفحه‌ای از اپ</option>
                          <option value="RELOAD">ریفرش مجدد برنامه</option>
                        </select>
                      </div>
                    </div>
                    {secondaryAction === 'OPEN_URL' && (
                      <div>
                        <label className="block text-[11px] text-slate-400 mb-1">آدرس اینترنتی (URL)</label>
                        <Input
                          value={secondaryValue}
                          onChange={(e) => setSecondaryValue(e.target.value)}
                          placeholder="https://t.me/support..."
                          required={hasSecondary}
                        />
                      </div>
                    )}
                    {secondaryAction === 'NAVIGATE' && (
                      <div>
                        <label className="block text-[11px] text-slate-400 mb-1">مقصد درون اپلیکیشن</label>
                        <select
                          value={secondaryValue}
                          onChange={(e) => setSecondaryValue(e.target.value)}
                          className="w-full bg-slate-900 border border-slate-700/80 rounded-xl px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-cyan-500"
                        >
                          <option value="">انتخاب صفحه مقصد...</option>
                          <option value="subscription">صفحه خرید اشتراک</option>
                          <option value="profile">صفحه پروفایل کاربر</option>
                          <option value="stages">لیست مراحل مکالمه</option>
                        </select>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* Live Preview (5 Columns) */}
            <div className="lg:col-span-5 flex flex-col">
              <div className="text-xs font-bold text-slate-400 mb-2 flex items-center justify-between">
                <span>پیش‌نمایش زنده در اپلیکیشن (Live Preview)</span>
                <span className="text-[11px] text-cyan-400">نمای شبیه‌سازی‌شده</span>
              </div>

              <div className="flex-1 bg-[#0A0E17] border border-slate-800 rounded-2xl p-4 flex flex-col items-center justify-center min-h-[380px] relative overflow-hidden shadow-2xl">
                {/* Background blurred app wallpaper effect */}
                <div className="absolute inset-0 bg-gradient-to-b from-cyan-950/20 via-[#0B0F17] to-slate-950 pointer-events-none" />
                <div className="absolute -top-12 -right-12 w-48 h-48 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

                {/* Dialog Mockup */}
                <div className="relative z-10 w-full max-w-sm rounded-2xl bg-[#131926]/95 border border-slate-700/70 p-5 shadow-2xl backdrop-blur-xl text-center space-y-4">
                  {/* Top Close icon for dismissible dialogs */}
                  {!isBlocking && (
                    <div className="flex justify-end">
                      <span className="p-1 rounded-full text-slate-400 hover:text-white bg-slate-800/40">
                        <X className="w-3.5 h-3.5" />
                      </span>
                    </div>
                  )}

                  {/* Banner Image Preview */}
                  {imageUrl.trim() && (
                    <div className="w-full h-32 rounded-xl overflow-hidden bg-slate-900 border border-slate-800">
                      <img
                        src={imageUrl}
                        alt="بنر دیالوگ"
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          (e.target as HTMLElement).style.display = 'none';
                        }}
                      />
                    </div>
                  )}

                  {/* Type Icon & Glow */}
                  {(() => {
                    const conf = getTypeColor(type);
                    const Icon = conf.icon;
                    return (
                      <div className="flex flex-col items-center gap-2">
                        <div
                          className={`w-12 h-12 rounded-2xl flex items-center justify-center border shadow-lg ${conf.bg} ${conf.border} ${conf.text}`}
                        >
                          <Icon className="w-6 h-6" />
                        </div>
                      </div>
                    );
                  })()}

                  {/* Title */}
                  <h4 className="text-base font-bold text-white tracking-tight">
                    {title.trim() || 'عنوان اطلاعیه'}
                  </h4>

                  {/* Message */}
                  <p className="text-xs text-slate-300 leading-relaxed whitespace-pre-line max-h-36 overflow-y-auto px-1">
                    {message.trim() || 'متن پیام در اینجا قرار می‌گیرد...'}
                  </p>

                  {/* Do Not Show for 24h Checkbox Preview */}
                  {!isBlocking ? (
                    <div className="flex items-center justify-center gap-2 pt-1 text-slate-400 text-[11px]">
                      <input
                        type="checkbox"
                        checked={false}
                        readOnly
                        className="rounded border-slate-700 bg-slate-800 text-cyan-500 w-3.5 h-3.5 cursor-not-allowed"
                      />
                      <span>تا ۲۴ ساعت آینده نشان نده</span>
                    </div>
                  ) : (
                    <div className="text-[11px] text-rose-400 font-medium flex items-center justify-center gap-1">
                      <ShieldAlert className="w-3 h-3" />
                      این پیام حالت مسدودکننده دارد و امکان رد شدن ندارد
                    </div>
                  )}

                  {/* Buttons Mockup */}
                  <div className="space-y-2 pt-2">
                    <button
                      type="button"
                      className="w-full py-2.5 px-4 rounded-xl font-medium text-xs text-white bg-gradient-to-r from-cyan-500 to-blue-600 shadow-md shadow-cyan-500/20"
                    >
                      {primaryText.trim() || 'فهمیدم'}
                    </button>

                    {hasSecondary && secondaryText.trim() && (
                      <button
                        type="button"
                        className="w-full py-2 px-4 rounded-xl font-medium text-xs text-slate-300 bg-slate-800/80 border border-slate-700 hover:bg-slate-700/80"
                      >
                        {secondaryText.trim()}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Modal Footer */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsModalOpen(false)}
              className="border-slate-700 text-slate-300"
            >
              انصراف
            </Button>
            <Button
              type="submit"
              disabled={isSaving}
              className="bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-medium"
            >
              {isSaving ? 'در حال ذخیره‌سازی...' : editingId ? 'ذخیره تغییرات' : 'ایجاد اطلاعیه'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default AnnouncementsPage;
