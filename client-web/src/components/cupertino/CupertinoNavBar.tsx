import React from 'react';
import { ChevronRight } from 'lucide-react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoNavBarProps {
  title: string;
  subtitle?: string;
  onBack?: () => void;
  backLabel?: string;
  endSlot?: React.ReactNode;
  className?: string;
}

export const CupertinoNavBar: React.FC<CupertinoNavBarProps> = ({
  title,
  subtitle,
  onBack,
  backLabel = 'بازگشت',
  endSlot,
  className,
}) => {
  return (
    <header
      className={twMerge(
        clsx(
          'sticky top-0 z-50 w-full pt-[var(--safe-top)] bg-[#121215]/80 backdrop-blur-xl border-b border-white/10 select-none',
          className
        )
      )}
    >
      <div className="relative flex items-center justify-between h-14 px-4 max-w-lg mx-auto">
        {/* Start Anchor (Top Right in RTL): Back Button */}
        <div className="flex items-center min-w-[70px] justify-start z-10">
          {onBack && (
            <button
              type="button"
              onClick={onBack}
              className="inline-flex items-center gap-0.5 text-apple-blue font-medium text-sm active:opacity-60 transition-opacity touch-manipulation"
            >
              {/* Chevron pointing right (>) in Persian RTL as per project rule */}
              <ChevronRight className="w-5 h-5 stroke-[2.5]" />
              <span>{backLabel}</span>
            </button>
          )}
        </div>

        {/* Center Anchor: Symmetrical Centered Title */}
        <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none px-24">
          <h1 className="text-sm sm:text-base font-bold text-white tracking-tight truncate max-w-full text-center leading-tight">
            {title}
          </h1>
          {subtitle && (
            <span className="text-[10px] sm:text-[11px] text-apple-gray-400 font-normal truncate max-w-full text-center leading-tight mt-0.5">
              {subtitle}
            </span>
          )}
        </div>

        {/* End Anchor (Top Left in RTL): Actions */}
        <div className="flex items-center min-w-[70px] justify-end z-10">
          {endSlot}
        </div>
      </div>
    </header>
  );
};
