import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoCardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'glass' | 'solid' | 'elevated';
  withSpecularHighlight?: boolean;
  children: React.ReactNode;
}

export const CupertinoCard: React.FC<CupertinoCardProps> = ({
  variant = 'glass',
  withSpecularHighlight = true,
  className,
  children,
  ...props
}) => {
  const variantStyles = {
    glass: 'apple-glass text-white',
    solid: 'bg-apple-gray-900 border border-white/10 text-white shadow-apple-md',
    elevated: 'bg-apple-dark-elevated border border-white/12 text-white shadow-apple-lg',
  };

  return (
    <div
      className={twMerge(
        clsx(
          'relative rounded-3xl p-5 overflow-hidden transition-all duration-200',
          variantStyles[variant],
          className
        )
      )}
      {...props}
    >
      {/* Specular Top Edge Light Bar */}
      {withSpecularHighlight && variant === 'glass' && (
        <div
          aria-hidden="true"
          className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
        />
      )}
      <div className="relative z-10">{children}</div>
    </div>
  );
};
