import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Play, Pause, RotateCcw, ArrowRight, Volume2, VolumeX, Sparkles } from 'lucide-react';
import { CupertinoNavBar, CupertinoButton } from '../components/cupertino';

export const IntroVideoPage: React.FC = () => {
  const navigate = useNavigate();
  const videoRef = useRef<HTMLVideoElement>(null);

  const [isPlaying, setIsPlaying] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [progress, setProgress] = useState(0);
  const [isEnded, setIsEnded] = useState(false);

  const videoUrl = '/resources/video/intro.mp4?v=2';

  const togglePlay = () => {
    if (!videoRef.current) return;
    if (isPlaying) {
      videoRef.current.pause();
      setIsPlaying(false);
    } else {
      videoRef.current.play();
      setIsPlaying(true);
      setIsEnded(false);
    }
  };

  const handleTimeUpdate = () => {
    if (!videoRef.current) return;
    const current = videoRef.current.currentTime;
    const duration = videoRef.current.duration || 1;
    setProgress((current / duration) * 100);
  };

  const handleVideoEnded = () => {
    setIsPlaying(false);
    setIsEnded(true);
  };

  const handleSkipOrProceed = () => {
    navigate('/');
  };

  return (
    <div className="flex flex-col min-h-screen bg-[#06070a] text-white selection:bg-apple-blue selection:text-white">
      {/* 1. Cupertino Top App Bar */}
      <CupertinoNavBar
        title="معرفی هوش مصنوعی مکالمه"
        subtitle="راهنمای شروع یادگیری و سناریوها"
        onBack={() => navigate('/')}
        endSlot={
          <button
            type="button"
            onClick={handleSkipOrProceed}
            className="px-3 py-1.5 rounded-full bg-white/10 hover:bg-white/15 text-xs text-apple-gray-300 font-semibold active:scale-95 transition-all"
          >
            رد کردن
          </button>
        }
      />

      {/* 2. Main Video Player Screen */}
      <main className="flex-1 max-w-lg w-full mx-auto px-4 py-4 flex flex-col justify-center space-y-4">
        {/* Video Frame Card */}
        <div className="relative rounded-3xl overflow-hidden apple-glass border border-white/20 bg-black aspect-video shadow-apple-lg group flex items-center justify-center">
          <video
            ref={videoRef}
            src={videoUrl}
            playsInline
            muted={isMuted}
            onTimeUpdate={handleTimeUpdate}
            onEnded={handleVideoEnded}
            onClick={togglePlay}
            className="w-full h-full object-cover cursor-pointer"
          />

          {/* Big Center Play Overlay Button */}
          {!isPlaying && !isEnded && (
            <button
              type="button"
              onClick={togglePlay}
              className="absolute inset-0 m-auto w-16 h-16 rounded-full bg-apple-blue/90 text-white flex items-center justify-center shadow-[0_0_24px_rgba(0,122,255,0.6)] hover:scale-105 active:scale-95 transition-all z-10"
            >
              <Play className="w-7 h-7 fill-white translate-x-0.5" />
            </button>
          )}

          {/* Video Finished Replay Overlay */}
          {isEnded && (
            <div className="absolute inset-0 bg-black/70 backdrop-blur-md flex flex-col items-center justify-center space-y-3 z-10 p-4 text-center">
              <Sparkles className="w-10 h-10 text-apple-yellow animate-bounce" />
              <h3 className="text-base font-bold text-white">ویدیو به پایان رسید!</h3>
              <p className="text-xs text-apple-gray-300">
                اکنون آماده‌اید تا اولین مکالمه خود را با هوش مصنوعی در فرودگاه آغاز کنید.
              </p>
              <div className="flex items-center gap-2 pt-2">
                <CupertinoButton
                  variant="filled"
                  size="md"
                  onClick={handleSkipOrProceed}
                  className="bg-apple-blue shadow-apple-md"
                >
                  <span>شروع مراحل مکالمه</span>
                  <ArrowRight className="w-4 h-4 rotate-180 mr-1" />
                </CupertinoButton>
                <CupertinoButton
                  variant="glass"
                  size="md"
                  onClick={() => {
                    if (videoRef.current) {
                      videoRef.current.currentTime = 0;
                      videoRef.current.play();
                      setIsPlaying(true);
                      setIsEnded(false);
                    }
                  }}
                >
                  <RotateCcw className="w-4 h-4 ml-1" />
                  <span>پخش مجدد</span>
                </CupertinoButton>
              </div>
            </div>
          )}

          {/* Bottom Video Progress Scrub Bar */}
          <div className="absolute bottom-0 inset-x-0 h-1 bg-white/20">
            <div
              className="h-full bg-apple-blue transition-all duration-150"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>

        {/* Video Controls Bar */}
        <div className="p-3.5 rounded-3xl apple-glass bg-white/5 border border-white/10 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={togglePlay}
              className="p-2.5 rounded-2xl bg-white/10 hover:bg-white/15 text-white active:scale-95 transition-all"
            >
              {isPlaying ? <Pause className="w-5 h-5" /> : <Play className="w-5 h-5 fill-white" />}
            </button>

            <button
              type="button"
              onClick={() => setIsMuted((prev) => !prev)}
              className="p-2.5 rounded-2xl bg-white/10 hover:bg-white/15 text-white active:scale-95 transition-all"
            >
              {isMuted ? <VolumeX className="w-5 h-5 text-apple-red" /> : <Volume2 className="w-5 h-5" />}
            </button>
          </div>

          <button
            type="button"
            onClick={handleSkipOrProceed}
            className="flex items-center gap-1.5 text-xs font-bold text-apple-blue px-3 py-2 rounded-2xl hover:bg-white/10 transition-all"
          >
            <span>شروع تمرین مکالمه</span>
            <ArrowRight className="w-4 h-4 rotate-180" />
          </button>
        </div>

        {/* Description Highlights */}
        <div className="p-5 rounded-3xl apple-glass bg-white/5 border border-white/10 space-y-3">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-apple-yellow" />
            <span>نحوه یادگیری و انجام تمرین‌ها</span>
          </h3>
          <ul className="text-xs text-apple-gray-300 space-y-2 leading-relaxed">
            <li className="flex items-start gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-apple-blue mt-1.5 shrink-0" />
              <span>
                در هر مرحله با یک شخصیت بومی در سناریویی واقعی (فرودگاه، هتل، کافه و دانشگاه) مکالمه
                می‌کنید.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-apple-green mt-1.5 shrink-0" />
              <span>
                می‌توانید با میکروفون صحبت کنید یا تایپ کنید؛ هوش مصنوعی پاسخ صوتی و متنی می‌دهد.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-apple-yellow mt-1.5 shrink-0" />
              <span>
                در صورت وجود اشتباه گرامری، بازخورد و اصلاح بلافاصله نمایش داده می‌شود.
              </span>
            </li>
          </ul>

          <div className="pt-2">
            <CupertinoButton
              variant="filled"
              size="lg"
              fullWidth
              onClick={handleSkipOrProceed}
              className="bg-apple-blue shadow-apple-md"
            >
              <span>ورود به فهرست مراحل</span>
              <ArrowRight className="w-4 h-4 rotate-180 mr-1.5" />
            </CupertinoButton>
          </div>
        </div>
      </main>
    </div>
  );
};

export default IntroVideoPage;
