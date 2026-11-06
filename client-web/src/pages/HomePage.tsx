import React, { useState } from 'react';
import { motion } from 'motion/react';
import confetti from 'canvas-confetti';
import {
  Mic,
  Volume2,
  Sparkles,
  Award,
  Layers,
  Settings,
  Flame,
  CheckCircle2,
  PlayCircle,
} from 'lucide-react';
import {
  CupertinoNavBar,
  CupertinoCard,
  CupertinoButton,
  CupertinoSegmentedControl,
  CupertinoSwitch,
} from '@/components/cupertino';

export const HomePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'conversation' | 'stages' | 'assessment'>('conversation');
  const [isRecording, setIsRecording] = useState(false);
  const [glassMode, setGlassMode] = useState(true);
  const [soundFeedback, setSoundFeedback] = useState(true);

  const handleCelebrate = () => {
    confetti({
      particleCount: 60,
      spread: 70,
      origin: { y: 0.7 },
      colors: ['#007aff', '#34c759', '#ff9500', '#5856d6'],
    });
  };

  return (
    <div className="flex flex-col min-h-screen bg-[#0c0c0e] text-white">
      {/* 1. Cupertino Top App Bar */}
      <CupertinoNavBar
        title="AI Speaking Plus"
        subtitle="نسخه پیشرفته وب (Apple Liquid Glass)"
        onBack={() => alert('بازگشت به منوی قبل')}
        endSlot={
          <button
            type="button"
            className="p-2 text-apple-gray-400 hover:text-white active:scale-95 transition-transform"
            onClick={() => alert('تنظیمات اسپیکینگ')}
          >
            <Settings className="w-5 h-5" />
          </button>
        }
      />

      {/* 2. Main Scrollable Container */}
      <main className="flex-1 w-full max-w-lg mx-auto p-4 space-y-4 pb-20">
        {/* Segmented Control */}
        <CupertinoSegmentedControl
          value={activeTab}
          onChange={setActiveTab}
          items={[
            { id: 'conversation', label: 'مکالمه هوشمند', icon: <Mic className="w-3.5 h-3.5" /> },
            { id: 'stages', label: 'مراحل یادگیری', icon: <Layers className="w-3.5 h-3.5" /> },
            { id: 'assessment', label: 'سطح‌سنجی', icon: <Award className="w-3.5 h-3.5" /> },
          ]}
        />

        {/* Hero Card: Apple Liquid Glass Material */}
        <CupertinoCard
          variant={glassMode ? 'glass' : 'solid'}
          withSpecularHighlight={glassMode}
          className="relative overflow-hidden group"
        >
          {/* Subtle Ambient Light Glow in Background */}
          <div className="absolute -top-12 -right-12 w-44 h-44 bg-apple-blue/20 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-10 -left-10 w-44 h-44 bg-apple-indigo/20 rounded-full blur-3xl pointer-events-none" />

          <div className="flex items-start justify-between">
            <div className="space-y-1">
              <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-apple-blue/20 text-apple-blue text-xs font-semibold">
                <Sparkles className="w-3.5 h-3.5" />
                <span>موتور هوش مصنوعی فعال</span>
              </div>
              <h2 className="text-xl font-bold pt-1">تمرین مکالمه روزانه</h2>
              <p className="text-xs text-apple-gray-400 leading-relaxed max-w-xs">
                میکروفون را لمس کنید و به انگلیسی صحبت کنید. صدای شما تحلیل و امتیازدهی می‌شود.
              </p>
            </div>

            <div className="flex items-center gap-1 bg-white/10 px-2 py-1 rounded-full text-xs text-apple-yellow font-medium">
              <Flame className="w-3.5 h-3.5 fill-apple-yellow" />
              <span>۷ روز</span>
            </div>
          </div>

          {/* Interactive Microphone Button */}
          <div className="my-6 flex flex-col items-center justify-center">
            <motion.button
              whileTap={{ scale: 0.92 }}
              onClick={() => setIsRecording(!isRecording)}
              className={`relative flex items-center justify-center w-24 h-24 rounded-full transition-all duration-300 ${
                isRecording
                  ? 'bg-apple-red shadow-[0_0_35px_rgba(255,59,48,0.5)] ring-4 ring-apple-red/40 animate-pulse'
                  : 'bg-apple-blue shadow-[0_0_35px_rgba(0,122,255,0.4)] hover:brightness-110'
              }`}
            >
              <Mic className="w-10 h-10 text-white" />
              {isRecording && (
                <span className="absolute -bottom-7 text-[11px] text-apple-red font-bold tracking-wider">
                  در حال گوش دادن...
                </span>
              )}
            </motion.button>
          </div>

          {/* Action Row */}
          <div className="flex items-center gap-2 pt-2">
            <CupertinoButton
              variant="filled"
              size="md"
              fullWidth
              icon={<PlayCircle className="w-4 h-4" />}
              onClick={handleCelebrate}
            >
              ثبت موفقیت و جشن
            </CupertinoButton>
            <CupertinoButton
              variant="glass"
              size="md"
              icon={<Volume2 className="w-4 h-4" />}
              onClick={() => alert('پخش نمونه تلفظ بومی')}
            >
              شنیدن
            </CupertinoButton>
          </div>
        </CupertinoCard>

        {/* Feature Demonstration List */}
        <div className="space-y-2">
          <h3 className="text-xs font-semibold text-apple-gray-400 px-2">
            تنظیمات تجربه کاربری (Apple Settings Style)
          </h3>

          <CupertinoCard variant="solid" className="p-0 overflow-hidden divide-y divide-white/10">
            {/* Setting Item 1 */}
            <div className="flex items-center justify-between p-4">
              <div className="space-y-0.5">
                <span className="text-sm font-medium">افکت شیشه‌ای اپل (Liquid Glass)</span>
                <p className="text-xs text-apple-gray-400">
                  بلور سخت‌افزاری با رندرینگ ۶۰ تا ۱۲۰ فریم بر ثانیه
                </p>
              </div>
              <CupertinoSwitch checked={glassMode} onChange={setGlassMode} />
            </div>

            {/* Setting Item 2 */}
            <div className="flex items-center justify-between p-4">
              <div className="space-y-0.5">
                <span className="text-sm font-medium">بازخورد صوتی زنده (Haptics/Audio)</span>
                <p className="text-xs text-apple-gray-400">پخش افکت‌های صوتی طبیعی حین کلیک</p>
              </div>
              <CupertinoSwitch checked={soundFeedback} onChange={setSoundFeedback} />
            </div>
          </CupertinoCard>
        </div>

        {/* Architecture Benchmark Card */}
        <CupertinoCard variant="glass" className="space-y-2.5">
          <div className="flex items-center gap-2 text-apple-green text-sm font-bold">
            <CheckCircle2 className="w-4 h-4" />
            <span>شاخص‌های وب‌اپ مدرن (React + Cupertino)</span>
          </div>
          <div className="grid grid-cols-3 gap-2 pt-1 text-center">
            <div className="bg-white/5 p-2 rounded-2xl border border-white/5">
              <div className="text-base font-extrabold text-white">~۶۰ KB</div>
              <div className="text-[10px] text-apple-gray-400">حجم لود (Gzip)</div>
            </div>
            <div className="bg-white/5 p-2 rounded-2xl border border-white/5">
              <div className="text-base font-extrabold text-apple-green">۱۲۰ Hz</div>
              <div className="text-[10px] text-apple-gray-400">فریم‌ریت اسکرول</div>
            </div>
            <div className="bg-white/5 p-2 rounded-2xl border border-white/5">
              <div className="text-base font-extrabold text-apple-blue">&lt; ۰.۳s</div>
              <div className="text-[10px] text-apple-gray-400">سرعت لود اولیه</div>
            </div>
          </div>
        </CupertinoCard>
      </main>
    </div>
  );
};
