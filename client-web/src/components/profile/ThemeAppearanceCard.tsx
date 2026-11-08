import React from 'react';
import { Palette, Monitor, Moon, Sun, Sparkles, Volume2 } from 'lucide-react';
import { CupertinoSegmentedControl, CupertinoSwitch } from '../cupertino';

export type ThemeMode = 'system' | 'dark' | 'light';

interface ThemeAppearanceCardProps {
  themeMode: ThemeMode;
  onThemeModeChange: (mode: ThemeMode) => void;
  glassEffect: boolean;
  onGlassEffectChange: (val: boolean) => void;
  soundFeedback: boolean;
  onSoundFeedbackChange: (val: boolean) => void;
}

export const ThemeAppearanceCard: React.FC<ThemeAppearanceCardProps> = ({
  themeMode,
  onThemeModeChange,
  glassEffect,
  onGlassEffectChange,
  soundFeedback,
  onSoundFeedbackChange,
}) => {
  return (
    <div className="space-y-3">
      {/* Section Header */}
      <div className="flex items-center gap-2 px-1">
        <Palette className="w-4 h-4 text-apple-blue" />
        <h3 className="text-sm font-bold text-white">تنظیمات و ظاهر برنامه</h3>
      </div>

      <div className="rounded-[24px] apple-glass-card p-4 shadow-apple-card space-y-4">
        {/* Apple 3-Way Segmented Control */}
        <div className="space-y-2">
          <label className="text-xs font-semibold text-apple-gray-300">
            تم رابط کاربری
          </label>
          <CupertinoSegmentedControl<ThemeMode>
            value={themeMode}
            onChange={onThemeModeChange}
            items={[
              {
                id: 'system',
                label: 'سیستم',
                icon: <Monitor className="w-3.5 h-3.5" />,
              },
              {
                id: 'dark',
                label: 'تاریک',
                icon: <Moon className="w-3.5 h-3.5" />,
              },
              {
                id: 'light',
                label: 'روشن',
                icon: <Sun className="w-3.5 h-3.5" />,
              },
            ]}
          />
        </div>

        <div className="border-t border-white/10 pt-3 space-y-3">
          {/* Liquid Glass Toggle */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-apple-blue/15 border border-apple-blue/30 flex items-center justify-center text-apple-blue">
                <Sparkles className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-semibold text-white">جلوه شیشه‌ای مایع (Liquid Glass)</p>
                <p className="text-[11px] text-apple-gray-400">بازتاب نور فریم و تار شدن پس‌زمینه</p>
              </div>
            </div>
            <CupertinoSwitch checked={glassEffect} onChange={onGlassEffectChange} />
          </div>

          {/* Sound Feedback Toggle */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-purple-500/15 border border-purple-500/30 flex items-center justify-center text-purple-400">
                <Volume2 className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-semibold text-white">افکت‌های صوتی تعاملی</p>
                <p className="text-[11px] text-apple-gray-400">صدای کلیک، تبریک و دریافت ستاره</p>
              </div>
            </div>
            <CupertinoSwitch checked={soundFeedback} onChange={onSoundFeedbackChange} />
          </div>
        </div>
      </div>
    </div>
  );
};
