import React, { useState, useRef, useEffect } from 'react';
import { Mic, Square, Send, Keyboard, Lightbulb, Sparkles, MicOff } from 'lucide-react';
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
  onStartRecording: () => void;
  onStopRecording: () => void;
  onCancelRecording?: () => void;
  recordingError?: string | null;
}

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
  onStartRecording,
  onStopRecording,
  onCancelRecording,
  recordingError,
}) => {
  const [inputMode, setInputMode] = useState<'voice' | 'text'>('voice');
  const [textInput, setTextInput] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (inputMode === 'text') {
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [inputMode]);

  const handleSendText = (e?: React.FormEvent) => {
    e?.preventDefault();
    const clean = textInput.trim();
    if (!clean || isSending || isModelSpeaking || isProcessingRecording) return;
    onSendMessage(clean);
    setTextInput('');
  };

  const isBusy = isSending || isModelSpeaking || isProcessingRecording;

  return (
    <div className="w-full select-none">
      {/* 1. Live Siri Waveform / Processing Card Floating Above Dock */}
      <AnimatePresence>
        {(isRecording || isProcessingRecording) && (
          <motion.div
            initial={{ opacity: 0, y: 14, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 10, scale: 0.95 }}
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

      {/* 2. Recording Error Alert Toast */}
      <AnimatePresence>
        {recordingError && !isRecording && (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 8 }}
            className="mb-2.5 px-4 py-2.5 rounded-2xl apple-glass bg-apple-red/20 border border-apple-red/30 text-white text-xs flex items-center justify-between shadow-apple-sm"
          >
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-apple-red shrink-0" />
              <span>{recordingError}</span>
            </div>
            <button
              type="button"
              onClick={() => setInputMode('text')}
              className="text-apple-yellow underline text-[11px] font-bold mr-2 shrink-0 active:opacity-70"
            >
              کیبورد
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 3. Main Apple Liquid Glass Floating Dock */}
      <div className="relative rounded-[28px] p-2 apple-glass-card border border-white/25 shadow-apple-lg flex items-center gap-2">
        {/* Specular Top Edge Light Bar */}
        <div
          aria-hidden="true"
          className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none rounded-t-[28px]"
        />

        {/* Hint Trigger Button (Amber Warm Apple Style) */}
        <motion.button
          type="button"
          whileTap={{ scale: 0.92 }}
          transition={{ type: 'spring', stiffness: 450, damping: 25 }}
          onClick={onRequestHint}
          disabled={isLoadingHint || isBusy}
          className="w-12 h-12 rounded-2xl apple-glass-gold text-apple-yellow active:opacity-80 disabled:opacity-35 transition-all shrink-0 flex items-center justify-center relative border border-apple-yellow/30 shadow-apple-sm"
          title="دریافت راهنمایی هوش مصنوعی"
        >
          {isLoadingHint ? (
            <Sparkles className="w-5 h-5 animate-spin text-apple-yellow" />
          ) : (
            <Lightbulb className="w-5 h-5 fill-apple-yellow/30 text-apple-yellow" />
          )}
        </motion.button>

        {/* Mode Input Center Pill Container */}
        <div className="flex-1 min-w-0">
          {inputMode === 'voice' ? (
            /* Voice Recording Big Pill */
            <div className="flex items-center justify-center">
              <motion.button
                type="button"
                whileTap={{ scale: isBusy ? 1 : 0.97 }}
                transition={{ type: 'spring', stiffness: 450, damping: 25 }}
                onClick={isRecording ? onStopRecording : onStartRecording}
                disabled={isBusy}
                className={`relative flex items-center justify-center gap-2.5 w-full h-12 px-4 rounded-2xl font-bold text-sm transition-all overflow-hidden border ${
                  isRecording
                    ? 'bg-gradient-to-r from-apple-red via-rose-600 to-apple-orange text-white border-apple-red/50 shadow-[0_0_20px_rgba(255,59,48,0.5)] ring-2 ring-apple-red/40'
                    : 'bg-gradient-to-r from-apple-blue to-[#5856D6] hover:brightness-105 text-white border-white/25 shadow-apple-md'
                } disabled:opacity-40 disabled:cursor-not-allowed`}
              >
                {/* Specular Edge Highlight */}
                <div
                  aria-hidden="true"
                  className="absolute inset-x-0 top-0 h-[1.5px] bg-gradient-to-r from-transparent via-white/40 to-transparent pointer-events-none"
                />

                {isRecording ? (
                  <>
                    <Square className="w-4 h-4 fill-white animate-pulse" />
                    <span>توقف و ارسال پیام</span>
                  </>
                ) : (
                  <>
                    <Mic className="w-4 h-4" />
                    <span className="truncate">
                      {isModelSpeaking ? 'لطفاً به صحبت‌ها گوش دهید...' : 'لمس کنید و انگلیسی بگویید'}
                    </span>
                  </>
                )}
              </motion.button>
            </div>
          ) : (
            /* Text Input Form */
            <form onSubmit={handleSendText} className="flex items-center gap-1.5 w-full">
              <input
                ref={inputRef}
                type="text"
                dir="ltr"
                value={textInput}
                onChange={(e) => setTextInput(e.target.value)}
                placeholder="Type your response in English..."
                disabled={isBusy}
                className="w-full h-12 bg-white/10 border border-white/15 focus:border-apple-blue focus:bg-white/15 rounded-2xl px-4 text-sm text-white placeholder-apple-gray-400 focus:outline-none transition-all"
              />
              <motion.button
                type="submit"
                whileTap={{ scale: 0.92 }}
                disabled={!textInput.trim() || isBusy}
                className="w-12 h-12 rounded-2xl bg-apple-blue hover:bg-blue-600 text-white flex items-center justify-center shrink-0 disabled:opacity-40 transition-all border border-apple-blue/50 shadow-apple-sm"
              >
                {isSending ? (
                  <Sparkles className="w-4 h-4 animate-spin" />
                ) : (
                  <Send className="w-4 h-4" />
                )}
              </motion.button>
            </form>
          )}
        </div>

        {/* Input Mode Toggle (Voice vs Keyboard) */}
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
          className="w-12 h-12 rounded-2xl apple-glass-pill bg-white/10 hover:bg-white/15 text-apple-gray-300 hover:text-white active:bg-white/20 disabled:opacity-35 transition-all shrink-0 flex items-center justify-center border border-white/15"
          title={inputMode === 'voice' ? 'استفاده از کیبورد متنی' : 'استفاده از ورودی صوتی'}
        >
          {inputMode === 'voice' ? (
            <Keyboard className="w-5 h-5" />
          ) : (
            <MicOff className="w-5 h-5 text-apple-blue" />
          )}
        </motion.button>
      </div>
    </div>
  );
};
