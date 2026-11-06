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
    blue: 'bg-apple-blue/15 text-apple-blue border-apple-blue/30',
    gold: 'bg-apple-yellow/15 text-apple-yellow border-apple-yellow/30',
    green: 'bg-apple-green/15 text-apple-green border-apple-green/30',
    red: 'bg-apple-red/15 text-apple-red border-apple-red/30',
    purple: 'bg-apple-purple/15 text-apple-purple border-apple-purple/30',
    gray: 'bg-white/10 text-apple-gray-300 border-white/15',
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
