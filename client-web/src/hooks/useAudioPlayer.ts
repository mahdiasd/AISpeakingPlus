import { useState, useRef, useCallback, useEffect } from 'react';

export function useAudioPlayer() {
  const [playingId, setPlayingId] = useState<string | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  const stop = useCallback(() => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    setPlayingId(null);
  }, []);

  const play = useCallback(
    (id: string, text: string, audioUrl?: string | null) => {
      // If currently playing the same id, stop it
      if (playingId === id) {
        stop();
        return;
      }

      stop();
      setPlayingId(id);

      // 1. Try server audio file if audioUrl is provided
      if (audioUrl) {
        try {
          const audio = new Audio(audioUrl);
          audioRef.current = audio;

          audio.onended = () => {
            setPlayingId(null);
            audioRef.current = null;
          };

          audio.onerror = () => {
            console.warn('Audio URL playback failed, falling back to Web SpeechSynthesis', audioUrl);
            playWithSynthesis(id, text);
          };

          audio.play().catch((err) => {
            console.warn('Audio play() promise rejected, falling back to synthesis', err);
            playWithSynthesis(id, text);
          });
          return;
        } catch {
          playWithSynthesis(id, text);
          return;
        }
      }

      // 2. Fallback to Web SpeechSynthesis
      playWithSynthesis(id, text);
    },
    [playingId, stop]
  );

  const playWithSynthesis = (_id: string, text: string) => {
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) {
      setPlayingId(null);
      return;
    }

    try {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.rate = 0.95; // Slightly slower for language learners

      // Try selecting a native English voice
      const voices = window.speechSynthesis.getVoices();
      const englishVoice =
        voices.find((v) => v.lang.startsWith('en') && v.name.includes('Natural')) ||
        voices.find((v) => v.lang.startsWith('en-GB')) ||
        voices.find((v) => v.lang.startsWith('en-US')) ||
        voices.find((v) => v.lang.startsWith('en'));

      if (englishVoice) {
        utterance.voice = englishVoice;
      }

      utterance.onend = () => {
        setPlayingId(null);
      };

      utterance.onerror = () => {
        setPlayingId(null);
      };

      window.speechSynthesis.speak(utterance);
    } catch (e) {
      console.warn('SpeechSynthesis failed', e);
      setPlayingId(null);
    }
  };

  return {
    playingId,
    play,
    stop,
    isPlaying: (id: string) => playingId === id,
  };
}
