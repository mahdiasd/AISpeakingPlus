import { useState, useEffect, useRef, useCallback } from 'react';
import { chatApi } from '../api/chatApi';

export interface SpeechRecognitionHook {
  isSupported: boolean;
  isListening: boolean;
  isProcessing: boolean;
  transcript: string;
  audioLevel: number; // 0.0 to 1.0 for real-time Siri waveform
  recordingDuration: number; // seconds
  error: string | null;
  startListening: () => Promise<void>;
  stopListening: () => Promise<void>;
  resetTranscript: () => void;
  cancelListening: () => void;
}

interface IWindow extends Window {
  SpeechRecognition?: any;
  webkitSpeechRecognition?: any;
  AudioContext?: typeof AudioContext;
  webkitAudioContext?: typeof AudioContext;
}

/**
 * Resamples raw Float32 audio samples from native input rate to target 16kHz mono.
 */
function downsampleBuffer(
  buffer: Float32Array,
  inputSampleRate: number,
  outputSampleRate = 16000
): Float32Array {
  if (inputSampleRate === outputSampleRate) {
    return buffer;
  }
  const sampleRateRatio = inputSampleRate / outputSampleRate;
  const newLength = Math.round(buffer.length / sampleRateRatio);
  const result = new Float32Array(newLength);
  let offsetResult = 0;
  let offsetBuffer = 0;

  while (offsetResult < result.length) {
    const nextOffsetBuffer = Math.round((offsetResult + 1) * sampleRateRatio);
    let accum = 0;
    let count = 0;
    for (let i = offsetBuffer; i < nextOffsetBuffer && i < buffer.length; i++) {
      accum += buffer[i];
      count++;
    }
    result[offsetResult] = count > 0 ? accum / count : 0;
    offsetResult++;
    offsetBuffer = nextOffsetBuffer;
  }
  return result;
}

/**
 * Converts Float32 audio samples (-1.0 to 1.0) to 16-bit signed PCM byte array.
 */
function floatToPcm16(samples: Float32Array): Uint8Array {
  const output = new Uint8Array(samples.length * 2);
  for (let i = 0; i < samples.length; i++) {
    const s = Math.max(-1, Math.min(1, samples[i]));
    const val = s < 0 ? s * 0x8000 : s * 0x7fff;
    const intVal = Math.floor(val);
    output[i * 2] = intVal & 0xff;
    output[i * 2 + 1] = (intVal >> 8) & 0xff;
  }
  return output;
}

/**
 * Packs PCM16 byte chunks into a standard 16kHz mono RIFF/WAV Blob compatible with backend Sherpa-ONNX.
 */
function pcmToWav(pcmBytes: Uint8Array, sampleRate = 16000): Blob {
  const buffer = new ArrayBuffer(44 + pcmBytes.length);
  const view = new DataView(buffer);

  // RIFF identifier
  view.setUint32(0, 0x52494646, false); // "RIFF"
  view.setUint32(4, 36 + pcmBytes.length, true); // file length - 8
  view.setUint32(8, 0x57415645, false); // "WAVE"

  // Format chunk
  view.setUint32(12, 0x666d7420, false); // "fmt "
  view.setUint32(16, 16, true); // Subchunk1Size (16 for PCM)
  view.setUint16(20, 1, true); // AudioFormat (1 = PCM)
  view.setUint16(22, 1, true); // NumChannels (1 = Mono)
  view.setUint32(24, sampleRate, true); // SampleRate (16000)
  view.setUint32(28, sampleRate * 2, true); // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
  view.setUint16(32, 2, true); // BlockAlign (NumChannels * BitsPerSample/8)
  view.setUint16(34, 16, true); // BitsPerSample (16 bits)

  // Data chunk
  view.setUint32(36, 0x64617461, false); // "data"
  view.setUint32(40, pcmBytes.length, true); // data size
  new Uint8Array(buffer, 44).set(pcmBytes);

  return new Blob([buffer], { type: 'audio/wav' });
}

export function useSpeechRecognition(onResultFinal?: (text: string) => void): SpeechRecognitionHook {
  const [isSupported, setIsSupported] = useState(true);
  const [isListening, setIsListening] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [audioLevel, setAudioLevel] = useState(0);
  const [recordingDuration, setRecordingDuration] = useState(0);
  const [error, setError] = useState<string | null>(null);

  // Audio recording & processing references
  const audioContextRef = useRef<AudioContext | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const audioProcessorRef = useRef<ScriptProcessorNode | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const pcmChunksRef = useRef<Uint8Array[]>([]);
  const animFrameRef = useRef<number | null>(null);
  const durationTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // Web Speech API fallback reference
  const recognitionRef = useRef<any>(null);
  const recognitionTranscriptRef = useRef('');

  // Clean up Web Speech API instance
  const cleanupRecognition = useCallback(() => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.onstart = null;
        recognitionRef.current.onresult = null;
        recognitionRef.current.onerror = null;
        recognitionRef.current.onend = null;
        recognitionRef.current.abort();
      } catch {}
      recognitionRef.current = null;
    }
  }, []);

  // Clean up AudioContext & MediaStream
  const cleanupAudio = useCallback(() => {
    if (animFrameRef.current) {
      cancelAnimationFrame(animFrameRef.current);
      animFrameRef.current = null;
    }
    if (durationTimerRef.current) {
      clearInterval(durationTimerRef.current);
      durationTimerRef.current = null;
    }

    if (audioProcessorRef.current) {
      try {
        audioProcessorRef.current.disconnect();
        audioProcessorRef.current.onaudioprocess = null;
      } catch {}
      audioProcessorRef.current = null;
    }

    if (analyserRef.current) {
      try {
        analyserRef.current.disconnect();
      } catch {}
      analyserRef.current = null;
    }

    if (mediaStreamRef.current) {
      try {
        mediaStreamRef.current.getTracks().forEach((track) => track.stop());
      } catch {}
      mediaStreamRef.current = null;
    }

    if (audioContextRef.current && audioContextRef.current.state !== 'closed') {
      try {
        audioContextRef.current.close().catch(() => {});
      } catch {}
      audioContextRef.current = null;
    }

    setAudioLevel(0);
    setRecordingDuration(0);
  }, []);

  useEffect(() => {
    const isMediaAvailable =
      typeof navigator !== 'undefined' &&
      !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia);
    setIsSupported(isMediaAvailable);

    return () => {
      cleanupRecognition();
      cleanupAudio();
    };
  }, [cleanupRecognition, cleanupAudio]);

  /**
   * Start listening and recording microphone audio.
   */
  const startListening = useCallback(async () => {
    setError(null);
    setTranscript('');
    recognitionTranscriptRef.current = '';
    pcmChunksRef.current = [];

    // Verify browser support for microphone
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      setError('مرورگر شما از دسترسی به میکروفون پشتیبانی نمی‌کند.');
      return;
    }

    try {
      // 1. Request microphone stream
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      });
      mediaStreamRef.current = stream;

      // 2. Setup Web Audio API Graph
      const win = window as unknown as IWindow;
      const AudioCtxClass = win.AudioContext || win.webkitAudioContext;
      if (!AudioCtxClass) {
        setError('پشتیبانی صوتی مرورگر فعال نیست.');
        return;
      }

      const audioCtx = new AudioCtxClass();
      if (audioCtx.state === 'suspended') {
        await audioCtx.resume();
      }
      audioContextRef.current = audioCtx;

      const source = audioCtx.createMediaStreamSource(stream);
      const analyser = audioCtx.createAnalyser();
      analyser.fftSize = 256;
      analyser.smoothingTimeConstant = 0.5;
      analyserRef.current = analyser;

      // Script processor for capturing PCM samples
      const processor = audioCtx.createScriptProcessor(2048, 1, 1);
      audioProcessorRef.current = processor;

      processor.onaudioprocess = (e) => {
        const inputData = e.inputBuffer.getChannelData(0);
        const inputRate = audioCtx.sampleRate || 16000;
        const downsampled = downsampleBuffer(inputData, inputRate, 16000);
        if (downsampled.length > 0) {
          const pcmBytes = floatToPcm16(downsampled);
          pcmChunksRef.current.push(pcmBytes);
        }
      };

      // Connect nodes: source -> analyser -> processor -> silent destination
      const zeroGain = audioCtx.createGain();
      zeroGain.gain.value = 0;

      source.connect(analyser);
      analyser.connect(processor);
      processor.connect(zeroGain);
      zeroGain.connect(audioCtx.destination);

      // 3. Audio volume visualizer loop (for Apple Siri Waveform)
      const dataArray = new Uint8Array(analyser.frequencyBinCount);
      const updateVolume = () => {
        if (!analyserRef.current) return;
        analyserRef.current.getByteFrequencyData(dataArray);

        let sum = 0;
        for (let i = 0; i < dataArray.length; i++) {
          sum += dataArray[i];
        }
        const avg = sum / dataArray.length;
        // Normalize 0-255 to 0.0-1.0 with soft dampening
        const normalized = Math.min(1, Math.max(0, avg / 128));
        setAudioLevel(normalized);

        animFrameRef.current = requestAnimationFrame(updateVolume);
      };
      animFrameRef.current = requestAnimationFrame(updateVolume);

      // Duration counter
      const startTime = Date.now();
      durationTimerRef.current = setInterval(() => {
        setRecordingDuration(Math.floor((Date.now() - startTime) / 1000));
      }, 500);

      setIsListening(true);

      // 4. Concurrently start Web Speech API for instant live transcript
      const SpeechRec = win.SpeechRecognition || win.webkitSpeechRecognition;
      if (SpeechRec) {
        try {
          const rec = new SpeechRec();
          rec.continuous = true;
          rec.interimResults = true;
          rec.lang = 'en-US';

          rec.onresult = (event: any) => {
            let interimTranscript = '';
            let finalTranscript = '';
            for (let i = 0; i < event.results.length; i++) {
              if (event.results[i].isFinal) {
                finalTranscript += event.results[i][0].transcript + ' ';
              } else {
                interimTranscript += event.results[i][0].transcript + ' ';
              }
            }
            const clean = (finalTranscript + interimTranscript).trim();
            if (clean) {
              setTranscript(clean);
              recognitionTranscriptRef.current = clean;
            }
          };

          rec.onerror = (event: any) => {
            console.warn('[SpeechRecognition] WebSpeech error/fallback:', event.error);
          };

          rec.start();
          recognitionRef.current = rec;
        } catch (recErr) {
          console.warn('[SpeechRecognition] Init failed, using server STT:', recErr);
        }
      }
    } catch (err: any) {
      console.error('Failed to start microphone recording:', err);
      if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
        setError('دسترسی به میکروفون داده نشده است. لطفاً در تنظیمات مرورگر، دسترسی را فعال کنید.');
      } else {
        setError('خطا در دسترسی به میکروفون دستگاه.');
      }
      cleanupAudio();
      cleanupRecognition();
      setIsListening(false);
    }
  }, [cleanupAudio, cleanupRecognition]);

  /**
   * Stop listening, package audio, and submit final transcribed text.
   */
  const stopListening = useCallback(async () => {
    setIsListening(false);

    // Stop live recognition
    cleanupRecognition();

    // Collect all recorded PCM chunks before cleaning up audio context
    const chunks = [...pcmChunksRef.current];
    cleanupAudio();

    const localTranscript = (recognitionTranscriptRef.current || transcript).trim();

    // Scenario A: Web Speech API gave a clean transcript
    if (localTranscript.length > 0) {
      setTranscript(localTranscript);
      onResultFinal?.(localTranscript);
      return;
    }

    // Scenario B: Fall back to server-side Sherpa-ONNX STT with recorded audio chunks
    if (chunks.length > 0) {
      try {
        setIsProcessing(true);

        // Combine chunks
        let totalLen = 0;
        for (const chunk of chunks) {
          totalLen += chunk.length;
        }

        const combined = new Uint8Array(totalLen);
        let offset = 0;
        for (const chunk of chunks) {
          combined.set(chunk, offset);
          offset += chunk.length;
        }

        // Must have at least ~0.3s of audio (0.3 * 16000 * 2 bytes = 9600 bytes)
        if (combined.length < 4000) {
          setError('صوت بسیار کوتاه بود. لطفاً جمله خود را کامل بیان کنید.');
          setIsProcessing(false);
          return;
        }

        // Generate 16kHz mono WAV Blob
        const wavBlob = pcmToWav(combined, 16000);

        // Send to backend Sherpa-ONNX STT
        const result = await chatApi.transcribeAudio(wavBlob);
        const recognizedText = (result.text || '').trim();

        if (recognizedText.length > 0) {
          setTranscript(recognizedText);
          onResultFinal?.(recognizedText);
        } else {
          setError('صدایی تشخیص داده نشد. لطفاً شفاف‌تر به زبان انگلیسی صحبت کنید.');
        }
      } catch (sttErr) {
        console.error('Server STT transcribe failed:', sttErr);
        setError('خطا در پردازش صدا در سرور. لطفاً مجدداً امتحان کنید.');
      } finally {
        setIsProcessing(false);
      }
    } else {
      setError('هیچ صدایی ضبط نشد. لطفاً دسترسی میکروفون را بررسی کنید.');
    }
  }, [cleanupAudio, cleanupRecognition, onResultFinal, transcript]);

  /**
   * Cancel and discard current recording.
   */
  const cancelListening = useCallback(() => {
    setIsListening(false);
    setIsProcessing(false);
    setTranscript('');
    recognitionTranscriptRef.current = '';
    pcmChunksRef.current = [];
    cleanupRecognition();
    cleanupAudio();
  }, [cleanupAudio, cleanupRecognition]);

  const resetTranscript = useCallback(() => {
    setTranscript('');
    recognitionTranscriptRef.current = '';
  }, []);

  return {
    isSupported,
    isListening,
    isProcessing,
    transcript,
    audioLevel,
    recordingDuration,
    error,
    startListening,
    stopListening,
    resetTranscript,
    cancelListening,
  };
}
