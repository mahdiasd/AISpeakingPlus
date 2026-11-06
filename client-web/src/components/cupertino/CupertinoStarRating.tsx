import React from 'react';
import { Star } from 'lucide-react';
import { clsx } from 'clsx';

export interface CupertinoStarRatingProps {
  stars: number; // 0 to 3
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  maxStars?: number;
}

export const CupertinoStarRating: React.FC<CupertinoStarRatingProps> = ({
  stars = 0,
  size = 'md',
  className,
  maxStars = 3,
}) => {
  const sizeMap = {
    sm: 'w-3 h-3',
    md: 'w-4 h-4',
    lg: 'w-5 h-5',
  };

  const clampedStars = Math.max(0, Math.min(maxStars, Math.round(stars)));

  return (
    <div
      aria-label={`${clampedStars} ستاره از ${maxStars}`}
      className={clsx('inline-flex items-center gap-1 select-none', className)}
    >
      {Array.from({ length: maxStars }).map((_, index) => {
        const isFilled = index < clampedStars;
        return (
          <Star
            key={index}
            className={clsx(
              sizeMap[size],
              'transition-all duration-200',
              isFilled
                ? 'fill-apple-yellow text-apple-yellow drop-shadow-[0_0_8px_rgba(255,204,0,0.5)]'
                : 'fill-transparent text-white/20'
            )}
          />
        );
      })}
    </div>
  );
};
