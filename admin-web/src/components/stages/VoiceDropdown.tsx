import React from 'react';
import { KOKORO_VOICES } from '../../types';
import { Volume2, ChevronDown } from 'lucide-react';
import { Badge } from '../ui/Badge';

interface VoiceDropdownProps {
  value?: string | null;
  onChange: (voiceId: string) => void;
  label?: string;
  error?: string;
}

export const VoiceDropdown: React.FC<VoiceDropdownProps> = ({
  value,
  onChange,
  label = 'صدای هوش مصنوعی (Kokoro TTS)',
  error,
}) => {
  const selectedVoice = KOKORO_VOICES.find((v) => v.id === value) || null;

  return (
    <div className="w-full">
      {label && (
        <div className="flex items-center justify-between mb-1.5">
          <label className="text-sm font-medium text-slate-700 dark:text-slate-300 flex items-center gap-1.5 select-none">
            <Volume2 className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
            {label}
          </label>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-normal">۱۱ صدای Kokoro TTS</span>
        </div>
      )}

      <div className="relative">
        <select
          value={value || ''}
          onChange={(e) => onChange(e.target.value)}
          className={`w-full appearance-none rounded-xl bg-white dark:bg-[#0E1422] border text-slate-900 dark:text-slate-100 text-sm px-3.5 py-2.5 pl-10 pr-4 transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-offset-white dark:focus:ring-offset-[#0B0F17] ${
            error
              ? 'border-rose-400 dark:border-rose-500/50 focus:border-rose-500 focus:ring-rose-200 dark:focus:ring-rose-500/30'
              : 'border-slate-300 dark:border-slate-800 hover:border-slate-400 dark:hover:border-slate-700 focus:border-indigo-600 dark:focus:border-indigo-500 focus:ring-indigo-100 dark:focus:ring-indigo-500/25'
          }`}
        >
          <option value="" disabled className="bg-white dark:bg-[#0E1422] text-slate-400">
            -- انتخاب صدای گوینده --
          </option>
          <optgroup label="🇺🇸 صداهای زن آمریکایی (US Female)" className="bg-slate-50 dark:bg-[#111726] text-indigo-700 dark:text-indigo-300 font-semibold">
            {KOKORO_VOICES.filter((v) => v.accent === 'US' && v.gender === 'Woman').map((v) => (
              <option key={v.id} value={v.id} className="bg-white dark:bg-[#0E1422] text-slate-900 dark:text-slate-100 py-1 font-normal">
                {v.name} ({v.id}) - {v.description}
              </option>
            ))}
          </optgroup>
          <optgroup label="🇺🇸 صداهای مرد آمریکایی (US Male)" className="bg-slate-50 dark:bg-[#111726] text-indigo-700 dark:text-indigo-300 font-semibold">
            {KOKORO_VOICES.filter((v) => v.accent === 'US' && v.gender === 'Man').map((v) => (
              <option key={v.id} value={v.id} className="bg-white dark:bg-[#0E1422] text-slate-900 dark:text-slate-100 py-1 font-normal">
                {v.name} ({v.id}) - {v.description}
              </option>
            ))}
          </optgroup>
          <optgroup label="🇬🇧 صداهای زن بریتانیایی (UK Female)" className="bg-slate-50 dark:bg-[#111726] text-indigo-700 dark:text-indigo-300 font-semibold">
            {KOKORO_VOICES.filter((v) => v.accent === 'UK' && v.gender === 'Woman').map((v) => (
              <option key={v.id} value={v.id} className="bg-white dark:bg-[#0E1422] text-slate-900 dark:text-slate-100 py-1 font-normal">
                {v.name} ({v.id}) - {v.description}
              </option>
            ))}
          </optgroup>
          <optgroup label="🇬🇧 صداهای مرد بریتانیایی (UK Male)" className="bg-slate-50 dark:bg-[#111726] text-indigo-700 dark:text-indigo-300 font-semibold">
            {KOKORO_VOICES.filter((v) => v.accent === 'UK' && v.gender === 'Man').map((v) => (
              <option key={v.id} value={v.id} className="bg-white dark:bg-[#0E1422] text-slate-900 dark:text-slate-100 py-1 font-normal">
                {v.name} ({v.id}) - {v.description}
              </option>
            ))}
          </optgroup>
        </select>

        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
          <ChevronDown className="w-4 h-4" />
        </div>
      </div>

      {/* Selected Voice Info Pill */}
      {selectedVoice && (
        <div className="mt-2.5 flex items-center gap-2 p-2.5 rounded-xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 text-xs">
          <Badge variant={selectedVoice.accent === 'US' ? 'info' : 'purple'} size="sm">
            {selectedVoice.accent === 'US' ? '🇺🇸 آمریکا' : '🇬🇧 بریتانیا'}
          </Badge>
          <Badge variant={selectedVoice.gender === 'Woman' ? 'warning' : 'neutral'} size="sm">
            {selectedVoice.gender === 'Woman' ? 'زن' : 'مرد'}
          </Badge>
          <span className="text-slate-700 dark:text-slate-300 font-medium">{selectedVoice.description}</span>
        </div>
      )}

      {error && <p className="mt-1.5 text-xs text-rose-600 dark:text-rose-400">{error}</p>}
    </div>
  );
};
