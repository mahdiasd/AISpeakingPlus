import React, { useState } from 'react';
import { Play, Square, Languages, Copy, Check } from 'lucide-react';
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
 * Exact layout match of Compose AiChatItem.kt:
 * - Left avatar squircle (36px) with specular ring
 * - Bubble shape: rounded-[20px] rounded-tl-[6px]
 * - Translucent Apple Liquid Glass with specular rim highlight
 * - Expandable Persian translation separated by clean divider
 * - Labelled Action Chips Row beneath bubble (Voice with equalizer, Translate, Copy)
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

  return (
    <div className="flex items-start gap-2.5 justify-start max-w-[92%] sm:max-w-[82%] my-3 mr-auto" dir="ltr">
      {/* 1. Character Avatar (36px Squircle with Specular Ring) */}
      <div className="relative shrink-0 mt-0.5">
        <div className="w-9 h-9 rounded-full overflow-hidden apple-glass border border-white/25 shadow-apple-sm flex items-center justify-center bg-gradient-to-br from-[#2a2d3d] to-[#141520]">
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
            <span className="text-xs font-bold text-apple-teal">
              {characterName.slice(0, 2).toUpperCase()}
            </span>
          )}
        </div>
      </div>

      {/* 2. Message Column */}
      <div className="space-y-2 flex-1 min-w-0">
        {/* Character Name Label */}
        <div className="flex items-center gap-1.5 px-1 select-none" dir="ltr">
          <span className="text-[11px] font-semibold text-apple-gray-300">{characterName}</span>
          <span className="text-[9px] px-1.5 py-0.2 rounded-md bg-apple-blue/20 text-apple-teal font-semibold">
            AI Host
          </span>
        </div>

        {/* Liquid Glass Bubble with Specular Top Highlight */}
        <div className="relative apple-glass rounded-[20px] rounded-tl-[6px] p-4 text-white border border-white/22 shadow-apple-md backdrop-blur-2xl overflow-hidden">
          {/* Specular Rim Light */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
          />

          {/* Main English Transcript */}
          {message ? (
            <p className="text-[15px] sm:text-[16px] leading-relaxed font-normal tracking-wide text-white select-text text-left">
              {message}
              {isStreaming && (
                <span className="inline-block w-2 h-4 ml-1.5 bg-apple-teal rounded-sm animate-pulse align-middle" />
              )}
            </p>
          ) : (
            /* Siri Iridescent Pulsing Dots */
            <div className="flex items-center gap-1.5 py-1 text-xs text-apple-gray-300">
              <span className="w-2 h-2 rounded-full bg-apple-blue animate-bounce" />
              <span className="w-2 h-2 rounded-full bg-apple-purple animate-bounce [animation-delay:0.15s]" />
              <span className="w-2 h-2 rounded-full bg-apple-teal animate-bounce [animation-delay:0.3s]" />
              <span className="mr-2 text-xs font-medium text-apple-gray-400" dir="rtl">
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
                transition={{ duration: 0.2 }}
                className="overflow-hidden"
              >
                <div className="w-full h-[1px] bg-white/10 my-2.5" />
                <p className="text-[13px] text-slate-300 leading-relaxed font-sans text-right select-text text-justify" dir="rtl">
                  {translatedMessage}
                </p>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* 3. Apple Labelled Action Chips Row beneath bubble (Matches Compose AiChatItem) */}
        {message && !isStreaming && (
          <div className="flex items-center gap-2 pt-0.5 select-none" dir="rtl">
            {/* Audio Voice Chip */}
            <button
              type="button"
              onClick={isPlaying ? onStopAudio : onPlayAudio}
              className={`h-7 px-2.5 rounded-full text-[11px] font-medium flex items-center gap-1.5 transition-all touch-manipulation border ${
                isPlaying
                  ? 'apple-glass-blue text-cyan-300 border-cyan-400/50 shadow-apple-sm'
                  : 'apple-glass-pill text-slate-300 border-white/20 hover:bg-white/15 active:scale-95'
              }`}
              title="پخش صوت مکالمه"
            >
              {isPlaying ? (
                <>
                  <Square className="w-3 h-3 fill-cyan-300 text-cyan-300" />
                  <span>توقف</span>
                  {/* Dynamic Equalizer Waveform */}
                  <div className="flex items-center gap-0.5 mr-0.5">
                    <span className="w-0.5 h-2 bg-cyan-300 rounded-full animate-bounce" />
                    <span className="w-0.5 h-3 bg-cyan-300 rounded-full animate-bounce [animation-delay:0.15s]" />
                    <span className="w-0.5 h-1.5 bg-cyan-300 rounded-full animate-bounce [animation-delay:0.3s]" />
                  </div>
                </>
              ) : (
                <>
                  <Play className="w-3 h-3 fill-apple-teal text-apple-teal" />
                  <span>پخش</span>
                </>
              )}
            </button>

            {/* Translation Toggle Chip */}
            {hasTranslation && (
              <button
                type="button"
                onClick={() => setShowTranslation((prev) => !prev)}
                className={`h-7 px-2.5 rounded-full text-[11px] font-medium flex items-center gap-1.5 transition-all touch-manipulation border ${
                  showTranslation
                    ? 'apple-glass-gold text-amber-300 border-amber-400/50'
                    : 'apple-glass-pill text-slate-300 border-white/20 hover:bg-white/15 active:scale-95'
                }`}
              >
                <Languages className="w-3 h-3 text-amber-300" />
                <span>{showTranslation ? 'بستن ترجمه' : 'ترجمه'}</span>
              </button>
            )}

            {/* Copy Chip */}
            <button
              type="button"
              onClick={handleCopy}
              className={`h-7 px-2.5 rounded-full text-[11px] font-medium flex items-center gap-1.5 transition-all touch-manipulation border ${
                copied
                  ? 'apple-glass-pill bg-cyan-950/40 text-cyan-300 border-cyan-400/40'
                  : 'apple-glass-pill text-slate-300 border-white/20 hover:bg-white/15 active:scale-95'
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
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
