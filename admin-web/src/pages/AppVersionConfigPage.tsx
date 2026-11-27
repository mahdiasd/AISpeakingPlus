import React, { useState, useEffect, useCallback } from 'react';
import {
  Smartphone,
  Save,
  RefreshCw,
  RotateCcw,
  ExternalLink,
  AlertTriangle,
  Sparkles,
  Link as LinkIcon,
  CheckCircle2,
  ShieldAlert,
} from 'lucide-react';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { AppVersionConfig, UpdateAppVersionConfigPayload } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';

const DEFAULT_CAFEBAZAAR_LINK = 'https://cafebazaar.ir/app/ir.aispeaking';

export const AppVersionConfigPage: React.FC = () => {
  const { error: toastError, success: toastSuccess } = useToast();

  const [initialConfig, setInitialConfig] = useState<AppVersionConfig | null>(null);
  const [lastVersion, setLastVersion] = useState<string>('10');
  const [minVersion, setMinVersion] = useState<string>('10');
  const [suggestVersion, setSuggestVersion] = useState<string>('10');
  const [link, setLink] = useState<string>(DEFAULT_CAFEBAZAAR_LINK);
  const [message, setMessage] = useState<string>('');

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [validationError, setValidationError] = useState<string | null>(null);

  const applyConfigToForm = (cfg: AppVersionConfig) => {
    setLastVersion(String(cfg.lastVersion));
    setMinVersion(String(cfg.minVersion));
    setSuggestVersion(String(cfg.suggestVersion));
    setLink(cfg.link);
    setMessage(cfg.message);
    setValidationError(null);
  };

  const fetchConfig = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.getAppVersionConfig();
      setInitialConfig(data);
      applyConfigToForm(data);
    } catch (err: any) {
      toastError(err.message || 'خطا در دریافت تنظیمات نسخه اپلیکیشن');
    } finally {
      setIsLoading(false);
    }
  }, [toastError]);

  useEffect(() => {
    fetchConfig();
  }, [fetchConfig]);

  const parsedLast = Number.parseInt(lastVersion, 10);
  const parsedMin = Number.parseInt(minVersion, 10);
  const parsedSuggest = Number.parseInt(suggestVersion, 10);

  const validate = (): UpdateAppVersionConfigPayload | null => {
    if (!Number.isInteger(parsedLast) || parsedLast < 1) {
      setValidationError('آخرین نسخه منتشرشده باید عددی صحیح و بزرگ‌تر از صفر باشد.');
      return null;
    }
    if (!Number.isInteger(parsedMin) || parsedMin < 0) {
      setValidationError('حداقل نسخه مجاز (آپدیت اجباری) نمی‌تواند منفی باشد.');
      return null;
    }
    if (!Number.isInteger(parsedSuggest) || parsedSuggest < 0) {
      setValidationError('نسخه پیشنهادی نمی‌تواند منفی باشد.');
      return null;
    }
    if (parsedMin > parsedLast) {
      setValidationError('حداقل نسخه مجاز (آپدیت اجباری) نمی‌تواند از آخرین نسخه منتشرشده بزرگ‌تر باشد.');
      return null;
    }
    if (parsedSuggest > parsedLast) {
      setValidationError('نسخه پیشنهادی نمی‌تواند از آخرین نسخه منتشرشده بزرگ‌تر باشد.');
      return null;
    }

    const trimmedLink = link.trim();
    if (!trimmedLink || (!trimmedLink.startsWith('https://') && !trimmedLink.startsWith('http://'))) {
      setValidationError('لینک کافه‌بازار معتبر نیست و باید با https:// یا http:// شروع شود.');
      return null;
    }

    const trimmedMessage = message.trim();
    if (!trimmedMessage) {
      setValidationError('متن پیام به‌روزرسانی به فارسی نمی‌تواند خالی باشد.');
      return null;
    }

    setValidationError(null);
    return {
      lastVersion: parsedLast,
      minVersion: parsedMin,
      suggestVersion: parsedSuggest,
      link: trimmedLink,
      message: trimmedMessage,
    };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = validate();
    if (!payload) return;

    setIsSaving(true);
    try {
      const updated = await api.updateAppVersionConfig(payload);
      setInitialConfig(updated);
      applyConfigToForm(updated);
      toastSuccess('تنظیمات نسخه اپلیکیشن با موفقیت ذخیره شد');
    } catch (err: any) {
      toastError(err.message || 'خطا در ذخیره تنظیمات نسخه اپلیکیشن');
    } finally {
      setIsSaving(false);
    }
  };

  const handleReset = () => {
    if (initialConfig) {
      applyConfigToForm(initialConfig);
    }
  };

  const isDirty =
    initialConfig !== null &&
    (String(initialConfig.lastVersion) !== lastVersion ||
      String(initialConfig.minVersion) !== minVersion ||
      String(initialConfig.suggestVersion) !== suggestVersion ||
      initialConfig.link !== link ||
      initialConfig.message !== message);

  const previewMin = Number.isInteger(parsedMin) ? parsedMin : 0;
  const previewSuggest = Number.isInteger(parsedSuggest) ? parsedSuggest : 0;
  const previewLast = Number.isInteger(parsedLast) ? parsedLast : 0;

  return (
    <div className="space-y-6 max-w-6xl mx-auto pb-12" dir="rtl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-indigo-50 dark:bg-indigo-500/15 border border-indigo-200 dark:border-indigo-500/30 flex items-center justify-center text-indigo-600 dark:text-indigo-400">
            <Smartphone className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-xl sm:text-2xl font-bold text-slate-900 dark:text-[#f1f3f5] tracking-tight">
                تنظیمات نسخه اپلیکیشن
              </h1>
              <Badge variant="purple" size="sm">
                Android
              </Badge>
            </div>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-[#9ca3af] mt-0.5">
              مدیریت داینامیک نسخه اجباری، نسخه پیشنهادی، لینک کافه‌بازار و پیام به‌روزرسانی
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={fetchConfig}
            isLoading={isLoading}
            icon={<RefreshCw className="w-4 h-4" />}
          >
            تازه‌سازی
          </Button>
        </div>
      </div>

      {isLoading ? (
        <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-16 flex flex-col items-center justify-center gap-3">
          <div className="w-8 h-8 border-3 border-indigo-500/20 border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin" />
          <p className="text-sm font-medium text-slate-500 dark:text-[#9ca3af]">
            در حال دریافت تنظیمات نسخه اپلیکیشن...
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Form Card */}
          <form
            onSubmit={handleSubmit}
            className="lg:col-span-7 bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-5 sm:p-6 shadow-2xs space-y-5"
          >
            <div className="flex items-center justify-between border-b border-slate-100 dark:border-[#2d3037] pb-4">
              <div>
                <h2 className="text-base font-bold text-slate-900 dark:text-[#f1f3f5]">
                  پیکربندی نسخه و به‌روزرسانی
                </h2>
                <p className="text-xs text-slate-500 dark:text-[#9ca3af] mt-0.5">
                  مقادیر ذخیره‌شده بلافاصله در اندپوینت‌های <span className="font-mono" dir="ltr">/api/v1/config</span> و{' '}
                  <span className="font-mono" dir="ltr">/api/v2/config</span> اعمال می‌شوند.
                </p>
              </div>
              {initialConfig?.updatedAt && (
                <span className="text-xs text-slate-400 dark:text-[#858a94] whitespace-nowrap">
                  آخرین ویرایش:{' '}
                  {new Date(initialConfig.updatedAt).toLocaleString('fa-IR', {
                    dateStyle: 'short',
                    timeStyle: 'short',
                  })}
                </span>
              )}
            </div>

            {validationError && (
              <div className="flex items-start gap-2.5 p-3.5 rounded-xl bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-500/30 text-rose-700 dark:text-rose-300 text-xs sm:text-sm">
                <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{validationError}</span>
              </div>
            )}

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Input
                type="number"
                min={1}
                step={1}
                dir="ltr"
                label="آخرین نسخه منتشرشده (lastVersion)"
                helperText="نسخه فعلی منتشرشده در مارکت"
                value={lastVersion}
                onChange={(e) => {
                  setLastVersion(e.target.value);
                  setValidationError(null);
                }}
                required
              />

              <Input
                type="number"
                min={0}
                step={1}
                dir="ltr"
                label="حداقل نسخه مجاز / اجباری (minVersion)"
                helperText="معادل force_version در اپ"
                value={minVersion}
                onChange={(e) => {
                  setMinVersion(e.target.value);
                  setValidationError(null);
                }}
                required
              />

              <Input
                type="number"
                min={0}
                step={1}
                dir="ltr"
                label="نسخه پیشنهادی (suggestVersion)"
                helperText="نمایش پیشنهاد آپدیت اختیاری"
                value={suggestVersion}
                onChange={(e) => {
                  setSuggestVersion(e.target.value);
                  setValidationError(null);
                }}
                required
              />
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between gap-2">
                <label className="block text-sm font-medium text-slate-700 dark:text-[#d1d5db]">
                  لینک صفحه کافه‌بازار اندروید
                </label>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      setLink(DEFAULT_CAFEBAZAAR_LINK);
                      setValidationError(null);
                    }}
                    className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline"
                  >
                    بازنشانی به لینک پیش‌فرض
                  </button>
                  {link.trim().startsWith('http') && (
                    <a
                      href={link.trim()}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1 text-xs text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200"
                    >
                      <span>تست لینک</span>
                      <ExternalLink className="w-3 h-3" />
                    </a>
                  )}
                </div>
              </div>
              <Input
                type="url"
                dir="ltr"
                placeholder={DEFAULT_CAFEBAZAAR_LINK}
                leftIcon={<LinkIcon className="w-4 h-4" />}
                value={link}
                onChange={(e) => {
                  setLink(e.target.value);
                  setValidationError(null);
                }}
                required
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-slate-700 dark:text-[#d1d5db]">
                متن پیام به‌روزرسانی (فارسی)
              </label>
              <textarea
                rows={4}
                value={message}
                onChange={(e) => {
                  setMessage(e.target.value);
                  setValidationError(null);
                }}
                placeholder="متن پیام نمایش‌داده‌شده به کاربر در دیالوگ به‌روزرسانی..."
                className="w-full rounded-xl bg-white dark:bg-[#24262b] border border-slate-300 dark:border-[#363842] hover:border-slate-400 dark:hover:border-[#474a56] focus:border-indigo-600 dark:focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100 dark:focus:ring-indigo-500/25 text-slate-900 dark:text-[#f1f3f5] placeholder-slate-400 dark:placeholder-[#858a94] text-sm px-3.5 py-2.5 transition-all"
                required
              />
            </div>

            <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-slate-100 dark:border-[#2d3037]">
              <Button
                type="button"
                variant="ghost"
                size="md"
                onClick={handleReset}
                disabled={!isDirty || isSaving}
                icon={<RotateCcw className="w-4 h-4" />}
              >
                لغو تغییرات
              </Button>

              <Button
                type="submit"
                variant="primary"
                size="md"
                isLoading={isSaving}
                icon={<Save className="w-4 h-4" />}
              >
                ذخیره تنظیمات نسخه
              </Button>
            </div>
          </form>

          {/* Live Preview & Behavior Summary */}
          <div className="lg:col-span-5 space-y-4">
            {/* Version Rules Summary */}
            <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-5 shadow-2xs space-y-3">
              <h3 className="text-sm font-bold text-slate-900 dark:text-[#f1f3f5]">
                خلاصه رفتار در اپلیکیشن
              </h3>

              <div className="space-y-2.5 text-xs sm:text-sm">
                <div className="flex items-start gap-2.5 p-3 rounded-xl bg-rose-50/70 dark:bg-rose-500/10 border border-rose-200/70 dark:border-rose-500/20">
                  <ShieldAlert className="w-4 h-4 text-rose-600 dark:text-rose-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold text-rose-800 dark:text-rose-300">به‌روزرسانی اجباری (Force Update)</p>
                    <p className="text-rose-700/90 dark:text-rose-300/80 mt-0.5">
                      نسخه‌های کوچک‌تر از <span className="font-mono font-bold" dir="ltr">{previewMin}</span> مسدود می‌شوند و باید اپ را آپدیت کنند.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-2.5 p-3 rounded-xl bg-amber-50/70 dark:bg-amber-500/10 border border-amber-200/70 dark:border-amber-500/20">
                  <Sparkles className="w-4 h-4 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold text-amber-800 dark:text-amber-300">به‌روزرسانی پیشنهادی (اختیاری)</p>
                    <p className="text-amber-700/90 dark:text-amber-300/80 mt-0.5">
                      نسخه‌های بین <span className="font-mono font-bold" dir="ltr">{previewMin}</span> تا{' '}
                      <span className="font-mono font-bold" dir="ltr">{Math.max(previewMin, previewSuggest - 1)}</span> پیام پیشنهاد آپدیت می‌بینند اما می‌توانند ادامه دهند.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-2.5 p-3 rounded-xl bg-emerald-50/70 dark:bg-emerald-500/10 border border-emerald-200/70 dark:border-emerald-500/20">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold text-emerald-800 dark:text-emerald-300">نسخه به‌روز</p>
                    <p className="text-emerald-700/90 dark:text-emerald-300/80 mt-0.5">
                      نسخه <span className="font-mono font-bold" dir="ltr">{previewLast}</span> و بالاتر بدون پیام به‌روزرسانی وارد برنامه می‌شوند.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Update Dialog Preview */}
            <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-5 shadow-2xs space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold text-slate-900 dark:text-[#f1f3f5]">
                  پیش‌نمایش دیالوگ کاربر
                </h3>
                <Badge variant="neutral" size="sm">
                  نسخه {previewLast}
                </Badge>
              </div>

              <div className="rounded-2xl border border-slate-200 dark:border-[#343741] bg-slate-50 dark:bg-[#18191c] p-4 space-y-3">
                <p className="text-sm font-bold text-slate-900 dark:text-slate-100">
                  به‌روزرسانی نسخه جدید
                </p>
                <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-300 leading-relaxed whitespace-pre-line">
                  {message.trim() || 'متن پیام به‌روزرسانی اینجا نمایش داده می‌شود...'}
                </p>
                <div className="flex items-center gap-2 pt-1">
                  <span className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-indigo-600 text-white text-xs font-semibold">
                    دریافت از کافه‌بازار
                    <ExternalLink className="w-3.5 h-3.5" />
                  </span>
                  <span className="text-[11px] font-mono text-slate-400 truncate" dir="ltr">
                    {link.trim() || DEFAULT_CAFEBAZAAR_LINK}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
