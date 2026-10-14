import React from 'react';

export interface SwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  disabled?: boolean;
  size?: 'sm' | 'md';
  className?: string;
  title?: string;
  id?: string;
}

export const Switch: React.FC<SwitchProps> = ({
  checked,
  onChange,
  disabled = false,
  size = 'md',
  className = '',
  title,
  id,
}) => {
  const isSm = size === 'sm';

  // Explicit LTR styling guarantees the thumb coordinates never flip or break in RTL layouts
  const trackStyle: React.CSSProperties = {
    direction: 'ltr',
    unicodeBidi: 'isolate',
  };

  const thumbStyle: React.CSSProperties = {
    transform: checked
      ? isSm
        ? 'translateX(16px)'
        : 'translateX(20px)'
      : 'translateX(0px)',
    transition: 'transform 200ms cubic-bezier(0.16, 1, 0.3, 1), background-color 200ms ease',
  };

  return (
    <button
      id={id}
      type="button"
      role="switch"
      aria-checked={checked}
      title={title}
      disabled={disabled}
      dir="ltr"
      style={trackStyle}
      onClick={(e) => {
        e.stopPropagation();
        if (!disabled) {
          onChange(!checked);
        }
      }}
      className={`box-border relative inline-flex shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:ring-offset-2 select-none active:scale-[0.96] ${
        isSm ? 'h-5 w-9' : 'h-6 w-11'
      } ${
        checked ? 'bg-emerald-600 dark:bg-emerald-500' : 'bg-slate-300 dark:bg-slate-700'
      } ${disabled ? 'opacity-50 cursor-not-allowed active:scale-100' : ''} ${className}`}
    >
      <span
        aria-hidden="true"
        style={thumbStyle}
        className={`box-border pointer-events-none inline-block rounded-full bg-white shadow-sm ring-0 ${
          isSm ? 'h-4 w-4' : 'h-5 w-5'
        }`}
      />
    </button>
  );
};
