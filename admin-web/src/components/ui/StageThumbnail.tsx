import React, { useState } from 'react';
import { Compass, Maximize2 } from 'lucide-react';
import { Modal } from './Modal';

interface StageThumbnailProps {
  src?: string | null;
  alt?: string;
  className?: string;
  aspectRatio?: 'wide' | 'square' | 'video' | 'portrait';
  fallbackTitle?: string;
  allowPreview?: boolean;
}

export const StageThumbnail: React.FC<StageThumbnailProps> = ({
  src,
  alt = 'Stage visual',
  className = '',
  aspectRatio = 'portrait',
  fallbackTitle,
  allowPreview = true,
}) => {
  const [hasError, setHasError] = useState(false);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);

  const aspectClasses = {
    wide: 'w-20 h-14 sm:w-24 sm:h-16',
    square: 'w-14 h-14 sm:w-16 sm:h-16',
    video: 'w-28 h-16 sm:w-32 sm:h-20',
    portrait: 'w-12 sm:w-14 aspect-[9/16]',
  };

  const showImage = Boolean(src && !hasError);

  return (
    <>
      <div
        onClick={(e) => {
          if (showImage && allowPreview) {
            e.stopPropagation();
            setIsPreviewOpen(true);
          }
        }}
        className={`relative shrink-0 overflow-hidden rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-900 shadow-xs flex items-center justify-center select-none ${aspectClasses[aspectRatio]} ${
          showImage && allowPreview ? 'cursor-pointer group' : ''
        } ${className}`}
        title={showImage && allowPreview ? 'کلیک برای مشاهده پیش‌نمایش عمودی در اندازه واقعی' : undefined}
      >
        {showImage ? (
          <>
            <img
              src={src!}
              alt={alt}
              onError={() => setHasError(true)}
              className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
              loading="lazy"
            />
            {allowPreview && (
              <div className="absolute inset-0 bg-slate-950/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center pointer-events-none">
                <Maximize2 className="w-3.5 h-3.5 text-white drop-shadow-sm" />
              </div>
            )}
          </>
        ) : (
          /* Modern, sleek SVG scenario placeholder illustration */
          <div className="w-full h-full flex flex-col items-center justify-center p-1 text-center bg-gradient-to-br from-indigo-50/70 via-slate-50 to-slate-100 dark:from-indigo-950/30 dark:via-slate-900/70 dark:to-slate-900 text-slate-400 dark:text-slate-500">
            <div className="w-7 h-7 rounded-lg bg-indigo-500/10 dark:bg-indigo-500/20 text-indigo-500 dark:text-indigo-400 flex items-center justify-center mb-1 shadow-xs border border-indigo-200/40 dark:border-indigo-500/20">
              <Compass className="w-4 h-4" />
            </div>
            {fallbackTitle ? (
              <span className="text-[9px] font-medium text-slate-600 dark:text-slate-400 line-clamp-2 max-w-[95%] leading-tight text-center px-0.5">
                {fallbackTitle}
              </span>
            ) : (
              <span className="text-[8px] font-mono text-slate-400 dark:text-slate-500 leading-none">
                عمودی ۹:۱۶
              </span>
            )}
          </div>
        )}
      </div>

      {showImage && allowPreview && (
        <Modal
          isOpen={isPreviewOpen}
          onClose={() => setIsPreviewOpen(false)}
          title={alt || 'پیش‌نمایش تصویر سناریو'}
          description="تصویر عمودی تمام‌صفحه محیط مکالمه مخصوص گوشی موبایل (نسبت ۹:۱۶)"
          maxWidth="md"
        >
          <div className="flex flex-col items-center justify-center p-2">
            <div className="relative w-full max-w-[280px] aspect-[9/16] rounded-[28px] overflow-hidden border-2 border-slate-300 dark:border-slate-700 shadow-2xl bg-black ring-4 ring-slate-100 dark:ring-slate-800">
              <div className="absolute top-2.5 left-1/2 -translate-x-1/2 w-16 h-3 bg-black/80 rounded-full z-10 backdrop-blur-xs flex items-center justify-center pointer-events-none border border-slate-800/40">
                <div className="w-2 h-2 rounded-full bg-slate-900 border border-slate-700/60" />
              </div>
              <img
                src={src!}
                alt={alt}
                className="w-full h-full object-cover"
              />
            </div>
            {fallbackTitle && (
              <p className="mt-4 text-sm font-semibold text-slate-800 dark:text-slate-200 text-center">
                {fallbackTitle}
              </p>
            )}
          </div>
        </Modal>
      )}
    </>
  );
};
