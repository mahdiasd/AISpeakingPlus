import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoPillProps {
  children: React.ReactNode;
  variant?: 'blue' | 'gold' | 'green' | 'red' | 'purple' | 'gray';
  icon?: React.ReactNode;
  size?: 'sm' | 'md';
  className?: string;
  onClick?: () => void;
}

export const CupertinoPill: React.FC<CupertinoPillProps> = ({
  children,
  variant = 'blue',
  icon,
  size = 'sm',
  className,
  onClick,
}) => {
  const variantStyles = {
    blue: 'apple-glass-blue text-apple-teal border-apple-blue/40 shadow-apple-sm',
    gold: 'apple-glass-gold text-apple-yellow border-apple-yellow/40 shadow-apple-sm',
    green: 'bg-apple-green/20 text-emerald-300 border-apple-green/40 backdrop-blur-md',
    red: 'bg-apple-red/20 text-rose-300 border-apple-red/40 backdrop-blur-md',
    purple: 'bg-apple-purple/20 text-purple-200 border-apple-purple/40 backdrop-blur-md',
    gray: 'apple-glass-pill text-apple-gray-200 border-white/25 shadow-apple-sm',
  };

  const sizeStyles = {
    sm: 'text-[11px] px-2.5 py-0.5 gap-1 font-medium',
    md: 'text-xs px-3 py-1 gap-1.5 font-semibold',
  };

  const Component = onClick ? 'button' : 'div';

  return (
    <Component
      type={onClick ? 'button' : undefined}
      onClick={onClick}
      className={twMerge(
        clsx(
          'inline-flex items-center rounded-full border backdrop-blur-md select-none transition-transform touch-manipulation',
          variantStyles[variant],
          sizeStyles[size],
          onClick && 'active:scale-95 cursor-pointer hover:brightness-110',
          className
        )
      )}
    >
      {icon && <span className="inline-flex shrink-0">{icon}</span>}
      <span className="truncate">{children}</span>
    </Component>
  );
};
