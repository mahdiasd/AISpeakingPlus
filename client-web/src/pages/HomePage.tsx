import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import confetti from 'canvas-confetti';
import {
  Crown,
  Star,
  Play,
  Layers,
  User,
  Sparkles,
  Lock,
  RotateCcw,
  Compass,
  CheckCircle2,
} from 'lucide-react';
import {
  CupertinoModal,
  CupertinoStarRating,
  CupertinoSkeleton,
  CupertinoPill,
} from '../components/cupertino';
import { StageBriefingModal } from '../components/home/StageBriefingModal';
import { StagesTimeline } from '../components/home/StagesTimeline';
import { LoginDialog } from '../components/auth/LoginDialog';
import { useStages } from '../hooks/useStages';
import { useAuth } from '../hooks/useAuth';
import { Stage } from '../types/stage';
import { getAvatarUrl } from '../utils/avatar';
import LiquidGlass from 'liquid-glass-react';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const { stages, currentStage, phaseGroups, stats, isLoading, refreshStages } = useStages();
  const { isLoggedIn, profile } = useAuth();

  const [selectedStage, setSelectedStage] = useState<Stage | null>(null);
  const [isTimelineOpen, setIsTimelineOpen] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (message: string) => {
    setToastMessage(message);
    setTimeout(() => {
      setToastMessage((prev) => (prev === message ? null : prev));
    }, 4000);
  };

  const handleStartStage = (stage: Stage) => {
    if (stage.lockStatus === 'LOCKED_PREVIOUS_STAGE') {
      showToast('برای باز شدن این مرحله، ابتدا باید مرحله قبلی را به پایان برسانید.');
      return;
    }
    if (stage.lockStatus === 'LOCKED_REGISTRATION') {
      setIsAuthModalOpen(true);
      return;
    }
    if (stage.lockStatus === 'LOCKED_SUBSCRIPTION') {
      navigate('/subscription');
      return;
    }

    confetti({
      particleCount: 50,
      spread: 60,
      origin: { y: 0.6 },
    });

    navigate(`/chat/${stage.id}`);
  };

  const isSubscriber = stats.currentTier === 'SUBSCRIBER';
  const totalStars = isLoggedIn && profile ? profile.totalStars : stats.totalStars;
  const completedStagesCount = stats.completedStagesCount;

  // Active hero stage fallback
  const heroStage = currentStage || stages[0];

  // Find the active phase group for the desktop curriculum strip
  const currentPhaseGroup =
    phaseGroups.find((g) => g.phaseNumber === heroStage?.phaseNumber) || phaseGroups[0];

  return (
    <div className="relative h-[100dvh] max-h-[100dvh] w-full bg-[#07080f] text-white selection:bg-apple-blue selection:text-white overflow-hidden flex flex-col justify-between">
      {/* 1. Ambient Background Layer with Apple Optical Refraction */}
      {heroStage && heroStage.backgroundUrl ? (
        <div className="fixed inset-0 z-0 select-none pointer-events-none overflow-hidden">
          <img
            src={heroStage.backgroundUrl}
            alt={heroStage.title}
            className="w-full h-full object-cover object-center filter brightness-[0.88] contrast-[1.05] lg:blur-3xl lg:opacity-25 lg:scale-125 transition-all duration-700"
          />
          {/* Dynamic Ambient Optical Light Orbs for Liquid Glass Refraction */}
          <div className="absolute -top-16 -right-16 w-80 h-80 rounded-full bg-apple-blue/20 blur-[100px] pointer-events-none" />
          <div className="absolute top-1/2 -left-20 w-80 h-80 rounded-full bg-apple-purple/20 blur-[110px] pointer-events-none" />
          <div className="absolute -bottom-10 right-1/4 w-72 h-72 rounded-full bg-apple-teal/15 blur-[90px] pointer-events-none" />
        </div>
      ) : (
        <div className="fixed inset-0 z-0 bg-gradient-to-b from-[#141728] via-[#090b14] to-[#040508]">
          <div className="absolute -top-16 -right-16 w-80 h-80 rounded-full bg-apple-blue/25 blur-[100px]" />
          <div className="absolute bottom-10 -left-10 w-80 h-80 rounded-full bg-apple-purple/25 blur-[110px]" />
        </div>
      )}

      {/* Optical Vignette & Scrim Gradients (Balanced so glass never turns solid black) */}
      <div className="fixed top-0 inset-x-0 h-28 bg-gradient-to-b from-black/50 via-black/15 to-transparent pointer-events-none z-10 lg:from-[#07080f]/85 lg:via-[#07080f]/40" />
      <div className="hidden lg:block fixed bottom-0 inset-x-0 h-44 bg-gradient-to-t from-black/75 via-black/30 to-transparent pointer-events-none z-10 lg:from-[#07080f]/90 lg:via-[#07080f]/35" />

      {/* 2. TOP HUD (Mobile View - Fixed at Top with Safe Area) */}
      <header className="fixed top-0 inset-x-0 z-30 w-full max-w-lg mx-auto pt-[max(env(safe-area-inset-top),14px)] px-4 flex lg:hidden items-center justify-between gap-2.5 pointer-events-auto">
        {/* Profile Avatar Button */}
        <button
          type="button"
          onClick={() => navigate('/profile')}
          className="w-10 h-10 rounded-2xl apple-glass-pill flex items-center justify-center text-white shadow-apple-sm active:scale-95 transition-all overflow-hidden shrink-0 border border-white/25"
          title="پروفایل کاربری"
        >
          {profile?.avatar ? (
            <img
              src={getAvatarUrl(profile.avatar)}
              alt="Avatar"
              className="w-full h-full object-cover"
            />
          ) : (
            <User className="w-5 h-5 text-slate-200" />
          )}
        </button>

        {/* Stars Metric Pill */}
        <div className="h-9 px-3 rounded-full apple-glass-gold flex items-center gap-1.5 text-apple-yellow shadow-apple-sm select-none">
          <Star className="w-3.5 h-3.5 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_6px_rgba(255,204,0,0.6)]" />
          <span className="text-xs font-bold font-mono">{totalStars.toLocaleString('fa-IR')}</span>
          <span className="text-[11px] font-sans font-medium text-apple-yellow/90">ستاره</span>
        </div>

        <div className="flex-1" />

        {/* VIP Subscription CTA */}
        {isSubscriber ? (
          <button
            type="button"
            onClick={() => navigate('/subscription')}
            className="h-9 px-3.5 rounded-full apple-glass bg-purple-900/35 border border-apple-purple/50 flex items-center gap-1.5 text-purple-200 text-xs font-bold active:scale-95 transition-all shadow-apple-sm"
          >
            <Crown className="w-3.5 h-3.5 fill-apple-purple text-apple-purple" />
            <span>اشتراک طلایی</span>
          </button>
        ) : (
          <button
            type="button"
            onClick={() => navigate('/subscription')}
            className="h-9 px-3.5 rounded-full bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange text-black font-extrabold text-xs flex items-center gap-1.5 shadow-[0_2px_12px_rgba(255,204,0,0.35)] active:scale-95 hover:brightness-105 transition-all"
          >
            <Crown className="w-3.5 h-3.5 fill-black text-black" />
            <span>اشتراک ویژه</span>
          </button>
        )}
      </header>

      {/* 2. TOP DESKTOP DOCK (Widescreen View) */}
      <header className="hidden lg:flex relative z-20 w-full max-w-6xl mx-auto pt-6 px-6 items-center justify-between shrink-0">
        <div className="w-full apple-glass-card rounded-[24px] px-6 py-3.5 flex items-center justify-between gap-4 border border-white/20 shadow-apple-lg">
          {/* Brand & Tagline */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-apple-blue to-apple-indigo p-0.5 flex items-center justify-center shadow-apple-sm">
              <div className="w-full h-full rounded-[14px] bg-[#0c0d16] flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-apple-teal" />
              </div>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-base tracking-tight text-white">AI Speaking Plus</span>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-apple-blue/20 text-apple-blue font-bold">
                  نسخه وب
                </span>
              </div>
              <p className="text-[11px] text-apple-gray-400">
                مکالمه هوشمند زبان انگلیسی در ۳۰ موقعیت واقعی در لندن
              </p>
            </div>
          </div>

          {/* Central Stats Group */}
          <div className="flex items-center gap-3 select-none">
            {/* Stars Pill */}
            <div className="h-9 px-3.5 rounded-full apple-glass-gold flex items-center gap-1.5 text-apple-yellow">
              <Star className="w-4 h-4 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_6px_rgba(255,204,0,0.6)]" />
              <span className="text-xs font-bold font-mono">{totalStars.toLocaleString('fa-IR')}</span>
              <span className="text-[11px] font-sans font-medium text-apple-yellow/90">ستاره</span>
            </div>

            {/* Stages Progress Pill */}
            <div className="h-9 px-3.5 rounded-full apple-glass-pill flex items-center gap-1.5 text-slate-200">
              <CheckCircle2 className="w-4 h-4 text-apple-green" />
              <span className="text-xs font-bold font-mono">
                {completedStagesCount.toLocaleString('fa-IR')} از {stages.length.toLocaleString('fa-IR')}
              </span>
              <span className="text-[11px] font-sans text-apple-gray-400">مرحله تکمیل شده</span>
            </div>

            {/* Level Pill */}
            <div className="h-9 px-3 rounded-full apple-glass-blue flex items-center gap-1 text-apple-teal">
              <span className="text-xs font-bold font-mono">
                سطح {profile?.languageLevel || 'A1'}
              </span>
            </div>
          </div>

          {/* Actions & Profile */}
          <div className="flex items-center gap-3">
            {isSubscriber ? (
              <button
                type="button"
                onClick={() => navigate('/subscription')}
                className="h-10 px-4 rounded-full apple-glass bg-purple-900/40 border border-apple-purple/40 flex items-center gap-2 text-apple-purple text-xs font-bold active:scale-95 transition-all shadow-apple-sm"
              >
                <Crown className="w-4 h-4 fill-apple-purple text-apple-purple" />
                <span>اشتراک طلایی</span>
              </button>
            ) : (
              <button
                type="button"
                onClick={() => navigate('/subscription')}
                className="h-10 px-4 rounded-full bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange text-black font-extrabold text-xs flex items-center gap-2 shadow-[0_2px_12px_rgba(255,204,0,0.35)] active:scale-95 hover:brightness-105 transition-all"
              >
                <Crown className="w-4 h-4 fill-black text-black" />
                <span>اشتراک ویژه</span>
              </button>
            )}

            <button
              type="button"
              onClick={() => navigate('/profile')}
              className="h-10 px-3 rounded-full apple-glass-pill flex items-center gap-2.5 text-white active:scale-95 transition-all border border-white/20"
            >
              <div className="w-7 h-7 rounded-full overflow-hidden bg-white/10 shrink-0">
                {profile?.avatar ? (
                  <img
                    src={getAvatarUrl(profile.avatar)}
                    alt="Avatar"
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <User className="w-4 h-4 text-slate-200 m-1.5" />
                )}
              </div>
              <span className="text-xs font-bold text-slate-100 max-w-[100px] truncate">
                {profile?.nickName || 'پروفایل'}
              </span>
            </button>
          </div>
        </div>
      </header>

      {/* 3. MOBILE VIEW: Sleek Bottom Glass Deck (Fixed Top & Bottom - No Scroll) */}
      <section className="fixed bottom-0 inset-x-0 z-30 w-full max-w-lg mx-auto px-4 pb-[max(env(safe-area-inset-bottom),16px)] pointer-events-auto lg:hidden">
        {isLoading && !heroStage ? (
          <div className="apple-glass rounded-[26px] p-4 border border-white/20 space-y-3">
            <CupertinoSkeleton className="h-5 w-1/3 rounded-xl" />
            <CupertinoSkeleton className="h-7 w-3/4 rounded-xl" />
            <CupertinoSkeleton className="h-11 rounded-2xl" />
          </div>
        ) : heroStage ? (
          <div className="liquid-glass-host">
            <LiquidGlass
              cornerRadius={26}
              blurAmount={0.25}
              saturation={180}
              aberrationIntensity={1.8}
              displacementScale={28}
              elasticity={0.05}
              padding="0px"
              className="w-full"
            >
              <div className="p-3.5 sm:p-4 space-y-2 w-full">
                {/* Top Info Bar: Stage Number & Stars */}
                <div className="flex items-center justify-between">
                  <span className="text-xs sm:text-[13px] font-extrabold text-apple-teal font-sans tracking-wide">
                    مرحله {heroStage.orderIndex.toLocaleString('fa-IR')} از {stages.length.toLocaleString('fa-IR')}
                  </span>
                  <CupertinoStarRating stars={heroStage.progress?.stars || 0} size="sm" />
                </div>

                {/* Stage Title */}
                <div>
                  <h1 className="text-lg sm:text-xl font-extrabold text-white leading-tight">
                    {heroStage.titleFa}
                  </h1>
                </div>

                {/* Character Avatar & Name */}
                <div className="flex items-center gap-2 pt-0.5">
                  <div className="w-6 h-6 rounded-full overflow-hidden bg-white/10 border border-white/30 flex items-center justify-center shrink-0">
                    {heroStage.characterAvatarUrl ? (
                      <img
                        src={heroStage.characterAvatarUrl}
                        alt={heroStage.characterName}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <User className="w-3.5 h-3.5 text-apple-blue" />
                    )}
                  </div>
                  <span className="text-xs text-white/70 font-medium tracking-tight" dir="ltr">
                    {heroStage.characterName}
                  </span>
                </div>

                {/* Primary Action Button (CTA) */}
                <div className="space-y-2 pt-0.5">
                  {heroStage.lockStatus === 'UNLOCKED' && (
                    <button
                      type="button"
                      onClick={() => handleStartStage(heroStage)}
                      className="w-full h-11 px-4 rounded-2xl bg-gradient-to-r from-apple-blue to-[#0062D2] hover:brightness-110 active:scale-[0.98] text-white font-extrabold text-sm shadow-[0_4px_18px_rgba(0,122,255,0.4)] flex items-center justify-center transition-all border border-white/20"
                    >
                      {heroStage.progress?.stars && heroStage.progress.stars > 0 ? (
                        <span>تکرار مرحله</span>
                      ) : (
                        <span>شروع مرحله</span>
                      )}
                    </button>
                  )}

                  {heroStage.lockStatus === 'LOCKED_REGISTRATION' && (
                    <button
                      type="button"
                      onClick={() => setIsAuthModalOpen(true)}
                      className="w-full h-11 px-4 rounded-2xl bg-gradient-to-r from-[#5856D6] to-purple-700 hover:brightness-110 active:scale-[0.98] text-white font-extrabold text-xs sm:text-sm shadow-apple-md flex items-center justify-center gap-2 transition-all border border-white/20"
                    >
                      <Lock className="w-4 h-4" />
                      <span>ورود و باز کردن مرحله</span>
                    </button>
                  )}

                  {heroStage.lockStatus === 'LOCKED_SUBSCRIPTION' && (
                    <button
                      type="button"
                      onClick={() => navigate('/subscription')}
                      className="w-full h-11 px-4 rounded-2xl bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange hover:brightness-110 active:scale-[0.98] text-black font-extrabold text-xs sm:text-sm shadow-[0_4px_16px_rgba(255,204,0,0.35)] flex items-center justify-center gap-2 transition-all border border-yellow-300/40"
                    >
                      <Crown className="w-4 h-4 fill-black" />
                      <span>باز کردن با اشتراک ویژه</span>
                    </button>
                  )}

                  {heroStage.lockStatus === 'LOCKED_PREVIOUS_STAGE' && (
                    <button
                      type="button"
                      disabled
                      className="w-full h-11 px-4 rounded-2xl bg-white/10 text-apple-gray-400 font-bold text-xs sm:text-sm border border-white/10 flex items-center justify-center gap-2 cursor-not-allowed opacity-80"
                    >
                      <Lock className="w-4 h-4" />
                      <span>ابتدا مرحله قبل را به پایان برسانید</span>
                    </button>
                  )}

                  {/* Secondary Button: Open Stages Timeline Curriculum */}
                  <button
                    type="button"
                    onClick={() => setIsTimelineOpen(true)}
                    className="w-full h-10 px-4 rounded-2xl apple-glass-pill hover:bg-white/15 active:scale-[0.98] text-slate-200 font-bold text-xs flex items-center justify-center transition-all"
                  >
                    <span>همه مرحله‌ها و پیشرفت</span>
                  </button>
                </div>
              </div>
            </LiquidGlass>
          </div>
        ) : null}
      </section>

      {/* 4. WIDESCREEN / DESKTOP RESPONSIVE WORKSPACE */}
      {/* Maintains exact vertical ratio of the artwork without stretching, paired with a lush Apple Glass stage hub */}
      <main className="hidden lg:grid grid-cols-12 gap-8 max-w-6xl w-full mx-auto my-auto py-8 px-6 items-center relative z-20">
        {heroStage ? (
          <>
            {/* RIGHT COLUMN (5 cols): Authentic Vertical Stage Artwork Showcase */}
            <div className="col-span-5 flex justify-center">
              <motion.div
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ type: 'spring', stiffness: 400, damping: 28 }}
                className="w-full max-w-[360px] aspect-[3/4] max-h-[560px] rounded-[36px] apple-glass-showcase overflow-hidden relative shadow-[0_24px_64px_-12px_rgba(0,0,0,0.7)] group border border-white/20 select-none flex flex-col justify-between p-4"
              >
                {/* Specular Rim Light */}
                <div
                  aria-hidden="true"
                  className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none z-20"
                />

                {/* Vertical Artwork (Contained / Fitted in authentic portrait ratio) */}
                {heroStage.backgroundUrl && (
                  <div className="absolute inset-0 z-0 overflow-hidden bg-black/40">
                    <img
                      src={heroStage.backgroundUrl}
                      alt={heroStage.titleFa}
                      className="w-full h-full object-cover object-center filter brightness-[0.88] contrast-[1.05] group-hover:scale-105 transition-transform duration-700 ease-out"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/20 to-black/60 pointer-events-none" />
                  </div>
                )}

                {/* Top Badge Overlay */}
                <div className="relative z-10 flex items-center justify-between">
                  <span className="apple-glass-pill px-3 py-1 rounded-full text-xs font-bold text-white border border-white/25">
                    مرحله {heroStage.orderIndex.toLocaleString('fa-IR')} از {stages.length.toLocaleString('fa-IR')}
                  </span>
                  <div className="apple-glass-pill px-2.5 py-1 rounded-full border border-white/25">
                    <CupertinoStarRating stars={heroStage.progress?.stars || 0} size="sm" />
                  </div>
                </div>

                {/* Bottom Overlay: Character info & voice preview */}
                <div className="relative z-10 apple-glass-pill rounded-2xl p-3 border border-white/25 space-y-2">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl overflow-hidden bg-white/10 border border-white/20 shrink-0">
                      {heroStage.characterAvatarUrl ? (
                        <img
                          src={heroStage.characterAvatarUrl}
                          alt={heroStage.characterName}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <User className="w-5 h-5 text-apple-blue m-2.5" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-white" dir="ltr">
                          {heroStage.characterName}
                        </span>
                        <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-apple-blue/20 text-apple-teal font-semibold">
                          میزبان AI
                        </span>
                      </div>
                      <span className="text-[11px] text-apple-gray-300 block truncate">
                        لهجه بومی بریتیش • تحلیل تلفظ زنده
                      </span>
                    </div>
                  </div>
                </div>
              </motion.div>
            </div>

            {/* LEFT COLUMN (7 cols): Stage Mission & Interactive Hub */}
            <div className="col-span-7">
              <motion.div
                initial={{ opacity: 0, x: -16 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ type: 'spring', stiffness: 400, damping: 28, delay: 0.05 }}
                className="apple-glass-card rounded-[36px] p-8 space-y-6 border border-white/16 shadow-apple-lg relative overflow-hidden"
              >
                {/* Specular Rim Light */}
                <div
                  aria-hidden="true"
                  className="absolute inset-x-0 top-0 h-[1.5px] apple-specular-edge pointer-events-none"
                />

                {/* Breadcrumbs & Level Badge */}
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2">
                    <CupertinoPill variant="blue" icon={<Compass className="w-3.5 h-3.5" />}>
                      {heroStage.seasonTitleFa}
                    </CupertinoPill>
                    <span className="text-xs text-apple-gray-400 font-medium">
                      {heroStage.phaseTitleFa}
                    </span>
                  </div>

                  <span className="text-xs font-mono font-bold px-3 py-1 rounded-full bg-white/10 text-apple-yellow border border-apple-yellow/30">
                    سطح الزامی: {profile?.languageLevel || 'A1'}
                  </span>
                </div>

                {/* Title & English Subtitle */}
                <div className="space-y-1.5">
                  <h1 className="text-3xl font-black text-white leading-tight">
                    {heroStage.titleFa}
                  </h1>
                  <p className="text-sm text-apple-gray-300 font-mono italic tracking-wide" dir="ltr">
                    {heroStage.title}
                  </p>
                </div>

                {/* Mission & Scenario Context Box */}
                <div className="p-4 rounded-2xl bg-white/[0.05] border border-white/10 space-y-2 backdrop-blur-sm">
                  <div className="flex items-center gap-2 text-xs font-bold text-apple-teal">
                    <Sparkles className="w-4 h-4 text-apple-teal shrink-0" />
                    <span>ماموریت مکالمه شما در این سناریو:</span>
                  </div>
                  <p className="text-xs text-slate-200 leading-relaxed">
                    {heroStage.briefingFa}
                  </p>
                  {heroStage.targetObjectiveFa && (
                    <p className="text-[11px] text-apple-gray-400 leading-relaxed border-t border-white/10 pt-2">
                      هدف مکالمه: {heroStage.targetObjectiveFa}
                    </p>
                  )}
                </div>

                {/* Primary & Secondary Action CTAs */}
                <div className="space-y-3 pt-1">
                  {heroStage.lockStatus === 'UNLOCKED' && (
                    <motion.button
                      whileTap={{ scale: 0.98 }}
                      type="button"
                      onClick={() => handleStartStage(heroStage)}
                      className="w-full py-4 px-6 rounded-2xl bg-gradient-to-r from-apple-blue via-indigo-600 to-purple-600 hover:brightness-110 text-white font-extrabold text-base shadow-[0_8px_24px_rgba(0,122,255,0.38)] flex items-center justify-center gap-2.5 transition-all cursor-pointer"
                    >
                      {heroStage.progress?.stars && heroStage.progress.stars > 0 ? (
                        <>
                          <RotateCcw className="w-5 h-5 ml-1" />
                          <span>تکرار مکالمه این مرحله</span>
                        </>
                      ) : (
                        <>
                          <Play className="w-5 h-5 fill-white ml-1" />
                          <span>شروع مکالمه هوشمند با {heroStage.characterName}</span>
                        </>
                      )}
                    </motion.button>
                  )}

                  {heroStage.lockStatus === 'LOCKED_REGISTRATION' && (
                    <motion.button
                      whileTap={{ scale: 0.98 }}
                      type="button"
                      onClick={() => setIsAuthModalOpen(true)}
                      className="w-full py-4 px-6 rounded-2xl bg-gradient-to-r from-[#5856D6] to-purple-700 hover:brightness-110 text-white font-extrabold text-sm shadow-apple-md flex items-center justify-center gap-2.5 transition-all cursor-pointer"
                    >
                      <Lock className="w-5 h-5" />
                      <span>ثبت‌نام و ورود برای باز شدن مرحله</span>
                    </motion.button>
                  )}

                  {heroStage.lockStatus === 'LOCKED_SUBSCRIPTION' && (
                    <motion.button
                      whileTap={{ scale: 0.98 }}
                      type="button"
                      onClick={() => navigate('/subscription')}
                      className="w-full py-4 px-6 rounded-2xl bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange hover:brightness-110 text-black font-extrabold text-sm shadow-[0_8px_24px_rgba(255,204,0,0.35)] flex items-center justify-center gap-2.5 transition-all cursor-pointer"
                    >
                      <Crown className="w-5 h-5 fill-black" />
                      <span>ارتقا به اشتراک ویژه برای باز شدن مرحله</span>
                    </motion.button>
                  )}

                  {heroStage.lockStatus === 'LOCKED_PREVIOUS_STAGE' && (
                    <button
                      type="button"
                      disabled
                      className="w-full py-4 px-6 rounded-2xl bg-white/10 text-apple-gray-400 font-bold text-sm border border-white/10 flex items-center justify-center gap-2.5 cursor-not-allowed opacity-80"
                    >
                      <Lock className="w-5 h-5" />
                      <span>ابتدا مرحله قبل را به پایان برسانید</span>
                    </button>
                  )}

                  {/* Secondary Curriculum Trigger */}
                  <motion.button
                    whileTap={{ scale: 0.98 }}
                    type="button"
                    onClick={() => setIsTimelineOpen(true)}
                    className="w-full py-3.5 px-6 rounded-2xl bg-white/[0.08] hover:bg-white/[0.12] text-slate-200 font-bold text-xs border border-white/10 flex items-center justify-center gap-2 transition-all cursor-pointer"
                  >
                    <Layers className="w-4 h-4 text-apple-blue" />
                    <span>
                      {completedStagesCount > 0
                        ? `مشاهده نقشه جامع ۳۰ مرحله (${completedStagesCount.toLocaleString('fa-IR')} انجام شده)`
                        : 'مشاهده نقشه جامع ۳۰ مرحله یادگیری'}
                    </span>
                  </motion.button>
                </div>

                {/* Phase Stages Quick Preview Strip (Desktop Curriculum Track) */}
                {currentPhaseGroup && (
                  <div className="pt-2 border-t border-white/10 space-y-2.5">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-apple-gray-400 font-medium">
                        مرحله‌های {currentPhaseGroup.phaseTitleFa}:
                      </span>
                      <span className="font-mono text-apple-gray-400">
                        {currentPhaseGroup.completedStages} از {currentPhaseGroup.totalStages} انجام شده
                      </span>
                    </div>

                    <div className="flex items-center gap-2 overflow-x-auto pb-1">
                      {currentPhaseGroup.stages.map((stg) => {
                        const isCurrent = stg.id === heroStage.id;
                        const isDone = (stg.progress?.stars || 0) > 0;
                        const isLocked = stg.lockStatus !== 'UNLOCKED';

                        return (
                          <button
                            key={stg.id}
                            type="button"
                            onClick={() => setSelectedStage(stg)}
                            className={`flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-semibold transition-all shrink-0 border ${
                              isCurrent
                                ? 'bg-apple-blue text-white border-apple-blue/50 shadow-apple-sm'
                                : isDone
                                ? 'bg-apple-yellow/15 text-apple-yellow border-apple-yellow/30'
                                : isLocked
                                ? 'bg-white/5 text-apple-gray-400 border-white/10 opacity-70'
                                : 'bg-white/10 text-slate-200 border-white/10 hover:bg-white/15'
                            }`}
                            title={stg.titleFa}
                          >
                            <span className="font-mono text-[11px]">{stg.orderIndex}</span>
                            <span className="max-w-[110px] truncate">{stg.titleFa}</span>
                            {isDone && <CheckCircle2 className="w-3.5 h-3.5 text-apple-yellow shrink-0" />}
                            {isLocked && <Lock className="w-3 h-3 text-apple-gray-400 shrink-0" />}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}
              </motion.div>
            </div>
          </>
        ) : null}
      </main>

      {/* 5. Full Stages Curriculum Timeline Modal */}
      <CupertinoModal
        isOpen={isTimelineOpen}
        onClose={() => setIsTimelineOpen(false)}
        title="نقشه جامع ۳۰ مرحله مکالمه"
        subtitle="مسیر یادگیری سناریو محور در لندن"
      >
        <div className="space-y-4">
          <StagesTimeline
            phaseGroups={phaseGroups}
            currentActiveStageId={currentStage?.id || null}
            onSelectStage={(stg) => {
              setSelectedStage(stg);
            }}
          />
        </div>
      </CupertinoModal>

      {/* 6. Stage Briefing Details Modal */}
      <StageBriefingModal
        stage={selectedStage}
        isOpen={Boolean(selectedStage)}
        onClose={() => setSelectedStage(null)}
        onStartStage={handleStartStage}
        onRequireAuth={() => setIsAuthModalOpen(true)}
        onRequireSubscription={() => navigate('/subscription')}
      />

      {/* 7. Cupertino Clean Login Dialog */}
      <LoginDialog
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={() => {
          showToast('ورود با موفقیت انجام شد!');
          refreshStages();
        }}
      />

      {/* 8. Floating Apple Toast */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div
            initial={{ opacity: 0, y: -24, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -16, scale: 0.95 }}
            transition={{ type: 'spring', stiffness: 450, damping: 28 }}
            className="fixed top-20 inset-x-4 max-w-sm mx-auto z-50 pointer-events-none"
          >
            <div className="apple-glass bg-[#1a1c26]/95 border border-white/20 text-white text-xs px-4 py-3 rounded-2xl shadow-apple-lg flex items-center gap-2 pointer-events-auto">
              <Sparkles className="w-4 h-4 text-apple-yellow shrink-0" />
              <span className="leading-relaxed">{toastMessage}</span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default HomePage;
