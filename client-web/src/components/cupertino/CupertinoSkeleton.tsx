import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoSkeletonProps {
  className?: string;
}

export const CupertinoSkeleton: React.FC<CupertinoSkeletonProps> = ({ className }) => {
  return (
    <div
      className={twMerge(
        clsx(
          'relative overflow-hidden rounded-2xl bg-white/5 border border-white/5',
          'before:absolute before:inset-0 before:-translate-x-full before:animate-[shimmer_1.8s_infinite] before:bg-gradient-to-r before:from-transparent before:via-white/10 before:to-transparent',
          className
        )
      )}
    />
  );
};
