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
 * 100% matching Compose Multiplatform `AiChatItem.kt`:
 * - Compact layout: small circular character avatar on top-left of bubble.
 * - NO "AI Host" or character name headers.
 * - High-clarity frosted glass bubble (.apple-glass-ai-bubble) allowing stage scenery to shine through.
 * - Compact horizontal action chips row: [پخش صدا], [ترجمه], [کپی].
 * - NO loading animation inside bubble when empty.
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

  // If no message text exists yet, do not render an empty bubble
  if (!message || message.trim().length === 0) {
    return null;
  }

  const handleCopy = () => {
    navigator.clipboard?.writeText(message);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const hasTranslation = Boolean(translatedMessage && translatedMessage.trim().length > 0);
  const initialChar = (characterName ? characterName.trim().charAt(0) : 'E').toUpperCase();

  return (
    <div className="flex items-start gap-2 justify-start max-w-[94%] sm:max-w-[85%] my-1.5 mr-auto self-start select-none" dir="ltr">
      {/* 1. Character Avatar (34px Circle matching Compose GameAvatar) */}
      <div className="w-[34px] h-[34px] rounded-full overflow-hidden shrink-0 mt-0.5 border border-white/30 shadow-sm bg-[#161826] flex items-center justify-center">
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
          <span className="text-xs font-bold text-cyan-300">
            {initialChar}
          </span>
        )}
      </div>

      {/* 2. Message Content Column (NO "AI Host" or name label) */}
      <div className="flex-1 min-w-0 space-y-1">
        {/* Apple Translucent Glass Bubble with Specular Top Highlight */}
        <div className="apple-glass-ai-bubble relative rounded-[20px] rounded-tl-[6px] px-3.5 py-2.5 text-white overflow-hidden">
          {/* Specular Rim Light */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 top-0 h-[1px] bg-gradient-to-r from-white/70 via-white/20 to-transparent pointer-events-none"
          />

          {/* Main English Transcript */}
          <p className="text-[14.5px] sm:text-[15px] leading-[1.55] font-normal tracking-wide text-white select-text text-left">
            {message}
            {isStreaming && (
              <span className="inline-block w-1.5 h-3.5 ml-1 bg-cyan-400 rounded-sm animate-pulse align-middle" />
            )}
          </p>

          {/* Expandable Persian Translation inside bubble */}
          <AnimatePresence>
            {showTranslation && hasTranslation && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                transition={{ duration: 0.18, ease: 'easeOut' }}
                className="overflow-hidden"
              >
                <div className="w-full h-[1px] bg-white/10 my-2" />
                <p
                  className="text-[12.5px] text-slate-200 leading-[1.65] font-sans text-right select-text text-justify"
                  dir="rtl"
                >
                  {translatedMessage}
                </p>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* 3. Compact Action Chips Row beneath bubble (Matching Compose GameChip) */}
        {!isStreaming && (
          <div className="flex items-center gap-1.5 pt-0.5 select-none" dir="rtl">
            {/* Audio Voice Chip */}
            <motion.button
              type="button"
              whileTap={{ scale: 0.94 }}
              onClick={isPlaying ? onStopAudio : onPlayAudio}
              className={`h-[26px] px-2.5 rounded-full text-[11px] font-medium flex items-center gap-1 transition-all touch-manipulation border ${
                isPlaying
                  ? 'bg-cyan-500/25 text-cyan-300 border-cyan-400/50 shadow-sm'
                  : 'bg-white/10 hover:bg-white/15 text-slate-200 border-white/15'
              }`}
              title="پخش صوت مکالمه"
            >
              {isPlaying ? (
                <>
                  <Square className="w-2.5 h-2.5 fill-cyan-300 text-cyan-300" />
                  <span>توقف</span>
                  <div className="flex items-center gap-0.5 mr-0.5">
                    <span className="w-0.5 h-2 bg-cyan-300 rounded-full animate-bounce [animation-duration:400ms]" />
                    <span className="w-0.5 h-3 bg-cyan-300 rounded-full animate-bounce [animation-duration:320ms] [animation-delay:120ms]" />
                    <span className="w-0.5 h-1.5 bg-cyan-300 rounded-full animate-bounce [animation-duration:450ms] [animation-delay:220ms]" />
                  </div>
                </>
              ) : (
                <>
                  <Play className="w-2.5 h-2.5 fill-cyan-400 text-cyan-400 ml-0.5" />
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
                className={`h-[26px] px-2.5 rounded-full text-[11px] font-medium flex items-center gap-1 transition-all touch-manipulation border ${
                  showTranslation
                    ? 'bg-amber-400/20 text-amber-300 border-amber-400/40'
                    : 'bg-white/10 hover:bg-white/15 text-slate-200 border-white/15'
                }`}
              >
                <Languages className="w-3 h-3 text-amber-300" />
                <span>{showTranslation ? 'بستن' : 'ترجمه'}</span>
              </motion.button>
            )}

            {/* Copy Chip */}
            <motion.button
              type="button"
              whileTap={{ scale: 0.94 }}
              onClick={handleCopy}
              className={`h-[26px] px-2 rounded-full text-[11px] font-medium flex items-center gap-1 transition-all touch-manipulation border ${
                copied
                  ? 'bg-cyan-950/40 text-cyan-300 border-cyan-400/50'
                  : 'bg-white/10 hover:bg-white/15 text-slate-300 border-white/15'
              }`}
            >
              {copied ? (
                <>
                  <Check className="w-2.5 h-2.5 text-cyan-300 stroke-[2.5]" />
                  <span>کپی شد</span>
                </>
              ) : (
                <>
                  <Copy className="w-2.5 h-2.5 text-slate-400" />
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
