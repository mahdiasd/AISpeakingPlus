import React, { useState, useRef } from 'react';
import {
  UploadCloud,
  Image as ImageIcon,
  X,
  Loader2,
  CheckCircle2,
  Smartphone,
  Maximize2,
  Copy,
  Check,
} from 'lucide-react';
import { api } from '../../api/client';
import { useToast } from '../../context/ToastContext';
import { Modal } from '../ui/Modal';

interface ImageDropzoneProps {
  label: string;
  value?: string | null;
  onChange: (url: string) => void;
  helperText?: string;
  aspectRatio?: 'square' | 'wide' | 'portrait';
}

export const ImageDropzone: React.FC<ImageDropzoneProps> = ({
  label,
  value,
  onChange,
  helperText = 'فرمت‌های WebP، PNG یا JPG (حداکثر ۵ مگابایت)',
  aspectRatio = 'portrait',
}) => {
  const { error: toastError, success: toastSuccess } = useToast();
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState<number | null>(null);
  const [previewError, setPreviewError] = useState(false);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFile = async (file: File) => {
    // Validate type
    if (!file.type.startsWith('image/')) {
      toastError('تنها فایل‌های تصویری مجاز هستند');
      return;
    }

    // Validate size (5MB)
    if (file.size > 5 * 1024 * 1024) {
      toastError('حجم تصویر نباید بیشتر از ۵ مگابایت باشد');
      return;
    }

    setIsUploading(true);
    setUploadProgress(20);
    setPreviewError(false);

    try {
      setUploadProgress(60);
      const res = await api.uploadMedia(file);
      setUploadProgress(100);
      onChange(res.url);
      toastSuccess('تصویر با موفقیت آپلود شد');
    } catch (err: any) {
      toastError(err.message || 'خطا در آپلود تصویر');
    } finally {
      setIsUploading(false);
      setUploadProgress(null);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleCopyUrl = async () => {
    if (!value) return;
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      toastSuccess('آدرس تصویر در کلیپ‌بورد کپی شد');
      setTimeout(() => setCopied(false), 2000);
    } catch {
      toastError('امکان کپی آدرس وجود ندارد');
    }
  };

  const isPortrait = aspectRatio === 'portrait';
  const isSquare = aspectRatio === 'square';

  return (
    <div className="w-full">
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium text-slate-700 dark:text-slate-300">{label}</label>
          {isPortrait && (
            <span className="text-[11px] font-mono px-2 py-0.5 rounded-md bg-indigo-50 text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-500/30">
              عمودی ۹:۱۶
            </span>
          )}
        </div>
        {value && (
          <button
            type="button"
            onClick={() => {
              onChange('');
              setPreviewError(false);
            }}
            className="text-xs font-medium text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 transition-colors flex items-center gap-1"
          >
            <X className="w-3.5 h-3.5" />
            حذف تصویر
          </button>
        )}
      </div>

      {value ? (
        isPortrait ? (
          /* Vertical Portrait 9:16 Display */
          <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] p-5 shadow-xs">
            <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6">
              {/* Phone Frame Mockup (Strict 9:16 Vertical Ratio) */}
              <div className="relative group shrink-0 w-52 sm:w-60 aspect-[9/16] rounded-[28px] overflow-hidden border-2 border-slate-300 dark:border-slate-700 shadow-xl bg-slate-950 ring-4 ring-slate-100 dark:ring-slate-800/80">
                {/* Phone Speaker/Notch Header */}
                <div className="absolute top-2.5 left-1/2 -translate-x-1/2 w-16 h-3 bg-black/85 rounded-full z-10 backdrop-blur-xs flex items-center justify-center pointer-events-none border border-slate-800/60 shadow-xs">
                  <div className="w-2 h-2 rounded-full bg-slate-900 border border-slate-700/60" />
                </div>

                {!previewError ? (
                  <img
                    src={value}
                    alt="Preview"
                    className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                    onError={() => setPreviewError(true)}
                  />
                ) : (
                  <div className="w-full h-full flex flex-col items-center justify-center p-4 text-center bg-slate-900 text-slate-400">
                    <ImageIcon className="w-8 h-8 mb-2 opacity-50" />
                    <p className="text-xs font-medium">تصویر بارگذاری نشد</p>
                    <p className="text-[10px] font-mono mt-1 text-slate-500 truncate max-w-[160px]" dir="ltr">
                      {value}
                    </p>
                  </div>
                )}

                {/* Hover overlay with action buttons */}
                <div className="absolute inset-0 bg-slate-950/75 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-2 p-3 backdrop-blur-xs">
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="w-full text-xs font-medium bg-white hover:bg-slate-100 text-slate-900 py-2 rounded-xl transition-all flex items-center justify-center gap-1.5 shadow-sm active:scale-95"
                  >
                    <UploadCloud className="w-4 h-4 text-indigo-500" />
                    تغییر تصویر
                  </button>
                  <button
                    type="button"
                    onClick={() => setIsPreviewOpen(true)}
                    className="w-full text-xs font-medium bg-slate-800 hover:bg-slate-700 text-white py-2 rounded-xl transition-all flex items-center justify-center gap-1.5 shadow-sm active:scale-95 border border-slate-700"
                  >
                    <Maximize2 className="w-4 h-4 text-indigo-400" />
                    اندازه کامل
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      onChange('');
                      setPreviewError(false);
                    }}
                    className="w-full text-xs font-medium bg-rose-600 hover:bg-rose-700 text-white py-2 rounded-xl transition-all flex items-center justify-center gap-1.5 active:scale-95 shadow-sm"
                  >
                    <X className="w-4 h-4" />
                    حذف
                  </button>
                </div>
              </div>

              {/* Info & Action Controls */}
              <div className="flex-1 w-full space-y-4 pt-1 text-right">
                <div className="space-y-1.5">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-lg bg-indigo-50 dark:bg-indigo-500/15 text-indigo-700 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-500/30">
                      نمای عمودی موبایل (Portrait ۹:۱۶)
                    </span>
                    <span className="text-xs text-emerald-600 dark:text-emerald-400 flex items-center gap-1 font-medium bg-emerald-50 dark:bg-emerald-500/10 px-2 py-0.5 rounded-lg border border-emerald-200 dark:border-emerald-500/20">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      آماده نمایش در اپلیکیشن
                    </span>
                  </div>
                  <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed pt-1">
                    این تصویر به عنوان پس‌زمینه عمودی تمام‌صفحه در مکالمه زنده زبان‌آموز با کاراکتر هوش مصنوعی و همچنین به عنوان کاور مرحله در لیست مراحل نمایش داده می‌شود.
                  </p>
                </div>

                {/* File Path Details with Copy Button */}
                <div className="p-3 rounded-xl bg-slate-50 dark:bg-[#111726] border border-slate-200 dark:border-slate-800 text-xs flex items-center justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <span className="block text-[11px] text-slate-400 mb-0.5">آدرس فایل تصویر:</span>
                    <span className="font-mono text-slate-700 dark:text-slate-300 truncate block select-all" dir="ltr">
                      {value}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={handleCopyUrl}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-200/60 dark:hover:bg-slate-800 transition-colors shrink-0"
                    title="کپی آدرس تصویر"
                  >
                    {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                  </button>
                </div>

                {/* Action Buttons */}
                <div className="flex flex-wrap items-center gap-2.5 pt-1">
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="text-xs font-medium bg-indigo-50 hover:bg-indigo-100 dark:bg-indigo-500/15 dark:hover:bg-indigo-500/25 text-indigo-700 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-500/30 px-3.5 py-2 rounded-xl transition-all flex items-center gap-1.5 shadow-2xs active:scale-95"
                  >
                    <UploadCloud className="w-4 h-4 text-indigo-500" />
                    تغییر فایل تصویر
                  </button>
                  <button
                    type="button"
                    onClick={() => setIsPreviewOpen(true)}
                    className="text-xs font-medium bg-white hover:bg-slate-100 dark:bg-[#111726] dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 border border-slate-300 dark:border-slate-800 px-3.5 py-2 rounded-xl transition-all flex items-center gap-1.5 shadow-2xs active:scale-95"
                  >
                    <Maximize2 className="w-4 h-4" />
                    مشاهده اندازه کامل
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      onChange('');
                      setPreviewError(false);
                    }}
                    className="text-xs font-medium text-rose-600 hover:text-rose-700 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-500/10 px-3 py-2 rounded-xl transition-all flex items-center gap-1"
                  >
                    <X className="w-4 h-4" />
                    حذف تصویر
                  </button>
                </div>
              </div>
            </div>
          </div>
        ) : (
          /* Standard / Square Layout */
          <div className="relative group rounded-2xl overflow-hidden border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] shadow-xs">
            <div
              className={`overflow-hidden flex items-center justify-center bg-slate-100 dark:bg-[#0B0F17]/60 ${
                isSquare ? 'w-44 h-44 mx-auto my-3 rounded-2xl border border-slate-200 dark:border-slate-800' : 'w-full h-52'
              }`}
            >
              {!previewError ? (
                <img
                  src={value}
                  alt="Preview"
                  className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                  onError={() => setPreviewError(true)}
                />
              ) : (
                <div className="w-full h-full flex flex-col items-center justify-center p-4 text-center bg-slate-100 dark:bg-slate-900 text-slate-400">
                  <ImageIcon className="w-8 h-8 mb-2 opacity-50" />
                  <p className="text-xs font-medium">تصویر بارگذاری نشد (مسیر نامعتبر یا در دسترس نیست)</p>
                  <p className="text-[11px] font-mono mt-1 text-slate-500 truncate max-w-xs" dir="ltr">
                    {value}
                  </p>
                </div>
              )}
            </div>

            <div className="absolute inset-0 bg-slate-900/60 dark:bg-black/70 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2.5 backdrop-blur-xs">
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="text-xs font-medium bg-white hover:bg-slate-100 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-900 dark:text-white px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 transition-all flex items-center gap-1.5 shadow-sm active:scale-95"
              >
                <UploadCloud className="w-4 h-4 text-indigo-500" />
                تغییر فایل
              </button>
              <button
                type="button"
                onClick={() => {
                  onChange('');
                  setPreviewError(false);
                }}
                className="text-xs font-medium bg-rose-600 hover:bg-rose-700 text-white px-3.5 py-2 rounded-xl transition-all flex items-center gap-1.5 active:scale-95 shadow-sm"
              >
                <X className="w-4 h-4" />
                حذف
              </button>
            </div>

            <div className="p-3 bg-slate-50 dark:bg-[#0E1422] border-t border-slate-200 dark:border-slate-800 flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
              <span className="truncate max-w-[280px] font-mono text-slate-700 dark:text-slate-300" dir="ltr">
                {value}
              </span>
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
            </div>
          </div>
        )
      ) : (
        /* Empty State Dropzone */
        <div
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onClick={() => fileInputRef.current?.click()}
          className={`relative border-2 border-dashed rounded-2xl p-7 text-center cursor-pointer transition-all duration-200 flex flex-col items-center justify-center ${
            isDragging
              ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-500/10'
              : 'border-slate-300 dark:border-slate-800 hover:border-slate-400 dark:hover:border-slate-700 bg-white/60 dark:bg-[#0E1422]/60 hover:bg-white dark:hover:bg-[#0E1422]'
          }`}
        >
          {isUploading ? (
            <div className="flex flex-col items-center gap-3 py-4">
              <Loader2 className="w-8 h-8 text-indigo-600 dark:text-indigo-400 animate-spin" />
              <p className="text-sm text-slate-700 dark:text-slate-300 font-medium">در حال آپلود و ذخیره‌سازی تصویر...</p>
              {uploadProgress && (
                <div className="w-48 bg-slate-200 dark:bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div
                    className="bg-indigo-600 dark:bg-indigo-500 h-full transition-all duration-300"
                    style={{ width: `${uploadProgress}%` }}
                  />
                </div>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center gap-3 py-2 max-w-sm">
              {isPortrait ? (
                /* Vertical Phone Silhouette Indicator */
                <div className="relative">
                  <div className="w-14 h-22 rounded-2xl border-2 border-dashed border-indigo-400/80 dark:border-indigo-500/60 bg-indigo-50/70 dark:bg-indigo-500/10 flex flex-col items-center justify-center text-indigo-600 dark:text-indigo-400 shadow-xs">
                    <Smartphone className="w-6 h-6 mb-1 opacity-80" />
                    <span className="text-[10px] font-mono font-bold">۹:۱۶</span>
                  </div>
                  <div className="absolute -bottom-1 -right-1 w-6 h-6 rounded-full bg-indigo-600 text-white flex items-center justify-center shadow-xs">
                    <UploadCloud className="w-3.5 h-3.5" />
                  </div>
                </div>
              ) : (
                <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shadow-xs">
                  {isDragging ? <UploadCloud className="w-6 h-6 animate-bounce" /> : <ImageIcon className="w-6 h-6" />}
                </div>
              )}
              <div className="space-y-1">
                <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                  فایل تصویر را به اینجا بکشید یا <span className="text-indigo-600 dark:text-indigo-400 hover:underline">انتخاب کنید</span>
                </p>
                {isPortrait && (
                  <p className="text-xs text-indigo-600 dark:text-indigo-400 font-medium">
                    نسبت ابعاد الزامی: عمودی ۹:۱۶ (مانند 1080x1920)
                  </p>
                )}
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">{helperText}</p>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Hidden File Input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/png,image/jpeg,image/webp"
        className="hidden"
        onChange={(e) => {
          if (e.target.files && e.target.files[0]) {
            handleFile(e.target.files[0]);
          }
        }}
      />

      {/* Full-size Portrait Modal */}
      {value && (
        <Modal
          isOpen={isPreviewOpen}
          onClose={() => setIsPreviewOpen(false)}
          title={`پیش‌نمایش: ${label}`}
          description="تصویر عمودی محیط مکالمه مخصوص گوشی موبایل (نسبت ۹:۱۶)"
          maxWidth="md"
        >
          <div className="flex flex-col items-center justify-center p-2">
            <div className="relative w-full max-w-[280px] aspect-[9/16] rounded-[28px] overflow-hidden border-2 border-slate-300 dark:border-slate-700 shadow-2xl bg-black ring-4 ring-slate-100 dark:ring-slate-800">
              <div className="absolute top-2.5 left-1/2 -translate-x-1/2 w-16 h-3 bg-black/85 rounded-full z-10 backdrop-blur-xs flex items-center justify-center pointer-events-none border border-slate-800/60 shadow-xs">
                <div className="w-2 h-2 rounded-full bg-slate-900 border border-slate-700/60" />
              </div>
              <img src={value} alt="Full preview" className="w-full h-full object-cover" />
            </div>
            <p className="mt-4 text-xs font-mono text-slate-500 dark:text-slate-400 truncate max-w-sm" dir="ltr">
              {value}
            </p>
          </div>
        </Modal>
      )}
    </div>
  );
};
