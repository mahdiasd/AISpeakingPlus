import React, { useState } from 'react';
import { Compass } from 'lucide-react';

interface StageThumbnailProps {
  src?: string | null;
  alt?: string;
  className?: string;
  aspectRatio?: 'wide' | 'square' | 'video' | 'portrait';
  fallbackTitle?: string;
}

export const StageThumbnail: React.FC<StageThumbnailProps> = ({
  src,
  alt = 'Stage visual',
  className = '',
  aspectRatio = 'portrait',
  fallbackTitle,
}) => {
  const [hasError, setHasError] = useState(false);

  const aspectClasses = {
    wide: 'w-20 h-14 sm:w-24 sm:h-16',
    square: 'w-14 h-14 sm:w-16 sm:h-16',
    video: 'w-28 h-16 sm:w-32 sm:h-20',
    portrait: 'w-12 h-20 sm:w-14 sm:h-24 aspect-[9/16]',
  };

  const showImage = Boolean(src && !hasError);

  return (
    <div
      className={`relative shrink-0 overflow-hidden rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-900 shadow-xs flex items-center justify-center select-none ${aspectClasses[aspectRatio]} ${className}`}
    >
      {showImage ? (
        <img
          src={src!}
          alt={alt}
          onError={() => setHasError(true)}
          className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
          loading="lazy"
        />
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
  );
};
