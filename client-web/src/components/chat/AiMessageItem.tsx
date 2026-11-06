import React, { useState } from 'react';
import { Volume2, VolumeX, Globe, Sparkles } from 'lucide-react';
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
}

export const AiMessageItem: React.FC<AiMessageItemProps> = ({
  message,
  translatedMessage,
  characterName,
  characterAvatarUrl,
  isStreaming = false,
  isPlaying = false,
  onPlayAudio,
}) => {
  const [showTranslation, setShowTranslation] = useState(false);

  return (
    <div className="flex items-start gap-3 justify-start max-w-[90%] sm:max-w-[82%] my-3 mr-auto" dir="ltr">
      {/* 1. Character Avatar Squircle (Anchored to the left) */}
      <div className="relative shrink-0">
        <div className="w-10 h-10 rounded-2xl overflow-hidden apple-glass border border-white/20 shadow-apple-sm flex items-center justify-center bg-gradient-to-br from-[#2c2d3a] to-[#16171f]">
          {characterAvatarUrl ? (
            <img
              src={characterAvatarUrl}
              alt={characterName}
              className="w-full h-full object-cover"
              onError={(e) => {
                // Fallback to initial
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          ) : (
            <span className="text-sm font-bold text-apple-blue">
              {characterName.slice(0, 2).toUpperCase()}
            </span>
          )}
        </div>
        {/* Active AI Indicator Dot */}
        {isStreaming && (
          <span className="absolute -top-0.5 -right-0.5 w-3 h-3 rounded-full bg-apple-blue animate-ping" />
        )}
      </div>

      {/* 2. Frosted Liquid Glass Message Card (Tails toward the left avatar) */}
      <div className="space-y-1.5 flex-1 min-w-0">
        {/* Character Title */}
        <div className="flex items-center gap-1.5 px-1" dir="ltr">
          <span className="text-[11px] font-semibold text-apple-gray-300">{characterName}</span>
          <span className="text-[9px] px-1.5 py-0.5 rounded-full bg-apple-blue/20 text-apple-blue font-bold">
            AI Host
          </span>
        </div>

        <div className="apple-glass rounded-3xl rounded-tl-md p-4 text-white space-y-3 border border-white/14 shadow-apple-md bg-[#1d1f2b]/80 backdrop-blur-2xl">
          {/* Main English Transcript */}
          {message ? (
            <p className="text-[15px] leading-relaxed font-normal tracking-wide text-slate-100 select-text text-left" dir="ltr">
              {message}
              {isStreaming && (
                <span className="inline-block w-2 h-4 ml-1 bg-apple-blue rounded-sm animate-pulse align-middle" />
              )}
            </p>
          ) : (
            /* Siri Thinking State */
            <div className="flex items-center gap-2 py-1 text-xs text-apple-gray-400" dir="rtl">
              <Sparkles className="w-4 h-4 text-apple-blue animate-spin" />
              <span>هوش مصنوعی در حال پاسخ دادن...</span>
            </div>
          )}

          {/* Persian Translation Collapsible */}
          <AnimatePresence>
            {showTranslation && translatedMessage && (
              <motion.div
                initial={{ opacity: 0, height: 0, y: -4 }}
                animate={{ opacity: 1, height: 'auto', y: 0 }}
                exit={{ opacity: 0, height: 0, y: -4 }}
                transition={{ type: 'spring', stiffness: 500, damping: 30 }}
                className="overflow-hidden"
              >
                <div
                  className="p-3 rounded-2xl bg-white/[0.06] border border-white/10 text-xs text-slate-300 leading-relaxed font-sans text-right"
                  dir="rtl"
                >
                  <span className="text-[10px] font-bold text-apple-yellow block mb-0.5">
                    ترجمه روان فارسی:
                  </span>
                  {translatedMessage}
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Action Footer (TTS Voice + Translation Button) */}
          {message && !isStreaming && (
            <div className="flex items-center justify-between pt-1 border-t border-white/10" dir="rtl">
              {/* Audio Playback Pill */}
              <button
                type="button"
                onClick={onPlayAudio}
                className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-full text-xs font-medium transition-all ${
                  isPlaying
                    ? 'bg-apple-blue text-white shadow-apple-sm'
                    : 'bg-white/10 text-slate-200 hover:bg-white/15 active:scale-95'
                }`}
                title="شنیدن تلفظ انگلیسی"
              >
                {isPlaying ? (
                  <>
                    <VolumeX className="w-3.5 h-3.5 shrink-0" />
                    <span>توقف صدا</span>
                    {/* Equalizer animation */}
                    <div className="flex items-center gap-0.5 mr-1">
                      <span className="w-0.5 h-2.5 bg-white rounded-full animate-bounce" />
                      <span className="w-0.5 h-4 bg-white rounded-full animate-bounce [animation-delay:0.15s]" />
                      <span className="w-0.5 h-2 bg-white rounded-full animate-bounce [animation-delay:0.3s]" />
                    </div>
                  </>
                ) : (
                  <>
                    <Volume2 className="w-3.5 h-3.5 text-apple-blue shrink-0" />
                    <span>پخش تلفظ</span>
                  </>
                )}
              </button>

              {/* Translation Toggle Pill */}
              {translatedMessage && (
                <button
                  type="button"
                  onClick={() => setShowTranslation((prev) => !prev)}
                  className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-full text-xs transition-all ${
                    showTranslation
                      ? 'bg-apple-yellow/20 text-apple-yellow font-bold'
                      : 'bg-white/10 text-apple-gray-300 hover:bg-white/15 active:scale-95'
                  }`}
                >
                  <Globe className="w-3.5 h-3.5" />
                  <span>{showTranslation ? 'بستن ترجمه' : 'ترجمه فارسی'}</span>
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
