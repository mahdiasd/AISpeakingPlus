import React, { useState, useRef } from 'react';
import { UploadCloud, Image as ImageIcon, X, Loader2, Maximize2, RefreshCw } from 'lucide-react';
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
  helperText = 'WebP، PNG یا JPG',
  aspectRatio = 'portrait',
}) => {
  const { error: toastError, success: toastSuccess } = useToast();
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [previewError, setPreviewError] = useState(false);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFile = async (file: File) => {
    if (!file.type.startsWith('image/')) {
      toastError('تنها فایل‌های تصویری مجاز هستند');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      toastError('حجم تصویر نباید بیشتر از ۵ مگابایت باشد');
      return;
    }

    setIsUploading(true);
    setPreviewError(false);

    try {
      const res = await api.uploadMedia(file);
      onChange(res.url);
      toastSuccess('تصویر با موفقیت بارگذاری شد');
    } catch (err: any) {
      toastError(err.message || 'خطا در بارگذاری تصویر');
    } finally {
      setIsUploading(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const isPortrait = aspectRatio === 'portrait';
  const isSquare = aspectRatio === 'square';

  // Proportional sizing: vertical 9:16 for portrait, 1:1 for square, banner for wide
  const sizeClasses = isPortrait
    ? 'w-44 sm:w-48 aspect-[9/16]'
    : isSquare
    ? 'w-28 h-28 aspect-square'
    : 'w-full max-w-sm h-36';

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <label className="text-xs font-semibold text-slate-700 dark:text-slate-300 select-none">
          {label}
        </label>
        {value && (
          <button
            type="button"
            onClick={() => {
              onChange('');
              setPreviewError(false);
            }}
            className="text-[11px] font-medium text-slate-400 hover:text-rose-600 dark:hover:text-rose-400 transition-colors"
          >
            حذف
          </button>
        )}
      </div>

      {value ? (
        <div className="flex flex-col items-start gap-2">
          {/* Preview Container with Apple-style hover controls */}
          <div
            className={`relative group rounded-2xl overflow-hidden border border-slate-200/80 dark:border-slate-800 bg-slate-900 shadow-xs select-none ${sizeClasses}`}
          >
            {!previewError ? (
              <img
                src={value}
                alt={label}
                className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                onError={() => setPreviewError(true)}
              />
            ) : (
              <div className="w-full h-full flex flex-col items-center justify-center p-3 text-center bg-slate-900 text-slate-400">
                <ImageIcon className="w-6 h-6 mb-1 opacity-50" />
                <span className="text-[10px]">خطا در تصویر</span>
              </div>
            )}

            {/* Quiet, refined blur overlay on hover */}
            <div className="absolute inset-0 bg-slate-950/60 backdrop-blur-xs opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-1.5 p-2">
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="p-2 rounded-xl bg-white/90 hover:bg-white text-slate-800 shadow-sm active:scale-95 transition-all"
                title="تغییر تصویر"
              >
                <RefreshCw className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={() => setIsPreviewOpen(true)}
                className="p-2 rounded-xl bg-slate-800/90 hover:bg-slate-800 text-white shadow-sm active:scale-95 transition-all"
                title="مشاهده اندازه واقعی"
              >
                <Maximize2 className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={() => {
                  onChange('');
                  setPreviewError(false);
                }}
                className="p-2 rounded-xl bg-rose-600/90 hover:bg-rose-600 text-white shadow-sm active:scale-95 transition-all"
                title="حذف تصویر"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      ) : (
        /* Empty State Dropzone: Quiet, elegant dashed frame */
        <div
          onDrop={handleDrop}
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={(e) => {
            e.preventDefault();
            setIsDragging(false);
          }}
          onClick={() => fileInputRef.current?.click()}
          className={`relative border border-dashed rounded-2xl cursor-pointer transition-all duration-200 flex flex-col items-center justify-center p-4 text-center select-none ${sizeClasses} ${
            isDragging
              ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-500/10'
              : 'border-slate-300 dark:border-slate-800 hover:border-slate-400 dark:hover:border-slate-700 bg-slate-50/50 dark:bg-[#0E1422]/40 hover:bg-white dark:hover:bg-[#121826]'
          }`}
        >
          {isUploading ? (
            <div className="flex flex-col items-center gap-2">
              <Loader2 className="w-6 h-6 text-indigo-500 animate-spin" />
              <span className="text-[11px] text-slate-500">بارگذاری...</span>
            </div>
          ) : (
            <div className="flex flex-col items-center gap-1.5 text-slate-400 group-hover:text-slate-600 dark:group-hover:text-slate-300">
              <div className="w-8 h-8 rounded-xl bg-white dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700/60 flex items-center justify-center text-slate-500 dark:text-slate-400 shadow-2xs">
                <UploadCloud className="w-4 h-4" />
              </div>
              <span className="text-xs font-medium text-slate-700 dark:text-slate-300">
                انتخاب تصویر
              </span>
              <span className="text-[10px] text-slate-400 dark:text-slate-500">
                {helperText}
              </span>
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

      {/* Clean Full-Size Preview Modal */}
      {value && (
        <Modal
          isOpen={isPreviewOpen}
          onClose={() => setIsPreviewOpen(false)}
          title={label}
          maxWidth="md"
        >
          <div className="flex items-center justify-center p-2">
            <div
              className={`rounded-2xl overflow-hidden border border-slate-700 shadow-2xl bg-black ${
                isPortrait ? 'w-full max-w-[280px] aspect-[9/16]' : 'max-w-md'
              }`}
            >
              <img src={value} alt={label} className="w-full h-full object-cover" />
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
