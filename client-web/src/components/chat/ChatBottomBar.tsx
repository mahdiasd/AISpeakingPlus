import React, { useState, useRef, useEffect } from 'react';
import {
  Mic,
  Square,
  Send,
  Keyboard,
  Lightbulb,
  Edit2,
  Trash2,
  RefreshCw,
  Sparkles,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { SiriVoiceVisualizer } from './SiriVoiceVisualizer';

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
 * 100% replicates Compose Multiplatform `ChatBottomBar.kt` in layout, animation and aesthetics.
 *
 * Structure in RTL:
 * - Floating Transcript Capsule (above dock when voice recognition has captured text)
 * - Siri Waveform / Processing Card (above dock while listening or processing)
 * - Main visionOS Capsule Dock (.apple-glass-dock, rounded-[30px]):
 *   - Start / Right: Toggle Mode button (switch between Keyboard and Mic)
 *   - Center / Flexible:
 *     - Voice Mode: Big rounded tactile pill button with ambient breathing glow ("برای صحبت لمس کنید"),
 *       switching to pulsing live wave indicator ("در حال شنیدن… لمس برای توقف") while recording.
 *     - Text Mode: Soft input pill with LTR English text, placeholder "Type in English...", and circular Azure Send button.
 *   - End / Left: Circular Champagne Gold Hint button with lamp icon and usage badge indicator.
 */
export const ChatBottomBar: React.FC<ChatBottomBarProps> = ({
  onSendMessage,
  onRequestHint,
  isModelSpeaking,
  isSending,
  isLoadingHint,
  isRecording,
  isProcessingRecording = false,
  recordingAudioLevel = 0,
  recordingDuration = 0,
  recordingTranscript = '',
  hintsUsedCount = 0,
  onStartRecording,
  onStopRecording,
  onCancelRecording,
  onClearTranscript,
  recordingError,
}) => {
  const [inputMode, setInputMode] = useState<'voice' | 'text'>('voice');
  const [textInput, setTextInput] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (inputMode === 'text') {
      const t = setTimeout(() => inputRef.current?.focus(), 150);
      return () => clearTimeout(t);
    }
  }, [inputMode]);

  const handleSendText = (e?: React.FormEvent) => {
    e?.preventDefault();
    const clean = textInput.trim();
    if (!clean || isSending || isModelSpeaking || isProcessingRecording) return;
    onSendMessage(clean);
    setTextInput('');
  };

  const handleSendTranscript = () => {
    const clean = recordingTranscript.trim();
    if (!clean || isSending || isModelSpeaking || isProcessingRecording) return;
    onSendMessage(clean);
    onClearTranscript?.();
  };

  const handleEditTranscript = () => {
    setTextInput(recordingTranscript);
    setInputMode('text');
  };

  const isBusy = isSending || isModelSpeaking || isProcessingRecording;
  const isInteractive = !isBusy && !isModelSpeaking;
  const hasTranscript = Boolean(recordingTranscript && recordingTranscript.trim().length > 0);

  return (
    <div className="w-full select-none" dir="rtl">
      {/* 1. Live Siri Waveform / Processing Card Floating Above Dock */}
      <AnimatePresence>
        {(isRecording || isProcessingRecording) && (
          <motion.div
            initial={{ opacity: 0, y: 14, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 10, scale: 0.96 }}
            transition={{ type: 'spring', stiffness: 450, damping: 28 }}
            className="mb-2.5"
          >
            <SiriVoiceVisualizer
              isRecording={isRecording}
              isProcessing={isProcessingRecording}
              audioLevel={recordingAudioLevel}
              recordingDuration={recordingDuration}
              transcript={recordingTranscript}
              onCancel={onCancelRecording}
            />
          </motion.div>
        )}
      </AnimatePresence>

      {/* 2. Floating Transcript Preview Capsule (shown when speech recognition has recognized text & idle) */}
      <AnimatePresence>
        {hasTranscript && inputMode === 'voice' && !isRecording && !isProcessingRecording && !isModelSpeaking && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 8, scale: 0.97 }}
            transition={{ type: 'spring', stiffness: 450, damping: 28 }}
            className="mb-2.5 apple-glass-card rounded-[20px] p-3 border border-white/25 shadow-apple-md backdrop-blur-[16px]"
          >
            <div className="flex items-center justify-between gap-2.5">
              {/* English Transcribed Text */}
              <div
                dir="ltr"
                className="flex-1 min-w-0 text-sm font-medium text-white truncate px-1 font-sans"
                title={recordingTranscript}
              >
                "{recordingTranscript}"
              </div>

              {/* Action Buttons: Instant Send, Edit, and Clear */}
              <div className="flex items-center gap-1.5 shrink-0">
                {/* Instant Send */}
                <motion.button
                  type="button"
                  whileTap={{ scale: 0.92 }}
                  onClick={handleSendTranscript}
                  disabled={isBusy}
                  className="h-8 px-3 rounded-full bg-gradient-to-r from-[#007AFF] to-[#5856D6] hover:brightness-110 text-white text-xs font-bold flex items-center gap-1.5 shadow-[0_2px_10px_rgba(0,122,255,0.4)] border border-white/20 transition-all"
                  title="ارسال این متن"
                >
                  <Send className="w-3.5 h-3.5 ml-0.5" />
                  <span>ارسال</span>
                </motion.button>

                {/* Edit in text mode */}
                <motion.button
                  type="button"
                  whileTap={{ scale: 0.92 }}
                  onClick={handleEditTranscript}
                  className="w-8 h-8 rounded-full apple-glass bg-white/10 hover:bg-white/15 text-[#38BDF8] border border-white/20 flex items-center justify-center transition-all"
                  title="ویرایش متنی"
                >
                  <Edit2 className="w-3.5 h-3.5" />
                </motion.button>

                {/* Clear transcript */}
                <motion.button
                  type="button"
                  whileTap={{ scale: 0.92 }}
                  onClick={onClearTranscript}
                  className="w-8 h-8 rounded-full apple-glass bg-white/10 hover:bg-white/15 text-rose-400 border border-white/20 flex items-center justify-center transition-all"
                  title="پاک کردن"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </motion.button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 3. Recording Error Alert Toast */}
      <AnimatePresence>
        {recordingError && !isRecording && (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 8 }}
            className="mb-2.5 px-4 py-2.5 rounded-2xl apple-glass bg-rose-500/20 border border-rose-500/30 text-white text-xs flex items-center justify-between shadow-apple-sm"
          >
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-rose-500 shrink-0" />
              <span>{recordingError}</span>
            </div>
            <button
              type="button"
              onClick={() => setInputMode('text')}
              className="text-amber-300 underline text-[11px] font-bold mr-2 shrink-0 active:opacity-70"
            >
              استفاده از کیبورد
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 4. Main Apple visionOS Floating Capsule Dock */}
      <div className="relative rounded-[30px] p-2 apple-glass-dock border border-white/25 shadow-apple-lg flex items-center gap-2">
        {/* Specular Top Edge Light Bar */}
        <div
          aria-hidden="true"
          className="absolute inset-x-0 top-0 h-[1.5px] bg-gradient-to-r from-transparent via-white/50 to-transparent pointer-events-none rounded-t-[30px]"
        />

        {/* Start / Right (RTL): Mode Toggle Button (Keyboard / Mic) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.92 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={() => {
            if (isRecording) {
              onCancelRecording?.();
            }
            setInputMode((prev) => (prev === 'voice' ? 'text' : 'voice'));
          }}
          disabled={isBusy}
          aria-label={inputMode === 'voice' ? 'تایپ متنی' : 'مکالمه صوتی'}
          className="w-[42px] h-[42px] rounded-full apple-glass bg-white/10 hover:bg-white/15 text-apple-gray-200 hover:text-white active:bg-white/20 disabled:opacity-35 transition-all shrink-0 flex items-center justify-center border border-white/20 shadow-apple-sm"
          title={inputMode === 'voice' ? 'تغییر به تایپ متنی' : 'تغییر به مکالمه صوتی'}
        >
          {inputMode === 'voice' ? (
            <Keyboard className="w-5 h-5 text-slate-200" />
          ) : (
            <Mic className="w-5 h-5 text-[#38BDF8]" />
          )}
        </motion.button>

        {/* Center / Flexible: Voice Pill or Text Input Form */}
        <div className="flex-1 min-w-0">
          {inputMode === 'voice' ? (
            <div className="flex items-center justify-center w-full">
              {isRecording ? (
                /* Active Recording Pill (Apple Voice Memos style with live pulse) */
                <motion.button
                  type="button"
                  whileTap={{ scale: 0.97 }}
                  transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                  onClick={onStopRecording}
                  className="relative flex items-center justify-center gap-2.5 w-full h-[44px] px-4 rounded-full font-bold text-xs sm:text-sm transition-all overflow-hidden bg-rose-500/20 border border-rose-500/60 shadow-[0_0_18px_rgba(244,63,94,0.4)] text-rose-300 ring-1 ring-rose-500/30"
                  title="توقف ضبط و ارسال"
                >
                  {/* Pulsing red dot */}
                  <span className="relative flex h-2.5 w-2.5 shrink-0">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-500 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-rose-500 shadow-[0_0_8px_#f43f5e]" />
                  </span>

                  {/* Mini animated wave bars */}
                  <div className="flex items-center gap-0.5 h-3.5">
                    <span className="w-0.5 h-3 bg-rose-400 rounded-full animate-pulse" />
                    <span className="w-0.5 h-2 bg-rose-400 rounded-full animate-pulse delay-75" />
                    <span className="w-0.5 h-3.5 bg-rose-400 rounded-full animate-pulse delay-150" />
                  </div>

                  <span className="truncate">در حال شنیدن… لمس برای توقف</span>

                  {/* Stop square icon */}
                  <Square className="w-3.5 h-3.5 fill-rose-400 text-rose-400 shrink-0 mr-1" />
                </motion.button>
              ) : isProcessingRecording ? (
                /* Preparing / Processing Voice Pill */
                <div className="relative flex items-center justify-center gap-2.5 w-full h-[44px] px-4 rounded-full font-bold text-xs sm:text-sm bg-amber-500/20 border border-amber-500/50 text-amber-300 shadow-[0_0_14px_rgba(245,158,11,0.25)]">
                  <RefreshCw className="w-4 h-4 animate-spin text-amber-300 shrink-0" />
                  <span className="truncate">در حال پردازش گفتار انگلیسی...</span>
                </div>
              ) : (
                /* Idle Tactile Apple Record Pill */
                <motion.button
                  type="button"
                  whileTap={{ scale: isInteractive ? 0.96 : 1 }}
                  transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                  onClick={isInteractive ? onStartRecording : undefined}
                  disabled={!isInteractive}
                  className={`relative flex items-center justify-center gap-2 w-full h-[44px] px-4 rounded-full font-bold text-xs sm:text-sm transition-all overflow-hidden border ${
                    !isInteractive
                      ? 'bg-white/10 text-slate-400 border-white/15 cursor-not-allowed opacity-60'
                      : hasTranscript
                      ? 'bg-white/15 hover:bg-white/20 text-white border-white/30 shadow-apple-sm'
                      : 'bg-gradient-to-r from-[#007AFF]/35 via-[#0A84FF]/25 to-[#5856D6]/35 hover:brightness-110 text-cyan-200 border-[#007AFF]/50 shadow-[0_0_18px_rgba(0,122,255,0.3)]'
                  }`}
                  title={isModelSpeaking ? 'هوش مصنوعی در حال پاسخ' : 'لمس کنید و صحبت کنید'}
                >
                  {/* Subtle top edge highlight */}
                  <div
                    aria-hidden="true"
                    className="absolute inset-x-0 top-0 h-[1px] bg-gradient-to-r from-transparent via-white/40 to-transparent pointer-events-none"
                  />

                  {isModelSpeaking ? (
                    <>
                      <RefreshCw className="w-4 h-4 animate-spin text-slate-400" />
                      <span className="truncate">هوش مصنوعی در حال پاسخ…</span>
                    </>
                  ) : hasTranscript ? (
                    <>
                      <Mic className="w-4 h-4 text-white" />
                      <span className="truncate">ضبط مجدد صدا</span>
                    </>
                  ) : (
                    <>
                      <Mic className="w-4 h-4 text-[#38BDF8]" />
                      <span className="truncate">برای صحبت لمس کنید</span>
                    </>
                  )}
                </motion.button>
              )}
            </div>
          ) : (
            /* Text Input Form */
            <form onSubmit={handleSendText} className="flex items-center gap-2 w-full">
              <input
                ref={inputRef}
                type="text"
                dir="ltr"
                value={textInput}
                onChange={(e) => setTextInput(e.target.value)}
                placeholder="Type in English..."
                disabled={isBusy}
                className="flex-1 min-w-0 h-[44px] bg-white/10 border border-white/20 focus:border-[#007AFF] focus:bg-white/15 rounded-full px-4 text-sm text-white placeholder-apple-gray-400 focus:outline-none transition-all"
              />

              {/* Circular Send Button (shown in text mode) */}
              <motion.button
                type="submit"
                whileTap={{ scale: 0.92 }}
                disabled={!textInput.trim() || isBusy}
                aria-label="ارسال پیام"
                className="w-[42px] h-[42px] rounded-full bg-gradient-to-tr from-[#007AFF] to-[#5856D6] hover:brightness-110 text-white flex items-center justify-center shrink-0 disabled:opacity-40 transition-all border border-white/30 shadow-[0_2px_12px_rgba(0,122,255,0.45)]"
                title="ارسال پیام"
              >
                {isSending ? (
                  <Sparkles className="w-4 h-4 animate-spin" />
                ) : (
                  <Send className="w-4 h-4 ml-0.5" />
                )}
              </motion.button>
            </form>
          )}
        </div>

        {/* End / Left (RTL): Circular Champagne Gold Hint Button */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.92 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onRequestHint}
          disabled={isLoadingHint || isBusy}
          aria-label="دریافت راهنمای گفتگو"
          className={`w-[42px] h-[42px] rounded-full flex items-center justify-center shrink-0 transition-all relative border shadow-apple-sm ${
            hintsUsedCount > 0
              ? 'bg-amber-500/25 border-amber-400/60 text-amber-300 shadow-[0_0_12px_rgba(245,158,11,0.3)]'
              : 'bg-white/10 hover:bg-white/15 border-white/20 text-amber-300'
          } active:scale-95 disabled:opacity-35`}
          title="دریافت راهنمای هوش مصنوعی"
        >
          {isLoadingHint ? (
            <RefreshCw className="w-4 h-4 animate-spin text-amber-300" />
          ) : (
            <Lightbulb className="w-4 h-4 fill-amber-400/30 text-amber-300" />
          )}

          {/* Usage badge dot / count */}
          {hintsUsedCount > 0 && (
            <span className="absolute -top-0.5 -right-0.5 w-4 h-4 rounded-full bg-amber-400 text-slate-950 font-bold text-[9px] flex items-center justify-center shadow-[0_0_6px_#f59e0b]">
              {hintsUsedCount}
            </span>
          )}
        </motion.button>
      </div>
    </div>
  );
};
