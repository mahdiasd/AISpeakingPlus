import React, { useState, useRef, useEffect } from 'react';
import {
  Mic,
  Send,
  Keyboard,
  Lightbulb,
  Edit2,
  Trash2,
  Square,
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
 * - Compact height: visionOS rounded capsule dock (~50px).
 * - Real-time spoken text preview capsule directly above the dock (both while recording and idle).
 * - Voice mode: [ Keyboard Toggle ] --- [ Record Pill / Active Voice Wave ] --- [ Send OR Hint ]
 * - Text mode: [ Mic Toggle ] --- [ "Type in English..." ] --- [ Send OR Hint ]
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
  };

  const isBusy = isSending || isModelSpeaking || isProcessingRecording;
  const isInteractive = !isBusy && !isModelSpeaking;
  const hasVoiceTranscript = Boolean(recordingTranscript && recordingTranscript.trim().length > 0);
  const hasInputText = Boolean(textInput && textInput.trim().length > 0);

  return (
    <div className="w-full select-none" dir="rtl">
      {/* 1. Floating Transcript Preview Capsule above Dock (Matching Compose CompactTranscriptCapsule) */}
      <AnimatePresence>
        {hasVoiceTranscript && inputMode === 'voice' && !isModelSpeaking && (
          <motion.div
            initial={{ opacity: 0, y: 8, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 6, scale: 0.97 }}
            transition={{ type: 'spring', stiffness: 450, damping: 28 }}
            className="mb-1.5 px-3 py-1.5 rounded-2xl bg-[#141726]/80 backdrop-blur-md border border-white/20 shadow-apple-sm flex items-center justify-between gap-2"
          >
            {/* English Spoken / Transcribed Text */}
            <div
              dir="ltr"
              className="flex-1 min-w-0 text-[13px] font-medium text-white truncate px-1 font-sans"
              title={recordingTranscript}
            >
              "{recordingTranscript}"
            </div>

            {/* Actions: Edit in text mode & Clear */}
            <div className="flex items-center gap-1 shrink-0">
              <button
                type="button"
                onClick={handleEditTranscript}
                className="w-7 h-7 rounded-full bg-white/10 hover:bg-white/15 text-[#38bdf8] flex items-center justify-center transition-all border border-white/15"
                title="ویرایش متنی"
              >
                <Edit2 className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={onClearTranscript}
                className="w-7 h-7 rounded-full bg-white/10 hover:bg-white/15 text-rose-400 flex items-center justify-center transition-all border border-white/15"
                title="پاک کردن"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 2. Main Apple Liquid Glass Capsule Dock */}
      <div className="w-full h-[52px] px-2 rounded-full bg-gradient-to-b from-[#1b1f33]/85 via-[#121526]/80 to-[#0e101c]/85 backdrop-blur-[14px] border border-white/20 shadow-[0_8px_32px_rgba(0,0,0,0.45)] flex items-center gap-2">
        {/* RTL Start / Right Button: Toggle Mode (Mic <-> Keyboard) */}
        {inputMode === 'voice' ? (
          <motion.button
            type="button"
            whileTap={{ scale: 0.92 }}
            onClick={() => setInputMode('text')}
            disabled={!isInteractive}
            aria-label="تغییر به تایپ متنی"
            className="w-9 h-9 rounded-full bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/20 flex items-center justify-center text-white transition-all shrink-0"
            title="تایپ با کیبورد"
          >
            <Keyboard className="w-4 h-4" />
          </motion.button>
        ) : (
          <motion.button
            type="button"
            whileTap={{ scale: 0.92 }}
            onClick={() => setInputMode('voice')}
            disabled={!isInteractive}
            aria-label="تغییر به مکالمه صوتی"
            className="w-9 h-9 rounded-full bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/20 flex items-center justify-center text-[#38bdf8] transition-all shrink-0"
            title="مکالمه صوتی"
          >
            <Mic className="w-4 h-4" />
          </motion.button>
        )}

        {/* Center: Interactive Input or Recording Pill */}
        <div className="flex-1 min-w-0 h-full flex items-center justify-center">
          {inputMode === 'voice' ? (
            /* Voice Mode Center Pill */
            isRecording ? (
              /* Apple Voice Memos style recording pill with pulsing wave */
              <motion.button
                type="button"
                whileTap={{ scale: 0.96 }}
                onClick={onStopRecording}
                className="w-full h-[38px] px-3 rounded-full bg-rose-500/20 border border-rose-400/50 flex items-center justify-between text-rose-300 font-bold text-xs shadow-[0_0_12px_rgba(244,63,94,0.25)] cursor-pointer"
              >
                <div className="flex items-center gap-2 min-w-0">
                  <span className="w-2.5 h-2.5 rounded-full bg-rose-500 animate-ping shrink-0" />
                  <span className="truncate">
                    {recordingTranscript ? `"${recordingTranscript}"` : 'در حال شنیدن… لمس برای توقف'}
                  </span>
                </div>
                <div className="flex items-center gap-1.5 shrink-0 mr-1">
                  <span className="w-1 h-3 bg-rose-400 rounded-full animate-pulse" />
                  <span className="w-1 h-4.5 bg-rose-300 rounded-full animate-pulse [animation-delay:150ms]" />
                  <span className="w-1 h-2.5 bg-rose-400 rounded-full animate-pulse [animation-delay:300ms]" />
                  <Square className="w-3 h-3 fill-rose-400 text-rose-400 mr-1" />
                </div>
              </motion.button>
            ) : isProcessingRecording ? (
              <div className="w-full h-[38px] px-3 rounded-full bg-amber-400/15 border border-amber-400/40 flex items-center justify-center gap-2 text-amber-300 text-xs font-bold">
                <span className="w-3 h-3 border-2 border-amber-400 border-t-transparent rounded-full animate-spin" />
                <span>در حال تبدیل صوت...</span>
              </div>
            ) : (
              /* Idle Touch-to-speak button with gentle breathing gradient */
              <motion.button
                type="button"
                whileTap={{ scale: 0.96 }}
                onClick={onStartRecording}
                disabled={!isInteractive}
                className={`w-full h-[38px] px-4 rounded-full flex items-center justify-center gap-2 text-xs sm:text-sm font-extrabold transition-all border ${
                  hasVoiceTranscript
                    ? 'bg-white/10 hover:bg-white/15 text-slate-200 border-white/20'
                    : 'bg-gradient-to-r from-[#007aff]/35 via-[#0052cc]/30 to-[#5856d6]/35 hover:brightness-110 text-white border-cyan-400/35 shadow-[0_2px_14px_rgba(0,122,255,0.25)]'
                }`}
              >
                <Mic className="w-4 h-4 text-cyan-300" />
                <span>{hasVoiceTranscript ? 'برای ضبط مجدد لمس کنید' : 'برای صحبت لمس کنید'}</span>
              </motion.button>
            )
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
                className="w-full h-[38px] px-3.5 rounded-full bg-white/10 text-white placeholder-slate-400 text-[13.5px] border border-white/15 focus:outline-none focus:border-[#0a84ff]/70 focus:bg-white/15 transition-all text-left font-sans"
              />
            </form>
          )}
        </div>

        {/* RTL End / Left Button: Send when text is ready, otherwise Hint Lamp */}
        {(inputMode === 'voice' && hasVoiceTranscript) || (inputMode === 'text' && hasInputText) ? (
          /* Circular Azure Send Button (matching Compose AppleSendButton) */
          <motion.button
            type="button"
            whileTap={{ scale: 0.92 }}
            onClick={inputMode === 'voice' ? handleSendTranscript : handleSendText}
            disabled={!isInteractive}
            aria-label="ارسال پیام"
            className="w-9 h-9 rounded-full bg-gradient-to-tr from-[#0a84ff] to-[#0066cc] hover:brightness-110 text-white border border-white/30 flex items-center justify-center shadow-[0_2px_10px_rgba(10,132,255,0.45)] transition-all shrink-0"
            title="ارسال"
          >
            <Send className="w-4 h-4 ml-0.5" />
          </motion.button>
        ) : (
          /* Circular Champagne Gold Hint Button (matching Compose AppleHintButton) */
          <div className="relative shrink-0">
            <motion.button
              type="button"
              whileTap={{ scale: 0.92 }}
              onClick={onRequestHint}
              disabled={!isInteractive || isLoadingHint}
              aria-label="دریافت راهنمایی"
              className="w-9 h-9 rounded-full bg-gradient-to-b from-amber-400/20 to-amber-500/10 hover:bg-amber-400/25 border border-amber-400/50 flex items-center justify-center text-amber-300 shadow-[0_0_10px_rgba(255,204,0,0.18)] transition-all"
              title="راهنمای جمله بعدی"
            >
              <Lightbulb className="w-4 h-4 text-amber-300" />
            </motion.button>

            {/* Hints Used Counter Badge */}
            {hintsUsedCount > 0 && (
              <span className="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-rose-500 text-white text-[9px] font-bold flex items-center justify-center border border-white/30">
                {hintsUsedCount}
              </span>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
