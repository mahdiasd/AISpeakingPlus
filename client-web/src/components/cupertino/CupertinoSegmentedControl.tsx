import React from 'react';
import { motion } from 'motion/react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface SegmentedItem<T extends string = string> {
  id: T;
  label: string;
  icon?: React.ReactNode;
}

export interface CupertinoSegmentedControlProps<T extends string = string> {
  items: SegmentedItem<T>[];
  value: T;
  onChange: (value: T) => void;
  className?: string;
}

export function CupertinoSegmentedControl<T extends string = string>({
  items,
  value,
  onChange,
  className,
}: CupertinoSegmentedControlProps<T>) {
  return (
    <div
      className={twMerge(
        clsx(
          'relative flex items-center p-1 bg-white/10 dark:bg-white/5 backdrop-blur-md rounded-full border border-white/10 select-none',
          className
        )
      )}
    >
      {items.map((item) => {
        const isSelected = item.id === value;
        return (
          <button
            key={item.id}
            type="button"
            onClick={() => onChange(item.id)}
            className={clsx(
              'relative flex-1 flex items-center justify-center gap-1.5 py-1.5 px-3 text-xs md:text-sm font-semibold rounded-full transition-colors duration-150 z-10 touch-manipulation',
              isSelected ? 'text-white' : 'text-apple-gray-400 hover:text-white'
            )}
          >
            {item.icon && <span className="shrink-0">{item.icon}</span>}
            <span>{item.label}</span>

            {/* Sliding iOS Background Indicator */}
            {isSelected && (
              <motion.div
                layoutId="cupertino-segmented-pill"
                transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                className="absolute inset-0 rounded-full bg-apple-blue shadow-apple-sm z-[-1]"
              />
            )}
          </button>
        );
      })}
    </div>
  );
}
