import React from 'react';

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(({
  label,
  error,
  helperText,
  className = '',
  children,
  id,
  ...props
}, ref) => {
  const selectId = id || (label ? `select-${label.replace(/\s+/g, '-').toLowerCase()}` : undefined);

  return (
    <div className="w-full">
      {label && (
        <label htmlFor={selectId} className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
          {label}
        </label>
      )}
      <div className="relative rounded-xl">
        <select
          ref={ref}
          id={selectId}
          className={`w-full rounded-xl bg-white dark:bg-[#24262b] border text-slate-900 dark:text-[#f1f3f5] text-sm py-2.5 transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 ${
            error
              ? 'border-rose-400 dark:border-rose-500/50 focus:border-rose-500'
              : 'border-slate-300 dark:border-[#363842] hover:border-slate-400 dark:hover:border-[#474a56] focus:border-indigo-600 dark:focus:border-indigo-500'
          } ${className}`}
          {...props}
        >
          {children}
        </select>
      </div>
      {error && <p className="mt-1.5 text-xs text-rose-600 dark:text-rose-400 leading-tight">{error}</p>}
      {!error && helperText && <p className="mt-1.5 text-xs text-slate-500 dark:text-slate-400 leading-tight">{helperText}</p>}
    </div>
  );
});

Select.displayName = 'Select';
