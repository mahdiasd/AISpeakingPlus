import React, { useState, useRef } from 'react';
import {
  Mic,
  Send,
  Keyboard,
  Edit2,
  Trash2,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

/** Downloaded lamp.svg converted to React component for Hint / Suggest */
const LampIcon: React.FC<{ className?: string }> = ({ className = 'w-5 h-5' }) => (
  <svg
    viewBox="0 0 32 32"
    fill="currentColor"
    className={className}
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M16.6099854 2.0199585c-5.1699829-.4599609-9.9899902 3.9700317-9.8800049 9.1600342 0 2.8399658 1.3300171 5.4400024 3.6400146 7.1199951.9000244.6500244 1.5100098 1.6199951 1.75 2.6900024h-.0299683c-.5599976 0-1 .4400024-1 1 0 .5499878.4400024 1 1 1h7.7399902c.5499878 0 1-.4500122 1-1 0-.539978-.4299927-.9700317-.960022-.9899902.2399902-1.0800171.8400269-2.0400391 1.7299805-2.6799927C24 16.5899658 25.3400269 13.8999634 25.2700195 10.9500122 25.1500244 6.25 21.3499756 2.3200073 16.6099854 2.0199585zM19.5999756 9.8299561l-3.6799927 3.6800537c-.1900024.1999512-.4500122.2999878-.7099609.2999878-.25 0-.5100098-.1000366-.7000122-.2999878l-2.1000366-2.0900269c-.3899536-.3900146-.3899536-1.0299683 0-1.4199829.3900146-.3900146 1.0200195-.3900146 1.4200439 0l1.3800049 1.3900146 2.9799805-2.9700317c.3900146-.3900146 1.0200195-.3900146 1.4099731 0C19.9899902 8.8099976 19.9899902 9.4400024 19.5999756 9.8299561zM18.3486328 24.5742188h-4.7792969c-.5527344 0-1 .4472656-1 1s.4472656 1 1 1h4.7792969c.5527344 0 1-.4472656 1-1S18.9013672 24.5742188 18.3486328 24.5742188zM15.9785156 28H15.96875c-.5517578 0-.9951172.4472656-.9951172 1s.453125 1 1.0048828 1c.5527344 0 1-.4472656 1-1S16.53125 28 15.9785156 28z" />
  </svg>
);

export interface ChatBottomBarProps {
  onSendMessage: (text: string) => void;
  onRequestHint: () => void;
  isModelSpeaking: boolean;
  isSending: boolean;
  isLoadingHint: boolean;
  isRecording: boolean;
  isProcessingRecording?: boolean;
  recordingAudioLevel?: number;
  recordingDuration?: number;
  recordingTranscript?: string;
  hintsUsedCount?: number;
  onStartRecording: () => void;
  onStopRecording: () => void;
  onCancelRecording?: () => void;
  onClearTranscript?: () => void;
  recordingError?: string | null;
}

/**
 * 2026 Apple Liquid Glass Floating Dockless Footer for conversation.
 * 100% matching Compose Multiplatform `ChatBottomBar.kt`:
 * - Dockless / Containerless: No outer container pill background.
 * - In Voice Mode: 3 floating buttons grouped closely in center (Hint, 58px Mic with concentric rings, Keyboard/Send).
 * - In Text Mode: Side icons smoothly slide to edges, STT-styled Input Box expands in the middle.
 * - When input is empty, right action button displays Voice (mic); tapping it switches back to voice mode.
 * - When text is entered, right action button smoothly morphs into Send.
 * - Real-time Voice Transcription Capsule without quotation marks.
 */
export const ChatBottomBar: React.FC<ChatBottomBarProps> = ({
  onSendMessage,
  onRequestHint,
  isModelSpeaking,
  isSending,
  isLoadingHint,
  isRecording,
  isProcessingRecording = false,
  recordingTranscript = '',
  hintsUsedCount = 0,
  onStartRecording,
  onStopRecording,
  onClearTranscript,
}) => {
  const [inputMode, setInputMode] = useState<'voice' | 'text'>('voice');
  const [textInput, setTextInput] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  const handleSendText = (e?: React.FormEvent) => {
    e?.preventDefault();
    const clean = textInput.trim();
    if (!clean || isSending || isModelSpeaking || isProcessingRecording) return;
    onSendMessage(clean);
    setTextInput('');
  };

  const handleSendTranscript = () => {
    if (isRecording) {
      onStopRecording();
    }
    const clean = recordingTranscript.trim();
    if (!clean || isSending || isModelSpeaking || isProcessingRecording) return;
    onSendMessage(clean);
    onClearTranscript?.();
  };

  const handleEditTranscript = () => {
    setTextInput(recordingTranscript);
    setInputMode('text');
    setTimeout(() => inputRef.current?.focus(), 120);
  };

  const isBusy = isSending || isModelSpeaking || isProcessingRecording;
  const isInteractive = !isBusy && !isModelSpeaking;
  const hasVoiceTranscript = Boolean(recordingTranscript && recordingTranscript.trim().length > 0);
  const hasInputText = Boolean(textInput && textInput.trim().length > 0);

  return (
    <div className="w-full select-none" dir="ltr">
      {/* 1. Floating Real-Time Voice Transcription Preview Capsule (رونویسی همزمان صدا) */}
      <AnimatePresence>
        {hasVoiceTranscript && inputMode === 'voice' && !isModelSpeaking && (
          <motion.div
            initial={{ opacity: 0, y: 8, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 6, scale: 0.97 }}
            transition={{ type: 'spring', stiffness: 450, damping: 28 }}
            className="mb-2.5 p-3 rounded-2xl bg-[#161a29]/90 backdrop-blur-md border border-white/20 shadow-[0_8px_24px_rgba(0,0,0,0.35)] flex flex-col gap-2"
          >
            {/* Header Row: RTL format (Right = Status Title with Green Dot, Left = Action Pills) */}
            <div className="flex items-center justify-between w-full" dir="rtl">
              {/* Right: Green Live Dot + "رونویسی همزمان صدا" */}
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse shadow-[0_0_8px_rgba(52,211,153,0.8)]" />
                <span className="text-xs font-bold text-emerald-400">
                  رونویسی همزمان صدا
                </span>
              </div>

              {/* Left: Action Buttons ("ویرایش متن", "پاک کردن") */}
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  onClick={handleEditTranscript}
                  disabled={!isInteractive}
                  className="px-2.5 py-1 rounded-xl bg-[#0a84ff]/15 hover:bg-[#0a84ff]/25 active:bg-[#0a84ff]/30 text-[#38bdf8] flex items-center gap-1 transition-all border border-[#0a84ff]/40 text-[11px] font-bold cursor-pointer"
                  title="ویرایش متنی"
                >
                  <Edit2 className="w-3 h-3" />
                  <span>ویرایش متن</span>
                </button>
                <button
                  type="button"
                  onClick={onClearTranscript}
                  disabled={!isInteractive}
                  className="px-2.5 py-1 rounded-xl bg-rose-500/15 hover:bg-rose-500/25 active:bg-rose-500/30 text-rose-300 flex items-center gap-1 transition-all border border-rose-400/40 text-[11px] font-bold cursor-pointer"
                  title="پاک کردن"
                >
                  <Trash2 className="w-3 h-3" />
                  <span>پاک کردن</span>
                </button>
              </div>
            </div>

            {/* Body Row: Plain English Transcribed Text (No quotation marks) + Cyan Cursor */}
            <div
              dir="ltr"
              className="flex items-center gap-1 text-[14px] font-medium text-white px-1 font-sans leading-relaxed text-left"
            >
              <span>{recordingTranscript}</span>
              <span className="w-0.5 h-4 rounded-sm bg-[#38bdf8] animate-pulse shrink-0 inline-block" />
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 2. Floating Dockless Controls (No outer container background, fully floating) */}
      <div className="relative w-full flex flex-col items-center pb-2">
        <motion.div
          layout
          transition={{ type: 'spring', stiffness: 380, damping: 28 }}
          className={`w-full flex items-center justify-between transition-all duration-300 ${
            inputMode === 'voice'
              ? 'max-w-[214px] mx-auto gap-4'
              : 'max-w-full px-3 gap-2.5'
          }`}
        >
          {/* 1. Physically LEFT: Suggest / Hint Lamp Button */}
          <div className="relative shrink-0">
            <motion.button
              type="button"
              whileTap={{ scale: 0.92 }}
              onClick={onRequestHint}
              disabled={!isInteractive || isLoadingHint}
              aria-label="دریافت راهنمایی"
              className={`w-12 h-12 rounded-full border flex items-center justify-center transition-all ${
                isLoadingHint
                  ? 'bg-amber-400/25 border-amber-400/80 text-amber-300 shadow-[0_0_16px_rgba(255,178,54,0.55)] animate-pulse'
                  : hintsUsedCount > 0
                  ? 'bg-gradient-to-b from-amber-400/25 to-amber-500/15 border-amber-400/60 text-amber-300 shadow-[0_0_10px_rgba(255,204,0,0.2)]'
                  : 'bg-white/10 hover:bg-white/15 border-white/20 text-slate-300 shadow-[0_4px_16px_rgba(0,0,0,0.25)]'
              }`}
              title="راهنمای جمله بعدی"
            >
              <LampIcon className="w-5 h-5" />
            </motion.button>

            {hintsUsedCount > 0 && !isLoadingHint && (
              <span className="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-amber-500 text-slate-900 text-[9px] font-bold flex items-center justify-center border border-white/30">
                {hintsUsedCount}
              </span>
            )}
          </div>

          {/* 2. Physically CENTER: Large Voice Button OR STT-styled Input Box */}
          <div className="flex-1 flex items-center justify-center">
            {inputMode === 'voice' ? (
              <div className="relative flex items-center justify-center">
                {isModelSpeaking ? (
                  /* Modern Living Siri Voice Waveform in Subscription Colors */
                  <div className="relative flex items-center justify-center h-12 px-4">
                    <div className="absolute inset-x-0 -inset-y-2 bg-gradient-to-r from-sky-400/30 via-indigo-500/25 to-emerald-400/25 blur-lg rounded-full animate-pulse pointer-events-none" />
                    <div className="relative z-10 flex items-center gap-1.5 h-8">
                      <span className="w-1 rounded-full bg-[#ff7a45] animate-[pulse_0.9s_ease-in-out_infinite] h-3.5" />
                      <span className="w-1 rounded-full bg-[#0ea5e9] animate-[pulse_1.2s_ease-in-out_0.2s_infinite] h-6" />
                      <span className="w-1 rounded-full bg-[#818cf8] animate-[pulse_1.0s_ease-in-out_0.4s_infinite] h-7" />
                      <span className="w-1 rounded-full bg-[#22c55e] animate-[pulse_1.3s_ease-in-out_0.1s_infinite] h-5" />
                      <span className="w-1 rounded-full bg-[#ffb236] animate-[pulse_1.1s_ease-in-out_0.3s_infinite] h-3" />
                    </div>
                  </div>
                ) : isRecording ? (
                  isProcessingRecording ? (
                    /* Preparing State: Unconstrained Expanding Radar Waves in Warm Peach / Amber */
                    <div className="relative w-14 h-14 flex items-center justify-center">
                      <span className="absolute -inset-4 rounded-full border border-orange-400/40 animate-ping pointer-events-none" />
                      <span className="absolute -inset-2 rounded-full border border-amber-400/50 animate-pulse pointer-events-none" />
                      <div className="relative z-10 w-14 h-14 rounded-full bg-gradient-to-tr from-[#ff7a45]/30 to-[#ffb236]/20 border border-amber-400/70 flex items-center justify-center text-amber-300 shadow-[0_0_20px_rgba(255,178,54,0.4)]">
                        <Mic className="w-6 h-6 animate-pulse" />
                      </div>
                    </div>
                  ) : (
                    /* Active Recording: Tri-Color Concentric Waves Overflowing */
                    <div className="relative w-14 h-14 flex items-center justify-center">
                      <span className="absolute -inset-5 rounded-full border border-emerald-400/35 animate-ping pointer-events-none" />
                      <span className="absolute -inset-3 rounded-full border border-sky-400/40 animate-pulse pointer-events-none" />

                      <motion.button
                        type="button"
                        whileTap={{ scale: 0.94 }}
                        onClick={onStopRecording}
                        className="relative z-10 w-14 h-14 rounded-full bg-gradient-to-tr from-[#0ea5e9] via-[#6366f1] to-[#22c55e] text-white flex items-center justify-center border-2 border-sky-300 shadow-[0_0_24px_rgba(14,165,233,0.55)] cursor-pointer"
                        title="لمس برای توقف ضبط"
                      >
                        <Mic className="w-6 h-6 text-white animate-pulse" />
                      </motion.button>
                    </div>
                  )
                ) : (
                  /* Idle State: Prominent 58px Mic Button with Subtle Concentric Glass Rings */
                  <div className="relative flex items-center justify-center">
                    {/* Concentric rings fading into dark background */}
                    <span className="absolute -inset-4 rounded-full border border-sky-400/15 pointer-events-none" />
                    <span className="absolute -inset-2 rounded-full border border-sky-400/25 pointer-events-none" />

                    <motion.button
                      type="button"
                      whileTap={{ scale: 0.94 }}
                      onClick={onStartRecording}
                      disabled={!isInteractive}
                      className="relative z-10 w-14 h-14 rounded-full bg-gradient-to-tr from-[#0ea5e9]/30 via-[#101c2e]/90 to-[#0b111f]/90 hover:brightness-110 active:brightness-95 text-white border border-sky-400/60 shadow-[0_4px_20px_rgba(14,165,233,0.3)] flex items-center justify-center cursor-pointer"
                      title="لمس برای صحبت"
                    >
                      <Mic className="w-6 h-6 text-sky-300" />
                    </motion.button>
                  </div>
                )}
              </div>
            ) : (
              /* Text Mode STT-styled Input Box */
              <form onSubmit={handleSendText} className="w-full h-12 flex items-center" dir="ltr">
                <input
                  ref={inputRef}
                  type="text"
                  value={textInput}
                  onChange={(e) => setTextInput(e.target.value)}
                  placeholder="Type in English..."
                  disabled={!isInteractive}
                  className="w-full h-12 px-4 rounded-2xl bg-[#161a29]/90 text-white placeholder-slate-400 text-[14px] border border-white/20 focus:outline-none focus:border-[#0a84ff]/70 focus:bg-[#1a2035]/95 shadow-[0_4px_16px_rgba(0,0,0,0.25)] transition-all text-left font-sans"
                />
              </form>
            )}
          </div>

          {/* 3. Physically RIGHT: Action Button (Send / Keyboard / Voice) */}
          <div className="relative shrink-0">
            {inputMode === 'voice' ? (
              hasVoiceTranscript ? (
                /* Send Button when voice transcript is ready */
                <motion.button
                  type="button"
                  whileTap={{ scale: 0.92 }}
                  onClick={handleSendTranscript}
                  disabled={!isInteractive}
                  aria-label="ارسال پیام"
                  className="w-12 h-12 rounded-full bg-gradient-to-tr from-[#0a84ff] to-[#0066cc] hover:brightness-110 text-white border border-white/30 flex items-center justify-center shadow-[0_4px_16px_rgba(10,132,255,0.45)] transition-all cursor-pointer"
                  title="ارسال"
                >
                  <Send className="w-5 h-5 ml-0.5" />
                </motion.button>
              ) : (
                /* Keyboard Toggle when idle in Voice mode */
                <motion.button
                  type="button"
                  whileTap={{ scale: 0.92 }}
                  onClick={() => {
                    setInputMode('text');
                    setTimeout(() => inputRef.current?.focus(), 120);
                  }}
                  disabled={!isInteractive}
                  aria-label="تغییر به تایپ متنی"
                  className="w-12 h-12 rounded-full bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/20 flex items-center justify-center text-white shadow-[0_4px_16px_rgba(0,0,0,0.25)] transition-all cursor-pointer"
                  title="تایپ با کیبورد"
                >
                  <Keyboard className="w-5 h-5" />
                </motion.button>
              )
            ) : hasInputText ? (
              /* Send Button in Text Mode when user typed text */
              <motion.button
                type="button"
                whileTap={{ scale: 0.92 }}
                onClick={handleSendText}
                disabled={!isInteractive}
                aria-label="ارسال پیام"
                className="w-12 h-12 rounded-full bg-gradient-to-tr from-[#0a84ff] to-[#0066cc] hover:brightness-110 text-white border border-white/30 flex items-center justify-center shadow-[0_4px_16px_rgba(10,132,255,0.45)] transition-all cursor-pointer"
                title="ارسال"
              >
                <Send className="w-5 h-5 ml-0.5" />
              </motion.button>
            ) : (
              /* Voice Button in Text Mode when input is empty (tapping switches back to voice mode!) */
              <motion.button
                type="button"
                whileTap={{ scale: 0.92 }}
                onClick={() => setInputMode('voice')}
                disabled={!isInteractive}
                aria-label="تغییر به مکالمه صوتی"
                className="w-12 h-12 rounded-full bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/20 flex items-center justify-center text-[#38bdf8] shadow-[0_4px_16px_rgba(0,0,0,0.25)] transition-all cursor-pointer"
                title="مکالمه صوتی"
              >
                <Mic className="w-5 h-5" />
              </motion.button>
            )}
          </div>
        </motion.div>

        {/* Layer C: Attached Live Badges (Floats directly under the center Voice button in Voice mode) */}
        {inputMode === 'voice' && isModelSpeaking && (
          <div className="mt-1">
            <div className="px-2.5 py-0.5 rounded-full bg-[#0a1726]/95 border border-sky-400/70 flex items-center gap-1 text-[10px] font-bold text-sky-400 shadow-[0_0_8px_rgba(14,165,233,0.35)]">
              <span className="w-1.5 h-1.5 rounded-full bg-sky-400 animate-pulse" />
              <span>دستیار در حال صحبت…</span>
            </div>
          </div>
        )}

        {inputMode === 'voice' && isRecording && !isProcessingRecording && (
          <div className="mt-1">
            <button
              type="button"
              onClick={onStopRecording}
              className="px-2 py-0.5 rounded-full bg-[#0d1f17]/95 border border-emerald-400/70 flex items-center gap-1 text-[10px] font-bold text-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.35)] cursor-pointer"
            >
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
              <span>در حال شنیدن</span>
            </button>
          </div>
        )}

        {inputMode === 'voice' && isRecording && isProcessingRecording && (
          <div className="mt-1">
            <button
              type="button"
              onClick={onStopRecording}
              className="px-2 py-0.5 rounded-full bg-[#26150d]/95 border border-orange-400/70 flex items-center gap-1 text-[10px] font-bold text-orange-300 shadow-[0_0_8px_rgba(255,122,69,0.35)] cursor-pointer"
            >
              <span className="w-1.5 h-1.5 rounded-full bg-orange-400 animate-pulse" />
              <span>آماده‌سازی میکروفون…</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
