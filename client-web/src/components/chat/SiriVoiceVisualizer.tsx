import React from 'react';
import { motion } from 'motion/react';
import { Sparkles } from 'lucide-react';

export interface SiriVoiceVisualizerProps {
  isRecording: boolean;
  isProcessing: boolean;
  audioLevel: number; // 0.0 to 1.0
  recordingDuration?: number; // seconds
  transcript?: string;
  onCancel?: () => void;
}

export const SiriVoiceVisualizer: React.FC<SiriVoiceVisualizerProps> = ({
  isRecording,
  isProcessing,
  audioLevel,
  recordingDuration = 0,
  transcript = '',
  onCancel,
}) => {
  // Format MM:SS
  const mins = Math.floor(recordingDuration / 60);
  const secs = recordingDuration % 60;
  const timeFormatted = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;

  // Multipliers for 7 dynamic visualizer waveform bars
  const barMultipliers = [0.4, 0.75, 1.2, 1.6, 1.3, 0.8, 0.45];

  if (isProcessing) {
    return (
      <div className="relative overflow-hidden rounded-3xl p-4 apple-glass bg-[#181a26]/90 border border-white/20 shadow-apple-lg backdrop-blur-2xl text-center space-y-3 select-none">
        {/* Specular Edge */}
        <div className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none" />

        {/* Apple Intelligence Thinking Orb */}
        <div className="relative flex items-center justify-center py-2">
          {/* Expanding Pulsing Ring 1 */}
          <motion.div
            animate={{ scale: [1, 1.6, 1], opacity: [0.5, 0, 0.5] }}
            transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
            className="absolute w-12 h-12 rounded-full bg-gradient-to-r from-apple-blue/40 to-apple-purple/40 blur-md pointer-events-none"
          />
          {/* Expanding Pulsing Ring 2 */}
          <motion.div
            animate={{ scale: [1, 1.3, 1], opacity: [0.7, 0.2, 0.7] }}
            transition={{ duration: 1.5, repeat: Infinity, ease: 'easeInOut', delay: 0.3 }}
            className="absolute w-10 h-10 rounded-full bg-gradient-to-r from-apple-teal/50 to-apple-indigo/50 blur-sm pointer-events-none"
          />

          {/* Central Iridescent Orb */}
          <div className="relative w-8 h-8 rounded-full bg-gradient-to-tr from-apple-blue via-apple-indigo to-pink-500 shadow-[0_0_16px_rgba(88,86,214,0.6)] flex items-center justify-center">
            <Sparkles className="w-4 h-4 text-white animate-spin" />
          </div>
        </div>

        <div className="space-y-1">
          <p className="text-xs font-bold text-white tracking-wide">
            در حال تبدیل گفتار با هوش مصنوعی...
          </p>
          <p className="text-[10px] text-apple-gray-400 font-sans">
            درحال استخراج دقیق تلفظ انگلیسی با مدل Sherpa-ONNX
          </p>
        </div>
      </div>
    );
  }

  if (!isRecording) return null;

  return (
    <div className="relative overflow-hidden rounded-3xl p-4 apple-glass bg-[#161824]/92 border border-apple-blue/30 shadow-apple-lg backdrop-blur-2xl space-y-3 select-none">
      {/* Specular Edge */}
      <div className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none" />

      {/* Top Header: Recording Status & Duration Pill */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="relative flex h-2.5 w-2.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-apple-red opacity-75" />
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-apple-red shadow-[0_0_8px_#ff3b30]" />
          </span>
          <span className="text-xs font-bold text-slate-100">در حال ضبط صدای شما...</span>
        </div>

        <div className="flex items-center gap-2">
          {/* Duration Pill */}
          <span className="font-mono text-[11px] font-bold px-2 py-0.5 rounded-full bg-white/10 text-white border border-white/10">
            {timeFormatted}
          </span>

          {/* Cancel button */}
          {onCancel && (
            <button
              type="button"
              onClick={onCancel}
              className="text-[11px] text-apple-gray-400 hover:text-white px-2 py-0.5 rounded-full bg-white/5 hover:bg-white/10 transition-colors"
            >
              انصراف
            </button>
          )}
        </div>
      </div>

      {/* Siri Dynamic Sound Waveform Bars */}
      <div className="h-12 flex items-center justify-center gap-1.5 px-4 bg-black/25 rounded-2xl border border-white/5">
        {barMultipliers.map((mult, idx) => {
          // Dynamic height based on audio level + multiplier (min 6px, max 40px)
          const computedHeight = Math.min(42, Math.max(6, Math.round(audioLevel * 45 * mult + 6)));
          const hueGradients = [
            'from-apple-teal to-apple-blue',
            'from-apple-blue to-apple-indigo',
            'from-apple-indigo to-apple-purple',
            'from-apple-purple to-pink-500',
            'from-pink-500 to-apple-orange',
            'from-apple-indigo to-apple-blue',
            'from-apple-blue to-apple-teal',
          ];

          return (
            <motion.div
              key={idx}
              animate={{ height: computedHeight }}
              transition={{ type: 'spring', stiffness: 500, damping: 25 }}
              className={`w-1.5 rounded-full bg-gradient-to-t ${hueGradients[idx % hueGradients.length]} shadow-[0_0_8px_rgba(0,122,255,0.4)]`}
            />
          );
        })}
      </div>

      {/* Live Interim Transcript or Prompt */}
      <div
        className="text-xs font-mono text-slate-200 min-h-[26px] px-3 py-1.5 rounded-xl bg-white/5 border border-white/5 select-text overflow-hidden text-center truncate"
        dir="ltr"
      >
        {transcript ? (
          <span className="text-white font-medium">"{transcript}"</span>
        ) : (
          <span className="text-apple-gray-400 italic">Listening to your English sentence...</span>
        )}
      </div>
    </div>
  );
};
