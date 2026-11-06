import React from 'react';
import { motion } from 'motion/react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoSwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  disabled?: boolean;
  className?: string;
  id?: string;
}

export const CupertinoSwitch: React.FC<CupertinoSwitchProps> = ({
  checked,
  onChange,
  disabled = false,
  className,
  id,
}) => {
  return (
    <button
      id={id}
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={() => !disabled && onChange(!checked)}
      className={twMerge(
        clsx(
          'relative inline-flex h-7 w-12 shrink-0 cursor-pointer rounded-full p-0.5 transition-colors duration-250 ease-in-out focus:outline-none touch-manipulation',
          checked ? 'bg-apple-green' : 'bg-apple-gray-700',
          disabled && 'opacity-40 cursor-not-allowed',
          className
        )
      )}
    >
      <motion.span
        animate={{
          x: checked ? (document.dir === 'rtl' ? -20 : 20) : 0,
        }}
        transition={{ type: 'spring', stiffness: 500, damping: 30 }}
        className="pointer-events-none block h-6 w-6 rounded-full bg-white shadow-apple-sm"
      />
    </button>
  );
};
