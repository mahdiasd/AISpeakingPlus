import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { TtsVoiceInfo, TtsSynthesizeResponse } from '../types';
import { encodeWav, float32ToInt16, decodeAndResampleTo16k } from '../utils/audio';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import {
  Volume2,
  Mic,
  MicOff,
  Play,
  Pause,
  Download,
  Copy,
  Sparkles,
  Sliders,
  AudioWaveform,
  Check,
  Radio,
  Upload,
  ArrowRightLeft,
  Activity,
  Trash2,
} from 'lucide-react';

const TTS_SAMPLE_PHRASES = [
  {
    category: 'مکالمه روزمره',
    text: "Good morning! How are you doing today? I was wondering if you could help me with directions to the city museum.",
  },
  {
    category: 'سفارش در رستوران',
    text: "Hi there, could I please get a medium vanilla latte with oat milk, and a warm chocolate croissant to go?",
  },
  {
    category: 'مصاحبه کاری',
    text: "I have over five years of experience designing scalable frontend architectures and working with international engineering teams.",
  },
  {
    category: 'تست تلفظ و سرعت',
    text: "Peter Piper picked a peck of pickled peppers. How many pickled peppers did Peter Piper pick?",
  },
];

export const VoiceLabPage: React.FC = () => {
  const { success, error: toastError, info } = useToast();

  // Active Tab: 'tts' | 'stt' | 'loopback'
  const [activeTab, setActiveTab] = useState<'tts' | 'stt' | 'loopback'>('tts');

  // ===================== TTS STATE =====================
  const [voices, setVoices] = useState<TtsVoiceInfo[]>([]);
  const [selectedVoiceId, setSelectedVoiceId] = useState<number>(0);
  const [ttsText, setTtsText] = useState<string>(
    "Hello! Welcome to AI Speaking Plus. You can test my voice and pitch right here to ensure the speech model sounds natural."
  );
  const [ttsSpeed, setTtsSpeed] = useState<number>(1.0);
  const [isSynthesizing, setIsSynthesizing] = useState<boolean>(false);
  const [ttsResult, setTtsResult] = useState<TtsSynthesizeResponse | null>(null);
  const [voiceFilter, setVoiceFilter] = useState<'ALL' | 'FEMALE' | 'MALE' | 'US' | 'UK'>('ALL');
  const [voiceSearch, setVoiceSearch] = useState<string>('');

  // TTS Audio Player State
  const [isPlayingTts, setIsPlayingTts] = useState<boolean>(false);
  const [ttsCurrentTime, setTtsCurrentTime] = useState<number>(0);
  const [ttsDuration, setTtsDuration] = useState<number>(0);
  const ttsAudioRef = useRef<HTMLAudioElement | null>(null);

  // Quick Voice Preview State
  const [previewVoiceId, setPreviewVoiceId] = useState<number | null>(null);
  const previewAudioRef = useRef<HTMLAudioElement | null>(null);

  // ===================== STT STATE =====================
  const [sttMode, setSttMode] = useState<'live' | 'record' | 'upload'>('live');
  const [isRecording, setIsRecording] = useState<boolean>(false);
  const [recordingSeconds, setRecordingSeconds] = useState<number>(0);
  const [sttStatus, setSttStatus] = useState<'idle' | 'connecting' | 'listening' | 'transcribing'>('idle');
  const [finalTranscripts, setFinalTranscripts] = useState<string[]>([]);
  const [livePartial, setLivePartial] = useState<string>('');
  const [recordedAudioBlob, setRecordedAudioBlob] = useState<Blob | null>(null);
  const [recordedAudioUrl, setRecordedAudioUrl] = useState<string | null>(null);
  const [browserTranscript, setBrowserTranscript] = useState<string>('');
  const [copiedText, setCopiedText] = useState<boolean>(false);

  // Web Audio & WebSocket references for STT
  const sttWsRef = useRef<WebSocket | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const scriptProcessorRef = useRef<ScriptProcessorNode | null>(null);
  const recordedBuffersRef = useRef<Float32Array[]>([]);
  const timerIntervalRef = useRef<any>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const animFrameRef = useRef<number | null>(null);
  const browserSpeechRecRef = useRef<any>(null);

  // ===================== LOOPBACK STATE =====================
  const [loopbackStatus, setLoopbackStatus] = useState<'idle' | 'listening' | 'synthesizing' | 'speaking'>('idle');
  const [loopbackText, setLoopbackText] = useState<string>('');
  const [loopbackAudioUrl, setLoopbackAudioUrl] = useState<string | null>(null);
  const loopbackAudioRef = useRef<HTMLAudioElement | null>(null);

  // Load Voices on Mount
  useEffect(() => {
    const fetchVoices = async () => {
      try {
        const data = await api.getTtsVoices();
        setVoices(data);
        if (data.length > 0) {
          setSelectedVoiceId(data[0].id);
        }
      } catch (err: any) {
        toastError(err.message || 'خطا در بارگذاری صداها');
      }
    };
    fetchVoices();
  }, [toastError]);

  // Clean up audio & recording when unmounting
  useEffect(() => {
    return () => {
      stopRecordingCleanup();
      if (ttsAudioRef.current) {
        ttsAudioRef.current.pause();
      }
      if (previewAudioRef.current) {
        previewAudioRef.current.pause();
      }
      if (loopbackAudioRef.current) {
        loopbackAudioRef.current.pause();
      }
    };
  }, []);

  // Filtered Voices list
  const filteredVoices = voices.filter((v) => {
    const matchesFilter =
      voiceFilter === 'ALL' ||
      (voiceFilter === 'FEMALE' && (v.gender === 'FEMALE' || v.gender === 'Woman')) ||
      (voiceFilter === 'MALE' && (v.gender === 'MALE' || v.gender === 'Man')) ||
      (voiceFilter === 'US' && (v.accent === 'AMERICAN' || v.accent === 'US')) ||
      (voiceFilter === 'UK' && (v.accent === 'BRITISH' || v.accent === 'UK'));

    const matchesSearch =
      voiceSearch.trim() === '' ||
      v.name.toLowerCase().includes(voiceSearch.toLowerCase()) ||
      v.code.toLowerCase().includes(voiceSearch.toLowerCase()) ||
      v.description.toLowerCase().includes(voiceSearch.toLowerCase());

    return matchesFilter && matchesSearch;
  });

  const selectedVoice = voices.find((v) => v.id === selectedVoiceId) || voices[0];

  // ========================================================
  // TTS METHODS
  // ========================================================
  const handleSynthesize = async () => {
    if (!ttsText.trim()) {
      toastError('لطفاً متنی برای تبدیل به گفتار وارد کنید.');
      return;
    }

    setIsSynthesizing(true);
    try {
      const result = await api.synthesizeTts({
        text: ttsText.trim(),
        voiceId: selectedVoiceId,
        speed: ttsSpeed,
      });
      setTtsResult(result);
      success(`صدا برای «${result.voiceName}» با موفقیت تولید شد.`);

      // Auto play after synthesis
      setTimeout(() => {
        if (ttsAudioRef.current) {
          ttsAudioRef.current.src = result.audioUrl;
          ttsAudioRef.current.play().catch(() => {});
          setIsPlayingTts(true);
        }
      }, 100);
    } catch (err: any) {
      toastError(err.message || 'خطا در تبدیل متن به گفتار');
    } finally {
      setIsSynthesizing(false);
    }
  };

  const handleQuickPreview = (voice: TtsVoiceInfo, e: React.MouseEvent) => {
    e.stopPropagation();

    if (previewVoiceId === voice.id) {
      // Toggle pause
      if (previewAudioRef.current) {
        previewAudioRef.current.pause();
      }
      setPreviewVoiceId(null);
      return;
    }

    setPreviewVoiceId(voice.id);
    const sampleText = `Hi, I am ${voice.name}. Nice to meet you!`;
    const previewUrl = `/api/v2/tts/speak?text=${encodeURIComponent(sampleText)}&voiceId=${voice.id}&speed=1.0`;

    if (!previewAudioRef.current) {
      previewAudioRef.current = new Audio();
    }
    previewAudioRef.current.src = previewUrl;
    previewAudioRef.current.onended = () => setPreviewVoiceId(null);
    previewAudioRef.current.onerror = () => {
      if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(sampleText);
        utterance.lang = voice.accent === 'BRITISH' || voice.accent === 'UK' ? 'en-GB' : 'en-US';
        utterance.onend = () => setPreviewVoiceId(null);
        utterance.onerror = () => setPreviewVoiceId(null);
        window.speechSynthesis.speak(utterance);
      } else {
        setPreviewVoiceId(null);
        toastError(`خطا در پیش‌شنوایی صدای ${voice.name}`);
      }
    };
    previewAudioRef.current.play().catch(() => {
      if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(sampleText);
        utterance.lang = voice.accent === 'BRITISH' || voice.accent === 'UK' ? 'en-GB' : 'en-US';
        utterance.onend = () => setPreviewVoiceId(null);
        utterance.onerror = () => setPreviewVoiceId(null);
        window.speechSynthesis.speak(utterance);
      } else {
        setPreviewVoiceId(null);
      }
    });
  };

  const toggleTtsPlay = () => {
    if (!ttsAudioRef.current) return;
    if (isPlayingTts) {
      ttsAudioRef.current.pause();
      setIsPlayingTts(false);
    } else {
      ttsAudioRef.current.play().catch(() => {});
      setIsPlayingTts(true);
    }
  };

  const handleCopyLink = () => {
    if (!ttsResult) return;
    const fullUrl = `${window.location.origin}${ttsResult.audioUrl}`;
    navigator.clipboard.writeText(fullUrl);
    success('لینک صوت در کلیپ‌بورد کپی شد.');
  };

  // ========================================================
  // STT METHODS & RECORDING
  // ========================================================
  const startRecordingCleanup = () => {
    if (timerIntervalRef.current) clearInterval(timerIntervalRef.current);
    if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
    if (sttWsRef.current) {
      try {
        sttWsRef.current.close();
      } catch {}
      sttWsRef.current = null;
    }
    if (scriptProcessorRef.current) {
      try {
        scriptProcessorRef.current.disconnect();
      } catch {}
      scriptProcessorRef.current = null;
    }
    if (mediaStreamRef.current) {
      mediaStreamRef.current.getTracks().forEach((t) => t.stop());
      mediaStreamRef.current = null;
    }
    if (audioContextRef.current) {
      try {
        audioContextRef.current.close();
      } catch {}
      audioContextRef.current = null;
    }
    if (browserSpeechRecRef.current) {
      try {
        browserSpeechRecRef.current.stop();
      } catch {}
    }
  };

  const stopRecordingCleanup = () => {
    startRecordingCleanup();
    setIsRecording(false);
    setSttStatus('idle');
  };

  // Canvas Waveform Animation
  const startCanvasWaveform = (analyser: AnalyserNode) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const bufferLength = analyser.frequencyBinCount;
    const dataArray = new Uint8Array(bufferLength);

    const draw = () => {
      animFrameRef.current = requestAnimationFrame(draw);
      analyser.getByteFrequencyData(dataArray);

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      const barWidth = (canvas.width / bufferLength) * 2.5;
      let x = 0;

      for (let i = 0; i < bufferLength; i++) {
        const barHeight = (dataArray[i] / 255) * canvas.height;

        const gradient = ctx.createLinearGradient(0, canvas.height, 0, 0);
        gradient.addColorStop(0, '#6366f1'); // Indigo
        gradient.addColorStop(1, '#a855f7'); // Purple

        ctx.fillStyle = gradient;
        ctx.fillRect(x, canvas.height - barHeight, barWidth - 1, barHeight);
        x += barWidth;
      }
    };

    draw();
  };

  const startRecording = async () => {
    stopRecordingCleanup();
    setLivePartial('');
    setRecordedAudioBlob(null);
    if (recordedAudioUrl) {
      URL.revokeObjectURL(recordedAudioUrl);
      setRecordedAudioUrl(null);
    }
    recordedBuffersRef.current = [];
    setRecordingSeconds(0);

    try {
      setSttStatus('connecting');

      // 1. Get Microphone stream
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          channelCount: 1,
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      });
      mediaStreamRef.current = stream;

      // 2. Setup AudioContext at 16000Hz (Native format required by sherpa-onnx)
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      const audioCtx = new AudioCtx({ sampleRate: 16000 });
      audioContextRef.current = audioCtx;

      const source = audioCtx.createMediaStreamSource(stream);

      // Setup AnalyserNode for visualizer
      const analyser = audioCtx.createAnalyser();
      analyser.fftSize = 64;
      analyserRef.current = analyser;
      source.connect(analyser);
      startCanvasWaveform(analyser);

      // ScriptProcessor for extracting raw PCM samples
      const processor = audioCtx.createScriptProcessor(4096, 1, 1);
      scriptProcessorRef.current = processor;
      source.connect(processor);
      processor.connect(audioCtx.destination);

      // 3. Connect WebSocket if in live mode
      if (sttMode === 'live' || activeTab === 'loopback') {
        const wsUrl = api.getSttWebSocketUrl();
        const ws = new WebSocket(wsUrl);
        sttWsRef.current = ws;

        ws.onopen = () => {
          setSttStatus('listening');
        };

        ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            if (data.type === 'partial') {
              setLivePartial(data.text);
              if (activeTab === 'loopback') {
                setLoopbackText(data.text);
              }
            } else if (data.type === 'final') {
              if (data.text.trim()) {
                setFinalTranscripts((prev) => [...prev, data.text.trim()]);
                setLivePartial('');
                if (activeTab === 'loopback') {
                  setLoopbackText(data.text.trim());
                }
              }
            } else if (data.type === 'error') {
              toastError(`خطای STT: ${data.message}`);
            }
          } catch (e) {
            console.error('WS Parse Error:', e);
          }
        };

        ws.onerror = (e) => {
          console.error('STT WebSocket Error:', e);
          toastError('خطا در اتصال به وب‌سوکت استریم گفتار');
          setSttStatus('idle');
        };

        ws.onclose = () => {
          if (isRecording) {
            setSttStatus('idle');
          }
        };
      } else {
        setSttStatus('listening');
      }

      // Process PCM audio frames
      processor.onaudioprocess = (e) => {
        const inputData = e.inputBuffer.getChannelData(0);
        // Save copy for recorded audio WAV
        recordedBuffersRef.current.push(new Float32Array(inputData));

        // If WebSocket is open and ready, send binary PCM16 frame
        if (sttWsRef.current && sttWsRef.current.readyState === WebSocket.OPEN) {
          const pcm16 = float32ToInt16(inputData);
          sttWsRef.current.send(pcm16.buffer);
        }
      };

      // 4. Also launch browser SpeechRecognition if supported (for comparison)
      const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
      if (SpeechRecognition) {
        try {
          const recognition = new SpeechRecognition();
          recognition.continuous = true;
          recognition.interimResults = true;
          recognition.lang = 'en-US';
          recognition.onresult = (event: any) => {
            let browserText = '';
            for (let i = 0; i < event.results.length; i++) {
              browserText += event.results[i][0].transcript + ' ';
            }
            setBrowserTranscript(browserText.trim());
          };
          recognition.start();
          browserSpeechRecRef.current = recognition;
        } catch {}
      }

      setIsRecording(true);

      // Start duration timer
      timerIntervalRef.current = setInterval(() => {
        setRecordingSeconds((prev) => prev + 1);
      }, 1000);
    } catch (err: any) {
      stopRecordingCleanup();
      toastError(`دسترسی به میکروفون امکان‌پذیر نیست: ${err.message}`);
    }
  };

  const stopRecording = async () => {
    // Flatten recorded samples
    const totalSamples = recordedBuffersRef.current.reduce((acc, curr) => acc + curr.length, 0);
    const merged = new Float32Array(totalSamples);
    let offset = 0;
    for (const buf of recordedBuffersRef.current) {
      merged.set(buf, offset);
      offset += buf.length;
    }

    // Stop streams & sockets
    stopRecordingCleanup();

    // Create WAV Blob
    if (merged.length > 0) {
      const wavBlob = encodeWav(merged, 16000);
      setRecordedAudioBlob(wavBlob);
      const url = URL.createObjectURL(wavBlob);
      setRecordedAudioUrl(url);

      // If in Record & Transcribe mode (batch REST), trigger transcribe
      if (sttMode === 'record') {
        await transcribeRecordedBlob(wavBlob);
      }

      // If in Loopback mode, trigger TTS on recognized text
      if (activeTab === 'loopback' && loopbackText.trim()) {
        triggerLoopbackTts(loopbackText.trim());
      }
    }
  };

  const transcribeRecordedBlob = async (blob: Blob) => {
    setSttStatus('transcribing');
    try {
      const res = await api.transcribeAudio(blob);
      if (res.text.trim()) {
        setFinalTranscripts((prev) => [...prev, res.text.trim()]);
        success('صوت با موفقیت به متن تبدیل شد.');
      } else {
        info('هیچ کلمه‌ای در صوت ارسالی تشخیص داده نشد.');
      }
    } catch (err: any) {
      toastError(err.message || 'خطا در تبدیل فایل صوتی به متن');
    } finally {
      setSttStatus('idle');
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setSttStatus('transcribing');
    try {
      // Decode and resample in browser to 16kHz mono
      const arrayBuffer = await file.arrayBuffer();
      const resampledSamples = await decodeAndResampleTo16k(arrayBuffer);
      const wavBlob = encodeWav(resampledSamples, 16000);

      setRecordedAudioBlob(wavBlob);
      setRecordedAudioUrl(URL.createObjectURL(wavBlob));

      const res = await api.transcribeAudio(wavBlob);
      if (res.text.trim()) {
        setFinalTranscripts((prev) => [...prev, res.text.trim()]);
        success(`فایل ${file.name} با موفقیت به متن تبدیل شد.`);
      } else {
        info('هیچ کلمه‌ای در فایل صوتی شناسایی نشد.');
      }
    } catch (err: any) {
      toastError(err.message || 'خطا در پردازش فایل صوتی');
    } finally {
      setSttStatus('idle');
      e.target.value = '';
    }
  };

  // ========================================================
  // LOOPBACK METHODS
  // ========================================================
  const triggerLoopbackTts = async (textToSpeak: string) => {
    setLoopbackStatus('synthesizing');
    try {
      const res = await api.synthesizeTts({
        text: textToSpeak,
        voiceId: selectedVoiceId,
        speed: ttsSpeed,
      });
      setLoopbackAudioUrl(res.audioUrl);
      setLoopbackStatus('speaking');

      setTimeout(() => {
        if (loopbackAudioRef.current) {
          loopbackAudioRef.current.src = res.audioUrl;
          loopbackAudioRef.current.play().catch(() => {});
          loopbackAudioRef.current.onended = () => setLoopbackStatus('idle');
        }
      }, 100);
    } catch (err: any) {
      setLoopbackStatus('idle');
      toastError(err.message || 'خطا در تولید پاسخ صوتی در تست چرخه‌ای');
    }
  };

  const copyTranscriptionToClipboard = () => {
    const fullText = [...finalTranscripts, livePartial].filter(Boolean).join(' ');
    if (!fullText) return;
    navigator.clipboard.writeText(fullText);
    setCopiedText(true);
    setTimeout(() => setCopiedText(false), 2000);
    success('متن گفتار کپی شد.');
  };

  const clearTranscripts = () => {
    setFinalTranscripts([]);
    setLivePartial('');
    setBrowserTranscript('');
  };

  const formatTimer = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 border-b border-slate-200 dark:border-slate-800 pb-5">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="p-2 rounded-xl bg-indigo-50 dark:bg-indigo-500/15 text-indigo-600 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-500/30">
              <AudioWaveform className="w-5 h-5" />
            </span>
            <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100">
              آزمایشگاه صوت و هوش مصنوعی (Voice Lab)
            </h1>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            تست و اعتبارسنجی آنلاین موتورهای صوتی Kokoro-82M (تبدیل متن به صوت) و Sherpa-ONNX Zipformer (تبدیل صوت به متن)
          </p>
        </div>

        {/* Status badges */}
        <div className="flex flex-wrap items-center gap-2">
          <Badge variant="purple" size="md">
            <Volume2 className="w-3.5 h-3.5" />
            Kokoro TTS (11 صدا)
          </Badge>
          <Badge variant="success" size="md">
            <Mic className="w-3.5 h-3.5" />
            Sherpa STT 16kHz
          </Badge>
          <Badge variant="info" size="md">
            <Activity className="w-3.5 h-3.5" />
            پورت 8080 Ktor
          </Badge>
        </div>
      </div>

      {/* Modern Tab Switcher */}
      <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-800/80 pb-2">
        <div className="flex items-center gap-2 p-1 rounded-2xl bg-slate-100 dark:bg-[#0E1422] border border-slate-200 dark:border-slate-800">
          <button
            onClick={() => setActiveTab('tts')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all duration-150 ${
              activeTab === 'tts'
                ? 'bg-white dark:bg-indigo-600 text-indigo-700 dark:text-white shadow-xs border border-slate-200 dark:border-transparent'
                : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <Volume2 className="w-4 h-4" />
            <span>تست تبدیل متن به گفتار (TTS)</span>
          </button>

          <button
            onClick={() => setActiveTab('stt')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all duration-150 ${
              activeTab === 'stt'
                ? 'bg-white dark:bg-indigo-600 text-indigo-700 dark:text-white shadow-xs border border-slate-200 dark:border-transparent'
                : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <Mic className="w-4 h-4" />
            <span>تست تبدیل گفتار به متن (STT)</span>
          </button>

          <button
            onClick={() => setActiveTab('loopback')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all duration-150 ${
              activeTab === 'loopback'
                ? 'bg-white dark:bg-indigo-600 text-indigo-700 dark:text-white shadow-xs border border-slate-200 dark:border-transparent'
                : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <ArrowRightLeft className="w-4 h-4" />
            <span>تست رفت و برگشت (Loopback)</span>
          </button>
        </div>
      </div>

      {/* ========================================================
          TAB 1: TEXT-TO-SPEECH (TTS)
          ======================================================== */}
      {activeTab === 'tts' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Left Column: Voice Explorer Catalog */}
          <div className="lg:col-span-5 space-y-4">
            <div className="bg-white dark:bg-[#0E1422] rounded-2xl border border-slate-200 dark:border-slate-800/80 p-4 shadow-xs">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <Volume2 className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                  <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                    انتخاب صدای گوینده
                  </h3>
                </div>
                <span className="text-xs text-slate-500 font-medium">
                  {filteredVoices.length} از {voices.length} صدا
                </span>
              </div>

              {/* Filters */}
              <div className="space-y-2 mb-3">
                <input
                  type="text"
                  placeholder="جستجوی صدا (نام، لهجه، ویژگی)..."
                  value={voiceSearch}
                  onChange={(e) => setVoiceSearch(e.target.value)}
                  className="w-full text-xs rounded-xl bg-slate-50 dark:bg-[#111726] border border-slate-200 dark:border-slate-800 px-3 py-2 text-slate-800 dark:text-slate-200 focus:outline-none focus:border-indigo-500"
                />

                <div className="flex flex-wrap items-center gap-1.5">
                  {(['ALL', 'FEMALE', 'MALE', 'US', 'UK'] as const).map((filter) => {
                    const label =
                      filter === 'ALL'
                        ? 'همه'
                        : filter === 'FEMALE'
                        ? '👩 زن'
                        : filter === 'MALE'
                        ? '👨 مرد'
                        : filter === 'US'
                        ? '🇺🇸 آمریکا'
                        : '🇬🇧 بریتانیا';
                    return (
                      <button
                        key={filter}
                        onClick={() => setVoiceFilter(filter)}
                        className={`text-xs px-2.5 py-1 rounded-lg transition-all ${
                          voiceFilter === filter
                            ? 'bg-indigo-600 text-white font-medium'
                            : 'bg-slate-100 dark:bg-slate-800/60 text-slate-600 dark:text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-800'
                        }`}
                      >
                        {label}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Voices List */}
              <div className="space-y-2 max-h-[500px] overflow-y-auto pr-1">
                {filteredVoices.map((voice) => {
                  const isSelected = selectedVoiceId === voice.id;
                  const isPreviewing = previewVoiceId === voice.id;

                  return (
                    <div
                      key={voice.id}
                      onClick={() => setSelectedVoiceId(voice.id)}
                      className={`p-3 rounded-xl border transition-all duration-150 cursor-pointer flex items-center justify-between gap-3 ${
                        isSelected
                          ? 'bg-indigo-50/70 dark:bg-indigo-500/10 border-indigo-500 shadow-xs'
                          : 'bg-white dark:bg-[#111726]/60 border-slate-200 dark:border-slate-800/80 hover:border-slate-300 dark:hover:border-slate-700'
                      }`}
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div
                          className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${
                            isSelected
                              ? 'bg-indigo-600 text-white'
                              : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                          }`}
                        >
                          <Volume2 className="w-4 h-4" />
                        </div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-2">
                            <span className="text-xs font-bold text-slate-900 dark:text-slate-100 truncate">
                              {voice.name}
                            </span>
                            <span className="text-[10px] font-mono text-slate-400">
                              (ID: {voice.id})
                            </span>
                          </div>
                          <p className="text-[11px] text-slate-500 dark:text-slate-400 truncate">
                            {voice.description}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 shrink-0">
                        <Badge
                          variant={
                            voice.accent === 'AMERICAN' || voice.accent === 'US'
                              ? 'info'
                              : 'purple'
                          }
                          size="sm"
                        >
                          {voice.accent === 'AMERICAN' || voice.accent === 'US'
                            ? '🇺🇸 US'
                            : '🇬🇧 UK'}
                        </Badge>

                        {/* Quick Listen Button */}
                        <button
                          type="button"
                          onClick={(e) => handleQuickPreview(voice, e)}
                          title="پیش‌شنوایی سریع صدا"
                          className={`p-1.5 rounded-lg border transition-colors ${
                            isPreviewing
                              ? 'bg-amber-500 text-white border-amber-600'
                              : 'bg-slate-50 hover:bg-slate-100 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 border-slate-200 dark:border-slate-700'
                          }`}
                        >
                          {isPreviewing ? (
                            <Pause className="w-3.5 h-3.5" />
                          ) : (
                            <Play className="w-3.5 h-3.5" />
                          )}
                        </button>

                        {isSelected && (
                          <div className="w-5 h-5 rounded-full bg-indigo-600 text-white flex items-center justify-center">
                            <Check className="w-3 h-3" />
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Right Column: Synthesis Studio & Audio Player */}
          <div className="lg:col-span-7 space-y-5">
            {/* Active Voice Card */}
            {selectedVoice && (
              <div className="bg-gradient-to-r from-indigo-500/10 via-purple-500/5 to-transparent p-4 rounded-2xl border border-indigo-200 dark:border-indigo-500/30 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-11 h-11 rounded-2xl bg-indigo-600 text-white flex items-center justify-center shadow-sm">
                    <Sparkles className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-slate-900 dark:text-slate-100">
                        {selectedVoice.name}
                      </span>
                      <span className="text-xs font-mono text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-500/20 px-2 py-0.5 rounded">
                        کد: {selectedVoice.code}
                      </span>
                    </div>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                      {selectedVoice.description}
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-1.5">
                  <Badge variant="purple" size="md">
                    {selectedVoice.gender === 'FEMALE' || selectedVoice.gender === 'Woman'
                      ? 'زن'
                      : 'مرد'}
                  </Badge>
                  <Badge variant="info" size="md">
                    {selectedVoice.accent === 'AMERICAN' || selectedVoice.accent === 'US'
                      ? 'لهجه آمریکایی'
                      : 'لهجه بریتانیایی'}
                  </Badge>
                </div>
              </div>
            )}

            {/* Input Studio */}
            <div className="bg-white dark:bg-[#0E1422] rounded-2xl border border-slate-200 dark:border-slate-800/80 p-5 shadow-xs space-y-4">
              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                    متن انگلیسی جهت تولید گفتار (Synthesis Text)
                  </label>
                  <span className="text-[11px] text-slate-400 font-mono">
                    {ttsText.length} کاراکتر
                  </span>
                </div>
                <textarea
                  rows={4}
                  value={ttsText}
                  onChange={(e) => setTtsText(e.target.value)}
                  placeholder="Enter English text to synthesize..."
                  className="w-full rounded-xl bg-slate-50 dark:bg-[#111726] border border-slate-300 dark:border-slate-800 p-3.5 text-sm text-slate-900 dark:text-slate-100 focus:outline-none focus:border-indigo-600 focus:ring-1 focus:ring-indigo-500"
                  dir="ltr"
                />
              </div>

              {/* Preset Sample Prompts */}
              <div>
                <span className="text-xs font-medium text-slate-500 dark:text-slate-400 block mb-1.5">
                  جملات نمونه پیشنهادی برای تست سریع:
                </span>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {TTS_SAMPLE_PHRASES.map((sample, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => setTtsText(sample.text)}
                      className="text-right p-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-[#111726]/60 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors group"
                    >
                      <span className="text-[11px] font-semibold text-indigo-600 dark:text-indigo-400 block mb-0.5">
                        {sample.category}
                      </span>
                      <p className="text-xs text-slate-600 dark:text-slate-300 line-clamp-1" dir="ltr">
                        {sample.text}
                      </p>
                    </button>
                  ))}
                </div>
              </div>

              {/* Speed Slider */}
              <div className="pt-2 border-t border-slate-100 dark:border-slate-800/80">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Sliders className="w-4 h-4 text-slate-500" />
                    <span className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                      سرعت پخش صدا (Speed)
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-mono font-bold text-indigo-600 dark:text-indigo-400">
                      {ttsSpeed.toFixed(2)}x
                    </span>
                    {ttsSpeed !== 1.0 && (
                      <button
                        type="button"
                        onClick={() => setTtsSpeed(1.0)}
                        className="text-[11px] text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                      >
                        (تنظیم مجدد ۱.۰x)
                      </button>
                    )}
                  </div>
                </div>
                <input
                  type="range"
                  min="0.5"
                  max="2.0"
                  step="0.05"
                  value={ttsSpeed}
                  onChange={(e) => setTtsSpeed(parseFloat(e.target.value))}
                  className="w-full accent-indigo-600 cursor-pointer"
                />
              </div>

              {/* Generate Button */}
              <div className="flex items-center justify-end gap-3 pt-2">
                <Button
                  variant="primary"
                  size="md"
                  onClick={handleSynthesize}
                  isLoading={isSynthesizing}
                  icon={<Sparkles className="w-4 h-4" />}
                >
                  تولید گفتار با مدل Kokoro-82M
                </Button>
              </div>
            </div>

            {/* Generated Audio Player Result Card */}
            {ttsResult && (
              <div className="bg-white dark:bg-[#0E1422] rounded-2xl border border-indigo-200 dark:border-indigo-500/30 p-5 shadow-xs space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
                    <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                      خروجی صوتی تولید شده
                    </h3>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={handleCopyLink}
                      className="text-xs text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400 flex items-center gap-1 p-1.5 rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800"
                      title="کپی لینک مستقیم فایل صوت"
                    >
                      <Copy className="w-3.5 h-3.5" />
                      کپی لینک
                    </button>
                    <a
                      href={ttsResult.audioUrl}
                      download={`kokoro_${selectedVoice?.code || 'voice'}.wav`}
                      className="text-xs text-white bg-indigo-600 hover:bg-indigo-700 flex items-center gap-1 px-2.5 py-1.5 rounded-lg font-medium shadow-xs"
                    >
                      <Download className="w-3.5 h-3.5" />
                      دانلود WAV
                    </a>
                  </div>
                </div>

                {/* Custom Audio Player UI */}
                <div className="p-4 rounded-xl bg-slate-50 dark:bg-[#111726] border border-slate-200 dark:border-slate-800/80 flex flex-col gap-3">
                  <div className="flex items-center gap-4">
                    <button
                      onClick={toggleTtsPlay}
                      className="w-12 h-12 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white flex items-center justify-center shrink-0 shadow-md shadow-indigo-500/20 transition-transform active:scale-95"
                    >
                      {isPlayingTts ? (
                        <Pause className="w-5 h-5" />
                      ) : (
                        <Play className="w-5 h-5 ml-0.5" />
                      )}
                    </button>

                    <div className="flex-1 space-y-1">
                      <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 font-mono">
                        <span>{ttsCurrentTime.toFixed(1)}s</span>
                        <span>{ttsDuration.toFixed(1)}s</span>
                      </div>
                      <input
                        type="range"
                        min="0"
                        max={ttsDuration || 100}
                        step="0.1"
                        value={ttsCurrentTime}
                        onChange={(e) => {
                          const time = parseFloat(e.target.value);
                          setTtsCurrentTime(time);
                          if (ttsAudioRef.current) {
                            ttsAudioRef.current.currentTime = time;
                          }
                        }}
                        className="w-full accent-indigo-600 cursor-pointer h-1.5 bg-slate-200 dark:bg-slate-700 rounded-lg"
                      />
                    </div>
                  </div>

                  {/* Hidden HTML audio element */}
                  <audio
                    ref={ttsAudioRef}
                    onTimeUpdate={() => {
                      if (ttsAudioRef.current) {
                        setTtsCurrentTime(ttsAudioRef.current.currentTime);
                      }
                    }}
                    onLoadedMetadata={() => {
                      if (ttsAudioRef.current) {
                        setTtsDuration(ttsAudioRef.current.duration);
                      }
                    }}
                    onEnded={() => setIsPlayingTts(false)}
                  />
                </div>

                {/* Technical Telemetry */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
                  <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-[#111726]/70 border border-slate-200 dark:border-slate-800">
                    <span className="text-[10px] text-slate-400 block">مدت زمان صوت</span>
                    <span className="text-xs font-bold font-mono text-slate-800 dark:text-slate-200">
                      {ttsResult.durationMs.toLocaleString()} ms
                    </span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-[#111726]/70 border border-slate-200 dark:border-slate-800">
                    <span className="text-[10px] text-slate-400 block">نرخ نمونه‌برداری</span>
                    <span className="text-xs font-bold font-mono text-indigo-600 dark:text-indigo-400">
                      {ttsResult.sampleRate.toLocaleString()} Hz (24kHz)
                    </span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-[#111726]/70 border border-slate-200 dark:border-slate-800">
                    <span className="text-[10px] text-slate-400 block">شناسه و نام صدا</span>
                    <span className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate block">
                      {ttsResult.voiceName} ({ttsResult.voiceId})
                    </span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-[#111726]/70 border border-slate-200 dark:border-slate-800">
                    <span className="text-[10px] text-slate-400 block">قالب خروجی</span>
                    <span className="text-xs font-bold font-mono text-emerald-600 dark:text-emerald-400">
                      16-bit Mono WAV
                    </span>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ========================================================
          TAB 2: SPEECH-TO-TEXT (STT)
          ======================================================== */}
      {activeTab === 'stt' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Left Column: Recording Controls & Visualizer */}
          <div className="lg:col-span-5 space-y-4">
            <div className="bg-white dark:bg-[#0E1422] rounded-2xl border border-slate-200 dark:border-slate-800/80 p-5 shadow-xs space-y-5">
              {/* STT Mode Selection */}
              <div>
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-2">
                  حالت آزمون تبدیل گفتار (STT Mode)
                </label>
                <div className="grid grid-cols-3 gap-1.5 p-1 rounded-xl bg-slate-100 dark:bg-[#111726] border border-slate-200 dark:border-slate-800">
                  <button
                    onClick={() => {
                      if (isRecording) stopRecording();
                      setSttMode('live');
                    }}
                    className={`py-2 text-xs font-semibold rounded-lg transition-all ${
                      sttMode === 'live'
                        ? 'bg-white dark:bg-indigo-600 text-indigo-700 dark:text-white shadow-xs'
                        : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                    }`}
                  >
                    استریم زنده
                  </button>
                  <button
                    onClick={() => {
                      if (isRecording) stopRecording();
                      setSttMode('record');
                    }}
                    className={`py-2 text-xs font-semibold rounded-lg transition-all ${
                      sttMode === 'record'
                        ? 'bg-white dark:bg-indigo-600 text-indigo-700 dark:text-white shadow-xs'
                        : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                    }`}
                  >
                    ضبط و ارسال
                  </button>
                  <button
                    onClick={() => {
                      if (isRecording) stopRecording();
                      setSttMode('upload');
                    }}
                    className={`py-2 text-xs font-semibold rounded-lg transition-all ${
                      sttMode === 'upload'
                        ? 'bg-white dark:bg-indigo-600 text-indigo-700 dark:text-white shadow-xs'
                        : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                    }`}
                  >
                    آپلود فایل
                  </button>
                </div>
              </div>

              {/* Live Audio Visualizer Canvas */}
              <div className="h-28 rounded-2xl bg-slate-900 flex flex-col items-center justify-center relative overflow-hidden border border-slate-800">
                <canvas
                  ref={canvasRef}
                  width={300}
                  height={112}
                  className="w-full h-full object-cover"
                />
                {!isRecording && (
                  <div className="absolute inset-0 flex flex-col items-center justify-center text-slate-500 text-xs gap-1.5 pointer-events-none">
                    <Radio className="w-5 h-5 text-slate-600" />
                    <span>برای مشاهده نوسان صوت، ضبط را آغاز کنید</span>
                  </div>
                )}
              </div>

              {/* Record Action Panel */}
              {sttMode !== 'upload' ? (
                <div className="flex flex-col items-center justify-center py-4 space-y-4">
                  {/* Big Record Button */}
                  <button
                    onClick={isRecording ? stopRecording : startRecording}
                    className={`w-20 h-20 rounded-full flex items-center justify-center transition-all duration-300 shadow-lg ${
                      isRecording
                        ? 'bg-rose-500 hover:bg-rose-600 text-white animate-pulse ring-8 ring-rose-500/20'
                        : 'bg-indigo-600 hover:bg-indigo-700 text-white hover:scale-105 shadow-indigo-500/30'
                    }`}
                  >
                    {isRecording ? (
                      <MicOff className="w-8 h-8" />
                    ) : (
                      <Mic className="w-8 h-8" />
                    )}
                  </button>

                  {/* Status & Timer */}
                  <div className="text-center space-y-1.5">
                    <div className="text-base font-mono font-bold text-slate-800 dark:text-slate-200">
                      {formatTimer(recordingSeconds)}
                    </div>
                    <div className="flex items-center justify-center gap-1.5">
                      <Badge
                        variant={
                          sttStatus === 'listening'
                            ? 'danger'
                            : sttStatus === 'connecting'
                            ? 'warning'
                            : sttStatus === 'transcribing'
                            ? 'info'
                            : 'neutral'
                        }
                        size="sm"
                        pulse={sttStatus === 'listening'}
                      >
                        {sttStatus === 'listening'
                          ? 'در حال گوش دادن...'
                          : sttStatus === 'connecting'
                          ? 'در حال برقراری اتصال...'
                          : sttStatus === 'transcribing'
                          ? 'در حال پردازش گفتار...'
                          : 'آماده ضبط'}
                      </Badge>
                    </div>
                    <span className="text-xs text-slate-500 dark:text-slate-400 block">
                      {isRecording
                        ? 'انگلیسی صحبت کنید تا کلمات در لحظه ظاهر شوند'
                        : 'برای صحبت کردن روی میکروفون کلیک کنید'}
                    </span>
                  </div>
                </div>
              ) : (
                /* File Upload Zone */
                <div className="border-2 border-dashed border-slate-300 dark:border-slate-800 rounded-2xl p-6 text-center hover:border-indigo-500 transition-colors">
                  <input
                    type="file"
                    id="audio-upload-input"
                    accept="audio/*"
                    onChange={handleFileUpload}
                    className="hidden"
                  />
                  <label
                    htmlFor="audio-upload-input"
                    className="cursor-pointer flex flex-col items-center justify-center gap-2"
                  >
                    <div className="w-12 h-12 rounded-xl bg-indigo-50 dark:bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 flex items-center justify-center">
                      <Upload className="w-6 h-6" />
                    </div>
                    <span className="text-xs font-bold text-slate-700 dark:text-slate-300">
                      انتخاب یا رها کردن فایل صوتی
                    </span>
                    <span className="text-[11px] text-slate-400">
                      فرمت‌های WAV، MP3، WebM یا M4A (تغییر نمونه‌برداری به 16kHz خودکار انجام می‌شود)
                    </span>
                  </label>
                </div>
              )}

              {/* Recorded Audio Preview */}
              {recordedAudioUrl && (
                <div className="p-3 rounded-xl bg-slate-50 dark:bg-[#111726] border border-slate-200 dark:border-slate-800 space-y-1.5">
                  <div className="flex items-center justify-between text-[11px] font-semibold text-slate-600 dark:text-slate-400">
                    <span>پیش‌نمایش صدای ضبط شده شما:</span>
                    {recordedAudioBlob && (
                      <span className="font-mono text-slate-400">
                        {(recordedAudioBlob.size / 1024).toFixed(1)} KB (WAV)
                      </span>
                    )}
                  </div>
                  <audio controls src={recordedAudioUrl} className="w-full h-8" />
                </div>
              )}

              {/* Engine Specs */}
              <div className="p-3 rounded-xl bg-slate-50 dark:bg-[#111726]/60 border border-slate-200 dark:border-slate-800/80 text-xs space-y-1 text-slate-500 dark:text-slate-400">
                <div className="flex items-center justify-between">
                  <span>مدل پردازش:</span>
                  <span className="font-mono text-slate-700 dark:text-slate-300">streaming-zipformer-en</span>
                </div>
                <div className="flex items-center justify-between">
                  <span>نرخ ورودی:</span>
                  <span className="font-mono text-indigo-600 dark:text-indigo-400">16,000 Hz Mono PCM</span>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column: Transcription Output & Telemetry */}
          <div className="lg:col-span-7 space-y-4">
            <div className="bg-white dark:bg-[#0E1422] rounded-2xl border border-slate-200 dark:border-slate-800/80 p-5 shadow-xs space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className={`w-2.5 h-2.5 rounded-full ${isRecording ? 'bg-emerald-500 animate-ping' : 'bg-slate-400'}`} />
                  <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                    متن پیاده‌سازی شده (Transcript)
                  </h3>
                </div>

                <div className="flex items-center gap-2">
                  {copiedText ? (
                    <Badge variant="success" size="sm">
                      <Check className="w-3 h-3" />
                      کپی شد
                    </Badge>
                  ) : (
                    <button
                      onClick={copyTranscriptionToClipboard}
                      disabled={finalTranscripts.length === 0 && !livePartial}
                      className="text-xs text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400 flex items-center gap-1 p-1.5 rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40"
                    >
                      <Copy className="w-3.5 h-3.5" />
                      کپی متن
                    </button>
                  )}

                  <button
                    onClick={clearTranscripts}
                    className="text-xs text-rose-500 hover:text-rose-600 flex items-center gap-1 p-1.5 rounded-lg border border-rose-200 dark:border-rose-900/40 hover:bg-rose-50 dark:hover:bg-rose-950/20"
                    title="پاک کردن متن"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Transcript Display Box */}
              <div
                className="min-h-[220px] max-h-[360px] overflow-y-auto p-4 rounded-xl bg-slate-50 dark:bg-[#111726] border border-slate-200 dark:border-slate-800 text-sm leading-relaxed"
                dir="ltr"
              >
                {finalTranscripts.length === 0 && !livePartial ? (
                  <div className="h-full flex flex-col items-center justify-center text-slate-400 dark:text-slate-500 text-xs py-12 gap-2">
                    <Mic className="w-8 h-8 opacity-40" />
                    <span>متن حاصل از تبدیل گفتار به صورت زنده در اینجا نمایان خواهد شد.</span>
                    <span className="text-[11px] text-slate-400">
                      (برای بهترین نتیجه، با لهجه واضح انگلیسی صحبت کنید)
                    </span>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {finalTranscripts.map((text, idx) => (
                      <p key={idx} className="text-slate-800 dark:text-slate-100 font-medium">
                        {text}
                      </p>
                    ))}
                    {livePartial && (
                      <p className="text-indigo-600 dark:text-indigo-400 italic font-medium flex items-center gap-1">
                        <span>{livePartial}</span>
                        <span className="inline-block w-1.5 h-3.5 bg-indigo-500 animate-pulse" />
                      </p>
                    )}
                  </div>
                )}
              </div>

              {/* Browser Native Speech Recognition comparison (if available) */}
              {browserTranscript && (
                <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-xs space-y-1">
                  <div className="flex items-center justify-between text-amber-700 dark:text-amber-300 font-semibold">
                    <span>تشخیص موازی مرورگر (Web Speech API):</span>
                    <span className="text-[10px]">(جهت مقایسه دقت)</span>
                  </div>
                  <p className="text-slate-700 dark:text-slate-300 font-mono" dir="ltr">
                    {browserTranscript}
                  </p>
                </div>
              )}

              {/* Quick stats footer */}
              <div className="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-100 dark:border-slate-800/80">
                <span>
                  تعداد جملات ثبت شده: {finalTranscripts.length}
                </span>
                <span>
                  تعداد کلمات کل: {finalTranscripts.join(' ').split(/\s+/).filter(Boolean).length}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================
          TAB 3: FULL LOOPBACK SIMULATOR
          ======================================================== */}
      {activeTab === 'loopback' && (
        <div className="bg-white dark:bg-[#0E1422] rounded-2xl border border-slate-200 dark:border-slate-800/80 p-6 shadow-xs space-y-6">
          <div className="max-w-2xl mx-auto text-center space-y-2">
            <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-500/15 text-indigo-600 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-500/30 flex items-center justify-center mx-auto">
              <ArrowRightLeft className="w-6 h-6" />
            </div>
            <h2 className="text-base font-bold text-slate-900 dark:text-slate-100">
              تست چرخه‌ای و شبیه‌ساز مکالمه (Loopback Simulation)
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              با میکروفون صحبت کنید؛ مدل STT صدای شما را می‌شنود و سپس مدل TTS فوراً همان کلمات را با صدای انتخابی شما بازگو می‌کند!
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 max-w-4xl mx-auto">
            {/* Step 1: Speak */}
            <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-[#111726]/60 flex flex-col items-center text-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-indigo-600 text-white flex items-center justify-center font-bold">
                ۱
              </div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-slate-100">
                صحبت در میکروفون (STT)
              </h4>
              <p className="text-[11px] text-slate-500">
                با کلیک روی دکمه شروع کنید و جمله‌ای انگلیسی بگویید.
              </p>
              <button
                onClick={isRecording ? stopRecording : startRecording}
                className={`px-4 py-2 rounded-xl text-xs font-bold text-white transition-all shadow-sm ${
                  isRecording ? 'bg-rose-500 hover:bg-rose-600 animate-pulse' : 'bg-indigo-600 hover:bg-indigo-700'
                }`}
              >
                {isRecording ? `توقف ضبط (${recordingSeconds}s)` : 'شروع صحبت'}
              </button>
            </div>

            {/* Step 2: Transcribe */}
            <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-[#111726]/60 flex flex-col items-center text-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-purple-600 text-white flex items-center justify-center font-bold">
                ۲
              </div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-slate-100">
                تشخیص کلمات شما
              </h4>
              <div className="w-full min-h-[50px] p-2 rounded-lg bg-white dark:bg-[#0E1422] border border-slate-200 dark:border-slate-800 text-xs font-mono text-slate-800 dark:text-slate-200 flex items-center justify-center text-center" dir="ltr">
                {loopbackText || '— هنوز متنی دریافت نشده —'}
              </div>
            </div>

            {/* Step 3: Speak Back */}
            <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-[#111726]/60 flex flex-col items-center text-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-600 text-white flex items-center justify-center font-bold">
                ۳
              </div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-slate-100">
                پاسخ صوتی گوینده (TTS)
              </h4>
              <span className="text-[11px] text-slate-500">
                با صدای: {selectedVoice?.name}
              </span>
              {loopbackAudioUrl ? (
                <button
                  onClick={() => {
                    if (loopbackAudioRef.current) {
                      loopbackAudioRef.current.currentTime = 0;
                      loopbackAudioRef.current.play().catch(() => {});
                    }
                  }}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 flex items-center gap-1.5 shadow-sm"
                >
                  <Play className="w-3.5 h-3.5" />
                  پخش مجدد صدای هوش مصنوعی
                </button>
              ) : (
                <span className="text-[11px] text-slate-400">
                  {loopbackStatus === 'synthesizing' ? 'در حال تبدیل به صوت...' : 'منتظر اتمام صحبت...'}
                </span>
              )}
              <audio ref={loopbackAudioRef} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
