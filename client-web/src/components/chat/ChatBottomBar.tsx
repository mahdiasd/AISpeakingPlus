import React, { useState, useRef, useEffect } from 'react';
import { Mic, MicOff, Send, Keyboard, Lightbulb, Sparkles, X } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

export interface ChatBottomBarProps {
  onSendMessage: (text: string) => void;
  onRequestHint: () => void;
  isModelSpeaking: boolean;
  isSending: boolean;
  isLoadingHint: boolean;
  isRecording: boolean;
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
    if (!clean || isSending || isModelSpeaking) return;
    onSendMessage(clean);
    setTextInput('');
  };

  return (
    <div className="fixed bottom-4 inset-x-3 max-w-lg mx-auto z-40">
      {/* Live Speech Recognition Transcript Floater */}
      <AnimatePresence>
        {isRecording && (
          <motion.div
            initial={{ opacity: 0, y: 12, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 8, scale: 0.95 }}
            transition={{ type: 'spring', stiffness: 450, damping: 28 }}
            className="mb-2 p-3.5 rounded-2xl apple-glass bg-[#1b1d28]/95 border border-apple-blue/40 shadow-apple-lg text-white space-y-2"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs font-bold text-apple-blue">
                <span className="w-2.5 h-2.5 rounded-full bg-apple-blue animate-ping" />
                <span>گوش دادن هوشمند (به انگلیسی صحبت کنید)...</span>
              </div>
              {onCancelRecording && (
                <button
                  type="button"
                  onClick={onCancelRecording}
                  className="p-1 rounded-full text-apple-gray-400 hover:text-white"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </div>

            <p className="text-sm font-mono text-slate-100 min-h-[22px] px-2 py-1 rounded-xl bg-white/5 border border-white/5 select-text" dir="ltr">
              {recordingTranscript || 'Listening to your English sentence...'}
            </p>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Recording Error Alert */}
      <AnimatePresence>
        {recordingError && (
          <motion.div
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 6 }}
            className="mb-2 px-3 py-2 rounded-2xl bg-apple-red/20 border border-apple-red/30 text-white text-xs flex items-center justify-between"
          >
            <span>{recordingError}</span>
            <button
              type="button"
              onClick={() => setInputMode('text')}
              className="text-apple-yellow underline text-[11px] font-bold mr-2"
            >
              استفاده از کیبورد
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Glass Floating Dock */}
      <div className="apple-glass rounded-3xl p-2 border border-white/15 bg-[#141620]/90 backdrop-blur-2xl shadow-apple-lg flex items-center gap-2">
        {/* 1. Hint Button */}
        <button
          type="button"
          onClick={onRequestHint}
          disabled={isLoadingHint || isSending || isModelSpeaking}
          className="p-3 rounded-2xl bg-white/10 hover:bg-white/15 text-apple-yellow active:scale-95 disabled:opacity-40 transition-all shrink-0 flex items-center justify-center relative"
          title="دریافت راهنمایی هوش مصنوعی"
        >
          {isLoadingHint ? (
            <Sparkles className="w-5 h-5 animate-spin" />
          ) : (
            <Lightbulb className="w-5 h-5 fill-apple-yellow/20" />
          )}
        </button>

        {/* 2. Mode Input Center Container */}
        <div className="flex-1 min-w-0">
          {inputMode === 'voice' ? (
            /* Voice Recording Big Pill */
            <div className="flex items-center justify-center">
              <button
                type="button"
                onClick={isRecording ? onStopRecording : onStartRecording}
                disabled={isSending || isModelSpeaking}
                className={`relative flex items-center justify-center gap-2.5 w-full py-3 px-4 rounded-2xl font-bold text-sm transition-all shadow-apple-md ${
                  isRecording
                    ? 'bg-gradient-to-r from-apple-red to-apple-orange text-white ring-4 ring-apple-red/30 animate-pulse'
                    : 'bg-gradient-to-r from-apple-blue to-[#5856D6] hover:brightness-110 text-white active:scale-[0.98]'
                } disabled:opacity-50`}
              >
                {isRecording ? (
                  <>
                    <MicOff className="w-5 h-5 animate-bounce" />
                    <span>توقف و ارسال پیام</span>
                  </>
                ) : (
                  <>
                    <Mic className="w-5 h-5" />
                    <span>{isModelSpeaking ? 'لطفاً صبر کنید...' : 'لمس کنید و انگلیسی بگویید'}</span>
                  </>
                )}
              </button>
            </div>
          ) : (
            /* Keyboard Text Input Capsule */
            <form onSubmit={handleSendText} className="flex items-center gap-2 w-full">
              <input
                ref={inputRef}
                type="text"
                value={textInput}
                onChange={(e) => setTextInput(e.target.value)}
                placeholder="Type in English..."
                dir="ltr"
                disabled={isSending || isModelSpeaking}
                className="w-full bg-white/10 border border-white/15 focus:border-apple-blue rounded-2xl px-3.5 py-2.5 text-sm text-white placeholder-apple-gray-400 focus:outline-none focus:ring-1 focus:ring-apple-blue transition-all"
              />
              <button
                type="submit"
                disabled={!textInput.trim() || isSending || isModelSpeaking}
                className="p-2.5 rounded-2xl bg-apple-blue hover:bg-apple-blue/90 text-white disabled:opacity-30 active:scale-95 transition-all shrink-0"
              >
                <Send className="w-4 h-4 rotate-180" />
              </button>
            </form>
          )}
        </div>

        {/* 3. Input Mode Switcher (Voice vs Keyboard) */}
        <button
          type="button"
          onClick={() => {
            if (isRecording) {
              onStopRecording();
            }
            setInputMode((prev) => (prev === 'voice' ? 'text' : 'voice'));
          }}
          className="p-3 rounded-2xl bg-white/10 hover:bg-white/15 text-slate-200 active:scale-95 transition-all shrink-0"
          title={inputMode === 'voice' ? 'تغییر به کیبورد متنی' : 'تغییر به ورودی صوتی'}
        >
          {inputMode === 'voice' ? (
            <Keyboard className="w-5 h-5 text-apple-gray-300" />
          ) : (
            <Mic className="w-5 h-5 text-apple-blue" />
          )}
        </button>
      </div>
    </div>
  );
};
