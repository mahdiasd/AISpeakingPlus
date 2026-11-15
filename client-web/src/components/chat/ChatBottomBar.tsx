import React, { useState, useRef } from 'react';
import {
  Mic,
  Send,
  Keyboard,
  Lightbulb,
  Edit2,
  Trash2,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

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
 * 2026 Apple Liquid Glass Floating Dock for conversation.
 * 100% matching Compose Multiplatform `ChatBottomBar.kt`:
 * - Compact visionOS rounded capsule dock (~52px).
 * - Real-time Voice Transcription Card (رونویسی همزمان صدا) without quotation marks.
 * - Center Glowing Mic button with Siri-like concentric expanding pulse rings radiating
 *   outside container boundaries.
 * - Attached live badge ("در حال شنیدن") floating at bottom center.
 * - Send button is ALWAYS on the physical RIGHT side.
 * - Under-text removed in idle state for clean, icon-aligned height.
 * - Smooth mode switching between Voice and Text.
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
            className="mb-2 p-3 rounded-2xl bg-[#161a29]/90 backdrop-blur-md border border-white/20 shadow-[0_8px_24px_rgba(0,0,0,0.35)] flex flex-col gap-2"
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
                  className="px-2.5 py-1 rounded-xl bg-[#0a84ff]/15 hover:bg-[#0a84ff]/25 active:bg-[#0a84ff]/30 text-[#38bdf8] flex items-center gap-1 transition-all border border-[#0a84ff]/40 text-[11px] font-bold"
                  title="ویرایش متنی"
                >
                  <Edit2 className="w-3 h-3" />
                  <span>ویرایش متن</span>
                </button>
                <button
                  type="button"
                  onClick={onClearTranscript}
                  disabled={!isInteractive}
                  className="px-2.5 py-1 rounded-xl bg-rose-500/15 hover:bg-rose-500/25 active:bg-rose-500/30 text-rose-300 flex items-center gap-1 transition-all border border-rose-400/40 text-[11px] font-bold"
                  title="پاک کردن"
                >
                  <Trash2 className="w-3 h-3" />
                  <span>پاک کردن</span>
                </button>
              </div>
            </div>

            {/* Body Row: Plain English Transcribed Text (No quotation marks per user requirement) + Cyan Cursor */}
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

      {/* 2. Main Apple Liquid Glass Capsule Dock */}
      <div className="relative w-full">
        {/* Layer A: Dock Capsule Background Pill (Height: 52px) */}
        <div
          className={`w-full h-[52px] rounded-full backdrop-blur-[16px] transition-all duration-300 shadow-[0_8px_32px_rgba(0,0,0,0.5)] ${
            isRecording
              ? 'bg-gradient-to-r from-[#182038]/90 via-[#1e1b38]/85 to-[#162738]/90 border border-cyan-400/50 ring-1 ring-cyan-400/30 shadow-[0_0_24px_rgba(56,189,248,0.22)]'
              : 'bg-gradient-to-b from-[#1b1f33]/85 via-[#121526]/80 to-[#0e101c]/85 border border-white/20'
          }`}
        />

        {/* Layer B: Controls Row (Physical LTR: Left = Hint/Mic, Center = Mic/Input, Right = Send/Keyboard) */}
        <div className="absolute inset-0 w-full h-[52px] px-2.5 flex items-center justify-between">
          {/* 1. Physically LEFT */}
          {inputMode === 'voice' ? (
            /* Left: Hint Lamp Button */
            <div className="relative shrink-0">
              <motion.button
                type="button"
                whileTap={{ scale: 0.92 }}
                onClick={onRequestHint}
                disabled={!isInteractive || isLoadingHint}
                aria-label="دریافت راهنمایی"
                className="w-10 h-10 rounded-full bg-gradient-to-b from-amber-400/20 to-amber-500/10 hover:bg-amber-400/25 border border-amber-400/50 flex items-center justify-center text-amber-300 shadow-[0_0_10px_rgba(255,204,0,0.18)] transition-all"
                title="راهنمای جمله بعدی"
              >
                <Lightbulb className="w-4 h-4 text-amber-300" />
              </motion.button>

              {hintsUsedCount > 0 && (
                <span className="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-rose-500 text-white text-[9px] font-bold flex items-center justify-center border border-white/30">
                  {hintsUsedCount}
                </span>
              )}
            </div>
          ) : (
            /* Left: Mode toggle back to Voice */
            <motion.button
              type="button"
              whileTap={{ scale: 0.92 }}
              onClick={() => setInputMode('voice')}
              disabled={!isInteractive}
              aria-label="تغییر به مکالمه صوتی"
              className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/20 flex items-center justify-center text-[#38bdf8] transition-all shrink-0"
              title="مکالمه صوتی"
            >
              <Mic className="w-4 h-4" />
            </motion.button>
          )}

          {/* 2. Physically CENTER */}
          <div className="flex-1 min-w-0 h-full flex items-center justify-center px-2">
            {inputMode === 'voice' ? (
              /* Voice Mode Center Control with Concentric Expanding Rings Overflowing Dock */
              <div className="relative flex items-center justify-center">
                {isRecording ? (
                  isProcessingRecording ? (
                    /* Preparing / Loading state */
                    <div className="w-11 h-11 rounded-full bg-amber-400/15 border border-amber-400/50 flex items-center justify-center">
                      <span className="w-4 h-4 border-2 border-amber-400 border-t-transparent rounded-full animate-spin" />
                    </div>
                  ) : (
                    /* Active Recording: Overflowing Concentric Waves (diameter 96px) */
                    <div className="relative w-11 h-11 flex items-center justify-center">
                      <span className="absolute -inset-6 rounded-full border border-cyan-400/35 animate-ping pointer-events-none" />
                      <span className="absolute -inset-3.5 rounded-full border border-cyan-300/40 animate-pulse pointer-events-none" />

                      <motion.button
                        type="button"
                        whileTap={{ scale: 0.94 }}
                        onClick={onStopRecording}
                        className="relative z-10 w-11 h-11 rounded-full bg-gradient-to-tr from-[#0a84ff] to-[#0055b3] text-white flex items-center justify-center border-2 border-cyan-300 shadow-[0_0_18px_rgba(56,189,248,0.5)] cursor-pointer"
                        title="لمس برای توقف ضبط"
                      >
                        <Mic className="w-5 h-5 text-cyan-200 animate-pulse" />
                      </motion.button>
                    </div>
                  )
                ) : (
                  /* Idle State: Clean 44px Mic Button without under-text */
                  <motion.button
                    type="button"
                    whileTap={{ scale: 0.94 }}
                    onClick={onStartRecording}
                    disabled={!isInteractive}
                    className="w-11 h-11 rounded-full bg-gradient-to-tr from-[#0a84ff]/35 via-[#0052cc]/30 to-[#5856d6]/35 hover:brightness-110 active:brightness-95 text-white border border-cyan-400/45 shadow-[0_2px_14px_rgba(0,122,255,0.25)] flex items-center justify-center"
                    title="لمس برای صحبت"
                  >
                    <Mic className="w-5 h-5 text-cyan-300" />
                  </motion.button>
                )}
              </div>
            ) : (
              /* Text Mode Input Field */
              <form onSubmit={handleSendText} className="w-full h-full flex items-center" dir="ltr">
                <input
                  ref={inputRef}
                  type="text"
                  value={textInput}
                  onChange={(e) => setTextInput(e.target.value)}
                  placeholder="Type in English..."
                  disabled={!isInteractive}
                  className="w-full h-[38px] px-4 rounded-full bg-white/10 text-white placeholder-slate-400 text-[13.5px] border border-white/15 focus:outline-none focus:border-[#0a84ff]/70 focus:bg-white/15 transition-all text-left font-sans"
                />
              </form>
            )}
          </div>

          {/* 3. Physically RIGHT: Send Button (Always on Right per user requirement) OR Keyboard Toggle */}
          {inputMode === 'voice' ? (
            hasVoiceTranscript ? (
              /* Send Button when voice transcript is ready */
              <motion.button
                type="button"
                whileTap={{ scale: 0.92 }}
                onClick={handleSendTranscript}
                disabled={!isInteractive}
                aria-label="ارسال پیام"
                className="w-10 h-10 rounded-full bg-gradient-to-tr from-[#0a84ff] to-[#0066cc] hover:brightness-110 text-white border border-white/30 flex items-center justify-center shadow-[0_2px_10px_rgba(10,132,255,0.45)] transition-all shrink-0"
                title="ارسال"
              >
                <Send className="w-4 h-4 ml-0.5" />
              </motion.button>
            ) : (
              /* Keyboard Toggle when idle */
              <motion.button
                type="button"
                whileTap={{ scale: 0.92 }}
                onClick={() => {
                  setInputMode('text');
                  setTimeout(() => inputRef.current?.focus(), 120);
                }}
                disabled={!isInteractive}
                aria-label="تغییر به تایپ متنی"
                className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/20 flex items-center justify-center text-white transition-all shrink-0"
                title="تایپ با کیبورد"
              >
                <Keyboard className="w-4 h-4" />
              </motion.button>
            )
          ) : (
            /* Send Button in Text Mode */
            <motion.button
              type="button"
              whileTap={{ scale: 0.92 }}
              onClick={handleSendText}
              disabled={!isInteractive || !hasInputText}
              aria-label="ارسال پیام"
              className={`w-10 h-10 rounded-full bg-gradient-to-tr from-[#0a84ff] to-[#0066cc] hover:brightness-110 text-white border border-white/30 flex items-center justify-center shadow-[0_2px_10px_rgba(10,132,255,0.45)] transition-all shrink-0 ${
                !hasInputText ? 'opacity-40 pointer-events-none' : ''
              }`}
              title="ارسال"
            >
              <Send className="w-4 h-4 ml-0.5" />
            </motion.button>
          )}
        </div>

        {/* Layer C: Attached Live Badge "در حال شنیدن" (Floats below the center dock edge) */}
        {isRecording && !isProcessingRecording && (
          <div className="absolute -bottom-2.5 left-1/2 -translate-x-1/2 z-20">
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

        {isRecording && isProcessingRecording && (
          <div className="absolute -bottom-2.5 left-1/2 -translate-x-1/2 z-20">
            <button
              type="button"
              onClick={onStopRecording}
              className="px-2 py-0.5 rounded-full bg-[#1f1a0d]/95 border border-amber-400/70 flex items-center gap-1 text-[10px] font-bold text-amber-300 shadow-[0_0_8px_rgba(255,178,54,0.35)] cursor-pointer"
            >
              <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
              <span>در حال آماده‌سازی…</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
