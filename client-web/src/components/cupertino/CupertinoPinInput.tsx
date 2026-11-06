import React, { useRef, useEffect } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CupertinoPinInputProps {
  length?: number;
  value: string;
  onChange: (value: string) => void;
  onComplete?: (value: string) => void;
  disabled?: boolean;
  error?: boolean;
  autoFocus?: boolean;
  className?: string;
}

export const CupertinoPinInput: React.FC<CupertinoPinInputProps> = ({
  length = 5,
  value,
  onChange,
  onComplete,
  disabled = false,
  error = false,
  autoFocus = true,
  className,
}) => {
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Split string into array of single characters
  const digits = Array.from({ length }, (_, i) => value[i] || '');

  useEffect(() => {
    if (autoFocus && inputRefs.current[0] && !disabled) {
      inputRefs.current[0].focus();
    }
  }, [autoFocus, disabled]);

  const handleInputChange = (index: number, e: React.ChangeEvent<HTMLInputElement>) => {
    const rawVal = e.target.value;
    // Extract only digits
    const cleanDigit = rawVal.replace(/\D/g, '').slice(-1);

    const newDigits = [...digits];
    newDigits[index] = cleanDigit;
    const combined = newDigits.join('');

    onChange(combined);

    if (cleanDigit && index < length - 1) {
      // Auto-advance to next input
      inputRefs.current[index + 1]?.focus();
    }

    if (combined.length === length) {
      onComplete?.(combined);
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      if (!digits[index] && index > 0) {
        // Move back and clear previous input
        const newDigits = [...digits];
        newDigits[index - 1] = '';
        onChange(newDigits.join(''));
        inputRefs.current[index - 1]?.focus();
      }
    } else if (e.key === 'ArrowLeft' && index < length - 1) {
      inputRefs.current[index + 1]?.focus();
    } else if (e.key === 'ArrowRight' && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, length);
    if (!pasted) return;

    onChange(pasted);
    const targetIndex = Math.min(pasted.length, length - 1);
    inputRefs.current[targetIndex]?.focus();

    if (pasted.length === length) {
      onComplete?.(pasted);
    }
  };

  return (
    <div
      dir="ltr"
      className={twMerge(clsx('flex items-center justify-center gap-2.5 sm:gap-3 select-none', className))}
      onPaste={handlePaste}
    >
      {Array.from({ length }).map((_, index) => {
        const isFilled = Boolean(digits[index]);
        return (
          <input
            key={index}
            ref={(el) => {
              inputRefs.current[index] = el;
            }}
            type="text"
            inputMode="numeric"
            pattern="[0-9]*"
            maxLength={1}
            value={digits[index]}
            disabled={disabled}
            onChange={(e) => handleInputChange(index, e)}
            onKeyDown={(e) => handleKeyDown(index, e)}
            className={twMerge(
              clsx(
                'w-12 h-14 sm:w-14 sm:h-16 rounded-2xl text-center text-xl sm:text-2xl font-bold font-mono transition-all duration-150 outline-none',
                'bg-white/5 border text-white',
                isFilled
                  ? 'border-apple-blue/80 shadow-[0_0_12px_rgba(0,122,255,0.3)] bg-white/10'
                  : 'border-white/10 hover:border-white/20',
                error && 'border-apple-red text-apple-red shadow-[0_0_12px_rgba(255,59,48,0.3)]',
                'focus:border-apple-blue focus:ring-2 focus:ring-apple-blue/40 focus:scale-[1.03]',
                disabled && 'opacity-40 cursor-not-allowed'
              )
            )}
          />
        );
      })}
    </div>
  );
};
