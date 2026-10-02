import React, { useState, useRef } from 'react';
import { UploadCloud, Image as ImageIcon, X, Loader2, CheckCircle2 } from 'lucide-react';
import { api } from '../../api/client';
import { useToast } from '../../context/ToastContext';

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

  return (
    <div className="w-full">
      <div className="flex items-center justify-between mb-2">
        <label className="text-sm font-medium text-slate-700 dark:text-slate-300">{label}</label>
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
        <div className="relative group rounded-2xl overflow-hidden border border-slate-200 dark:border-slate-800 bg-white dark:bg-[#0E1422] shadow-xs">
          <div
            className={`w-full overflow-hidden flex items-center justify-center bg-slate-100 dark:bg-[#0B0F17]/60 ${
              aspectRatio === 'square' ? 'h-44' : aspectRatio === 'portrait' ? 'h-72 py-2' : 'h-52'
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
                <p className="text-[11px] font-mono mt-1 text-slate-500 truncate max-w-xs" dir="ltr">{value}</p>
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
      ) : (
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
            <div className="flex flex-col items-center gap-3 py-2">
              <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shadow-xs">
                {isDragging ? <UploadCloud className="w-6 h-6 animate-bounce" /> : <ImageIcon className="w-6 h-6" />}
              </div>
              <div>
                <p className="text-sm font-medium text-slate-800 dark:text-slate-200">
                  فایل تصویر را به اینجا بکشید یا <span className="text-indigo-600 dark:text-indigo-400 hover:underline">انتخاب کنید</span>
                </p>
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
    </div>
  );
};
