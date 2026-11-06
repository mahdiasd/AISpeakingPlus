import { useState, useEffect, useRef, useCallback } from 'react';

export interface SpeechRecognitionHook {
  isSupported: boolean;
  isListening: boolean;
  transcript: string;
  error: string | null;
  startListening: () => void;
  stopListening: () => void;
  resetTranscript: () => void;
}

// Typing for Web Speech API
interface IWindow extends Window {
  SpeechRecognition?: any;
  webkitSpeechRecognition?: any;
}

export function useSpeechRecognition(onResultFinal?: (text: string) => void): SpeechRecognitionHook {
  const [isSupported, setIsSupported] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [error, setError] = useState<string | null>(null);

  const recognitionRef = useRef<any>(null);

  useEffect(() => {
    const win = typeof window !== 'undefined' ? (window as unknown as IWindow) : null;
    const SpeechRecognition = win?.SpeechRecognition || win?.webkitSpeechRecognition;

    if (SpeechRecognition) {
      setIsSupported(true);
      try {
        const recognition = new SpeechRecognition();
        recognition.continuous = false;
        recognition.interimResults = true;
        recognition.lang = 'en-US';

        recognition.onstart = () => {
          setIsListening(true);
          setError(null);
        };

        recognition.onresult = (event: any) => {
          let currentTranscript = '';
          let isFinal = false;

          for (let i = event.resultIndex; i < event.results.length; i++) {
            const res = event.results[i];
            currentTranscript += res[0].transcript;
            if (res.isFinal) {
              isFinal = true;
            }
          }

          setTranscript(currentTranscript);

          if (isFinal && currentTranscript.trim().length > 0) {
            onResultFinal?.(currentTranscript.trim());
          }
        };

        recognition.onerror = (event: any) => {
          console.warn('SpeechRecognition error:', event.error);
          if (event.error === 'not-allowed') {
            setError('دسترسی به میکروفون داده نشده است.');
          } else if (event.error === 'no-speech') {
            // No speech detected, silent recovery
          } else {
            setError(`خطا در تشخیص گفتار: ${event.error}`);
          }
          setIsListening(false);
        };

        recognition.onend = () => {
          setIsListening(false);
        };

        recognitionRef.current = recognition;
      } catch (err) {
        console.error('Failed to initialize SpeechRecognition', err);
        setIsSupported(false);
      }
    } else {
      setIsSupported(false);
    }

    return () => {
      if (recognitionRef.current) {
        try {
          recognitionRef.current.abort();
        } catch {}
      }
    };
  }, [onResultFinal]);

  const startListening = useCallback(() => {
    setError(null);
    setTranscript('');
    if (recognitionRef.current) {
      try {
        recognitionRef.current.start();
      } catch (e: any) {
        // If already started, stop then restart
        try {
          recognitionRef.current.stop();
          setTimeout(() => recognitionRef.current?.start(), 150);
        } catch {
          setError('میکروفون در حال حاضر مشغول است.');
        }
      }
    } else {
      setError('مرورگر شما از تشخیص مستقیم گفتار انگلیسی پشتیبانی نمی‌کند.');
    }
  }, []);

  const stopListening = useCallback(() => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
      } catch {}
    }
    setIsListening(false);
  }, []);

  const resetTranscript = useCallback(() => {
    setTranscript('');
  }, []);

  return {
    isSupported,
    isListening,
    transcript,
    error,
    startListening,
    stopListening,
    resetTranscript,
  };
}
