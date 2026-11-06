import React from 'react';
import { motion, HTMLMotionProps } from 'motion/react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoButtonProps extends Omit<HTMLMotionProps<'button'>, 'children'> {
  variant?: 'glass' | 'filled' | 'tinted' | 'plain';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
  children: React.ReactNode;
  icon?: React.ReactNode;
}

export const CupertinoButton: React.FC<CupertinoButtonProps> = ({
  variant = 'glass',
  size = 'md',
  fullWidth = false,
  className,
  children,
  icon,
  disabled,
  ...props
}) => {
  const baseClasses =
    'relative inline-flex items-center justify-center font-medium select-none transition-colors duration-150 active:scale-[0.97] touch-manipulation';

  const sizeClasses = {
    sm: 'text-xs h-8 px-3.5 rounded-full gap-1.5',
    md: 'text-sm h-11 px-5 rounded-full gap-2 font-semibold',
    lg: 'text-base h-13 px-7 rounded-full gap-2.5 font-bold',
  };

  const variantClasses = {
    glass: 'apple-glass-pill text-white active:bg-white/15',
    filled: 'bg-apple-blue text-white shadow-apple-sm active:bg-blue-600',
    tinted: 'bg-apple-blue/15 text-apple-blue border border-apple-blue/20 active:bg-apple-blue/25',
    plain: 'bg-transparent text-apple-blue active:opacity-60',
  };

  return (
    <motion.button
      whileTap={{ scale: disabled ? 1 : 0.97 }}
      transition={{ type: 'spring', stiffness: 450, damping: 25 }}
      disabled={disabled}
      className={twMerge(
        clsx(
          baseClasses,
          sizeClasses[size],
          variantClasses[variant],
          fullWidth && 'w-full',
          disabled && 'opacity-40 cursor-not-allowed active:scale-100',
          className
        )
      )}
      {...props}
    >
      {icon && <span className="inline-flex shrink-0">{icon}</span>}
      <span>{children}</span>
    </motion.button>
  );
};
