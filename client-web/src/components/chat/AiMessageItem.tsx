import React, { useState } from 'react';
import { Play, Square, Languages, Copy, Check, Volume2 } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

export interface AiMessageItemProps {
  id: string;
  message: string;
  translatedMessage?: string | null;
  audioUrl?: string | null;
  characterName: string;
  characterAvatarUrl?: string | null;
  isStreaming?: boolean;
  isPlaying?: boolean;
  onPlayAudio?: () => void;
  onStopAudio?: () => void;
}

/**
 * 2026 Apple Liquid Glass AI Message Item.
 *
 * Characteristics:
 * - Left-aligned in RTL layout (chatted by AI Host, placed on the left with start-anchor avatar).
 * - 3D Character Avatar (36px squircle) with initial letter badge ("E" for Emily).
 * - Liquid Glass bubble with specular light rim and subtle translucent dark slate background (blur 14px).
 * - Hero English copy with Apple typography and sound wave animation when TTS is playing.
 * - Siri iridescent glowing typing dots when generating.
 * - Action chips beneath bubble: Voice playback ("پخش صدا" with equalizer), Translation toggle ("ترجمه"), and Copy ("کپی").
 */
export const AiMessageItem: React.FC<AiMessageItemProps> = ({
  message,
  translatedMessage,
  characterName,
  characterAvatarUrl,
  isStreaming = false,
  isPlaying = false,
  onPlayAudio,
  onStopAudio,
}) => {
  const [showTranslation, setShowTranslation] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    if (!message) return;
    navigator.clipboard?.writeText(message);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const hasTranslation = Boolean(translatedMessage && translatedMessage.trim().length > 0);
  const initialChar = (characterName ? characterName.trim().charAt(0) : 'E').toUpperCase();

  return (
    <div className="flex items-start gap-2.5 justify-start max-w-[92%] sm:max-w-[82%] my-3 mr-auto self-start" dir="ltr">
      {/* 1. Character Avatar (36px Squircle with Specular Ring & Initial Badge) */}
      <div className="relative shrink-0 mt-0.5 select-none">
        <div className="w-9 h-9 rounded-[12px] overflow-hidden apple-glass border border-white/25 shadow-apple-sm flex items-center justify-center bg-gradient-to-br from-[#252836] to-[#12131c]">
          {characterAvatarUrl ? (
            <img
              src={characterAvatarUrl}
              alt={characterName}
              className="w-full h-full object-cover"
              onError={(e) => {
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          ) : (
            <span className="text-sm font-bold text-cyan-300 drop-shadow-[0_1px_4px_rgba(6,182,212,0.4)]">
              {initialChar}
            </span>
          )}
        </div>

        {/* Small 3D initial badge in bottom-right of avatar if image is present */}
        {characterAvatarUrl && (
          <div className="absolute -bottom-1 -right-1 w-4 h-4 rounded-full bg-[#0f172a] border border-cyan-400/50 flex items-center justify-center shadow-sm">
            <span className="text-[9px] font-bold text-cyan-300 leading-none">
              {initialChar}
            </span>
          </div>
        )}
      </div>

      {/* 2. Message Content Column */}
      <div className="space-y-1.5 flex-1 min-w-0">
        {/* Character Name & AI Badge Row */}
        <div className="flex items-center gap-1.5 px-1 select-none" dir="ltr">
          <span className="text-[12px] font-semibold text-slate-200">{characterName}</span>
          <span className="text-[9px] px-1.5 py-0.5 rounded-full bg-cyan-500/15 text-cyan-300 border border-cyan-400/25 font-semibold">
            AI Host
          </span>
          {isPlaying && (
            <span className="flex items-center gap-1 text-[10px] text-cyan-400 font-medium ml-1">
              <Volume2 className="w-3 h-3 animate-pulse" />
              <span>در حال صحبت...</span>
            </span>
          )}
        </div>

        {/* Liquid Glass Bubble with Specular Top Highlight */}
        <div className="relative apple-glass rounded-[20px] rounded-tl-[6px] p-4 text-white border border-white/18 shadow-[0_8px_32px_0_rgba(0,0,0,0.35)] backdrop-blur-[14px] bg-[rgba(15,23,42,0.60)] overflow-hidden">
          {/* Specular Rim Light */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
          />

          {/* Active Audio Waveform Overlay while playing TTS */}
          {isPlaying && (
            <div className="absolute top-2 right-3 flex items-center gap-0.5 px-2 py-1 rounded-full bg-cyan-950/40 border border-cyan-400/30">
              <span className="w-0.5 h-2.5 bg-cyan-400 rounded-full animate-bounce [animation-duration:450ms]" />
              <span className="w-0.5 h-4 bg-cyan-300 rounded-full animate-bounce [animation-duration:320ms] [animation-delay:100ms]" />
              <span className="w-0.5 h-2 bg-cyan-400 rounded-full animate-bounce [animation-duration:480ms] [animation-delay:200ms]" />
              <span className="w-0.5 h-3.5 bg-cyan-300 rounded-full animate-bounce [animation-duration:380ms] [animation-delay:150ms]" />
            </div>
          )}

          {/* Main English Transcript */}
          {message ? (
            <p className="text-[15px] sm:text-[16px] leading-[1.65] font-normal tracking-wide text-white select-text text-left">
              {message}
              {isStreaming && (
                <span className="inline-block w-2 h-4 ml-1.5 bg-cyan-400 rounded-sm animate-pulse align-middle" />
              )}
            </p>
          ) : (
            /* Siri Iridescent Pulsing Dots */
            <div className="flex items-center gap-1.5 py-1 text-xs text-slate-300">
              <span className="w-2 h-2 rounded-full bg-[#af52de] animate-bounce" />
              <span className="w-2 h-2 rounded-full bg-[#06b6d4] animate-bounce [animation-delay:0.15s]" />
              <span className="w-2 h-2 rounded-full bg-[#007aff] animate-bounce [animation-delay:0.3s]" />
              <span className="mr-2 text-xs font-medium text-slate-400" dir="rtl">
                در حال پاسخ...
              </span>
            </div>
          )}

          {/* Expandable Persian Translation inside bubble */}
          <AnimatePresence>
            {showTranslation && hasTranslation && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                transition={{ duration: 0.22, ease: 'easeOut' }}
                className="overflow-hidden"
              >
                <div className="w-full h-[1px] bg-white/10 my-3" />
                <p
                  className="text-[13.5px] text-slate-200 leading-[1.7] font-sans text-right select-text text-justify"
                  dir="rtl"
                >
                  {translatedMessage}
                </p>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* 3. Apple Labelled Action Chips Row beneath bubble */}
        {message && !isStreaming && (
          <div className="flex items-center gap-2 pt-1 select-none" dir="rtl">
            {/* Audio Voice Chip */}
            <motion.button
              type="button"
              whileTap={{ scale: 0.94 }}
              onClick={isPlaying ? onStopAudio : onPlayAudio}
              className={`h-7 px-3 rounded-full text-[11.5px] font-medium flex items-center gap-1.5 transition-all touch-manipulation border ${
                isPlaying
                  ? 'apple-glass-blue text-cyan-300 border-cyan-400/50 shadow-apple-sm'
                  : 'apple-glass-pill text-slate-200 border-white/20 hover:bg-white/15'
              }`}
              title="پخش صوت مکالمه"
            >
              {isPlaying ? (
                <>
                  <Square className="w-3 h-3 fill-cyan-300 text-cyan-300" />
                  <span>توقف</span>
                  {/* Dynamic Equalizer Waveform */}
                  <div className="flex items-center gap-0.5 mr-0.5">
                    <span className="w-0.5 h-2 bg-cyan-300 rounded-full animate-bounce [animation-duration:400ms]" />
                    <span className="w-0.5 h-3 bg-cyan-300 rounded-full animate-bounce [animation-duration:320ms] [animation-delay:120ms]" />
                    <span className="w-0.5 h-1.5 bg-cyan-300 rounded-full animate-bounce [animation-duration:450ms] [animation-delay:220ms]" />
                  </div>
                </>
              ) : (
                <>
                  <Play className="w-3 h-3 fill-cyan-400 text-cyan-400" />
                  <span>پخش صدا</span>
                </>
              )}
            </motion.button>

            {/* Translation Toggle Chip */}
            {hasTranslation && (
              <motion.button
                type="button"
                whileTap={{ scale: 0.94 }}
                onClick={() => setShowTranslation((prev) => !prev)}
                className={`h-7 px-3 rounded-full text-[11.5px] font-medium flex items-center gap-1.5 transition-all touch-manipulation border ${
                  showTranslation
                    ? 'apple-glass-gold text-amber-300 border-amber-400/50'
                    : 'apple-glass-pill text-slate-200 border-white/20 hover:bg-white/15'
                }`}
              >
                <Languages className="w-3.5 h-3.5 text-amber-300" />
                <span>{showTranslation ? 'بستن ترجمه' : 'ترجمه'}</span>
              </motion.button>
            )}

            {/* Copy Chip */}
            <motion.button
              type="button"
              whileTap={{ scale: 0.94 }}
              onClick={handleCopy}
              className={`h-7 px-2.5 rounded-full text-[11.5px] font-medium flex items-center gap-1.5 transition-all touch-manipulation border ${
                copied
                  ? 'apple-glass-pill bg-cyan-950/40 text-cyan-300 border-cyan-400/50'
                  : 'apple-glass-pill text-slate-200 border-white/20 hover:bg-white/15'
              }`}
            >
              {copied ? (
                <>
                  <Check className="w-3 h-3 text-cyan-300 stroke-[2.5]" />
                  <span>کپی شد</span>
                </>
              ) : (
                <>
                  <Copy className="w-3 h-3 text-slate-400" />
                  <span>کپی</span>
                </>
              )}
            </motion.button>
          </div>
        )}
      </div>
    </div>
  );
};
