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
} from 'lucide-react';
import {
  CupertinoModal,
  CupertinoStarRating,
  CupertinoSkeleton,
} from '../components/cupertino';
import { StageBriefingModal } from '../components/home/StageBriefingModal';
import { StagesTimeline } from '../components/home/StagesTimeline';
import { LoginDialog } from '../components/auth/LoginDialog';
import { useStages } from '../hooks/useStages';
import { useAuth } from '../hooks/useAuth';
import { Stage } from '../types/stage';
import { getAvatarUrl } from '../utils/avatar';

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

  return (
    <div className="relative min-h-screen w-full bg-[#07080c] text-white selection:bg-apple-blue selection:text-white overflow-hidden flex flex-col justify-between">
      {/* 1. Fullscreen Hero Artwork Background */}
      {heroStage && heroStage.backgroundUrl ? (
        <div className="fixed inset-0 z-0 select-none pointer-events-none overflow-hidden">
          <img
            src={heroStage.backgroundUrl}
            alt={heroStage.title}
            className="w-full h-full object-cover object-center filter brightness-[0.78] contrast-[1.05] transition-all duration-700"
          />
        </div>
      ) : (
        <div className="fixed inset-0 z-0 bg-gradient-to-b from-[#141728] via-[#090b14] to-[#040508]" />
      )}

      {/* 2. Optical Top & Bottom Scrim Gradients */}
      <div className="fixed top-0 inset-x-0 h-44 bg-gradient-to-b from-black/85 via-black/45 to-transparent pointer-events-none z-10" />
      <div className="fixed bottom-0 inset-x-0 h-[62%] bg-gradient-to-t from-black/95 via-black/60 to-transparent pointer-events-none z-10" />

      {/* 3. Top HUD (Floating Navbar) */}
      <header className="relative z-20 w-full max-w-lg mx-auto pt-4 px-4 flex items-center justify-between gap-3">
        {/* Profile Avatar Button */}
        <button
          type="button"
          onClick={() => navigate('/profile')}
          className="w-11 h-11 rounded-full apple-glass bg-[#181a26]/85 border border-white/20 flex items-center justify-center text-white shadow-apple-sm active:scale-95 transition-all overflow-hidden shrink-0"
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
        <div className="h-10 px-3.5 rounded-full apple-glass bg-[#181a26]/85 border border-apple-yellow/30 flex items-center gap-1.5 text-apple-yellow shadow-apple-sm select-none">
          <Star className="w-4 h-4 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_6px_rgba(255,204,0,0.6)]" />
          <span className="text-xs font-bold font-mono">{totalStars.toLocaleString('fa-IR')}</span>
          <span className="text-[11px] font-sans font-medium text-apple-yellow/90">ستاره</span>
        </div>

        <div className="flex-1" />

        {/* VIP Subscription CTA */}
        {isSubscriber ? (
          <button
            type="button"
            onClick={() => navigate('/subscription')}
            className="h-10 px-3.5 rounded-full apple-glass bg-purple-900/40 border border-apple-purple/40 flex items-center gap-1.5 text-apple-purple text-xs font-bold active:scale-95 transition-all shadow-apple-sm"
          >
            <Crown className="w-4 h-4 fill-apple-purple text-apple-purple" />
            <span>اشتراک طلایی</span>
          </button>
        ) : (
          <button
            type="button"
            onClick={() => navigate('/subscription')}
            className="h-10 px-4 rounded-full bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange text-black font-extrabold text-xs flex items-center gap-1.5 shadow-[0_2px_12px_rgba(255,204,0,0.35)] active:scale-95 hover:brightness-105 transition-all"
          >
            <Crown className="w-4 h-4 fill-black text-black" />
            <span>اشتراک ویژه</span>
          </button>
        )}
      </header>

      {/* 4. Middle Ambient Spacer */}
      <div className="flex-1" />

      {/* 5. Bottom Hero Stage Card */}
      <section className="relative z-20 w-full max-w-lg mx-auto px-4 pb-6">
        {isLoading && !heroStage ? (
          <div className="apple-glass rounded-[28px] p-6 border border-white/20 space-y-4">
            <CupertinoSkeleton className="h-6 w-1/3 rounded-xl" />
            <CupertinoSkeleton className="h-8 w-3/4 rounded-xl" />
            <CupertinoSkeleton className="h-14 rounded-2xl" />
          </div>
        ) : heroStage ? (
          <div className="apple-glass bg-[#141724]/90 backdrop-blur-3xl rounded-[28px] p-5 border border-white/20 shadow-apple-lg space-y-4">
            {/* Top Info Bar: Stage Number & Stars */}
            <div className="flex items-center justify-between">
              <span className="text-xs font-extrabold text-apple-blue font-sans tracking-wide">
                مرحله {heroStage.orderIndex.toLocaleString('fa-IR')} از {stages.length.toLocaleString('fa-IR')}
              </span>
              <CupertinoStarRating stars={heroStage.progress?.stars || 0} size="sm" />
            </div>

            {/* Stage Title & Season */}
            <div className="space-y-1">
              <span className="text-[11px] text-apple-teal font-semibold block">
                {heroStage.seasonTitleFa} • {heroStage.phaseTitleFa}
              </span>
              <h1 className="text-xl font-extrabold text-white leading-snug">
                {heroStage.titleFa}
              </h1>
            </div>

            {/* Character Squircle & Name */}
            <div className="flex items-center gap-2.5 pt-0.5">
              <div className="w-8 h-8 rounded-xl overflow-hidden bg-white/10 border border-white/20 flex items-center justify-center shrink-0">
                {heroStage.characterAvatarUrl ? (
                  <img
                    src={heroStage.characterAvatarUrl}
                    alt={heroStage.characterName}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <User className="w-4 h-4 text-apple-blue" />
                )}
              </div>
              <span className="text-xs text-apple-gray-300 font-medium" dir="ltr">
                {heroStage.characterName}
              </span>
            </div>

            {/* Primary Action Button (CTA) */}
            <div className="space-y-2 pt-1">
              {heroStage.lockStatus === 'UNLOCKED' && (
                <button
                  type="button"
                  onClick={() => handleStartStage(heroStage)}
                  className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-apple-blue to-[#5856D6] hover:brightness-110 active:scale-[0.98] text-white font-extrabold text-sm shadow-[0_4px_16px_rgba(0,122,255,0.35)] flex items-center justify-center gap-2 transition-all"
                >
                  {heroStage.progress?.stars && heroStage.progress.stars > 0 ? (
                    <>
                      <RotateCcw className="w-4 h-4 ml-0.5" />
                      <span>تکرار مرحله</span>
                    </>
                  ) : (
                    <>
                      <Play className="w-4 h-4 fill-white ml-0.5" />
                      <span>شروع مرحله</span>
                    </>
                  )}
                </button>
              )}

              {heroStage.lockStatus === 'LOCKED_REGISTRATION' && (
                <button
                  type="button"
                  onClick={() => setIsAuthModalOpen(true)}
                  className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-[#5856D6] to-purple-700 hover:brightness-110 active:scale-[0.98] text-white font-extrabold text-sm shadow-apple-md flex items-center justify-center gap-2 transition-all"
                >
                  <Lock className="w-4 h-4" />
                  <span>ورود و باز کردن مرحله</span>
                </button>
              )}

              {heroStage.lockStatus === 'LOCKED_SUBSCRIPTION' && (
                <button
                  type="button"
                  onClick={() => navigate('/subscription')}
                  className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-apple-yellow via-amber-400 to-apple-orange hover:brightness-110 active:scale-[0.98] text-black font-extrabold text-sm shadow-[0_4px_16px_rgba(255,204,0,0.35)] flex items-center justify-center gap-2 transition-all"
                >
                  <Crown className="w-4 h-4 fill-black" />
                  <span>باز کردن با اشتراک ویژه</span>
                </button>
              )}

              {heroStage.lockStatus === 'LOCKED_PREVIOUS_STAGE' && (
                <button
                  type="button"
                  disabled
                  className="w-full py-3.5 px-4 rounded-2xl bg-white/10 text-apple-gray-400 font-bold text-sm border border-white/10 flex items-center justify-center gap-2 cursor-not-allowed opacity-80"
                >
                  <Lock className="w-4 h-4" />
                  <span>ابتدا مرحله قبل را به پایان برسانید</span>
                </button>
              )}

              {/* Secondary Button: Open Stages Timeline Curriculum */}
              <button
                type="button"
                onClick={() => setIsTimelineOpen(true)}
                className="w-full py-3 px-4 rounded-2xl bg-white/10 hover:bg-white/15 active:scale-[0.98] text-slate-200 font-bold text-xs border border-white/10 flex items-center justify-center gap-2 transition-all"
              >
                <Layers className="w-4 h-4 text-apple-blue" />
                <span>
                  {completedStagesCount > 0
                    ? `همه مرحله‌ها و پیشرفت (${completedStagesCount.toLocaleString('fa-IR')} انجام شده)`
                    : 'همه مرحله‌ها و نقشه مسیر'}
                </span>
              </button>
            </div>
          </div>
        ) : null}
      </section>

      {/* 6. Full Stages Curriculum Timeline Modal */}
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

      {/* 7. Stage Briefing Details Modal */}
      <StageBriefingModal
        stage={selectedStage}
        isOpen={Boolean(selectedStage)}
        onClose={() => setSelectedStage(null)}
        onStartStage={handleStartStage}
        onRequireAuth={() => setIsAuthModalOpen(true)}
        onRequireSubscription={() => navigate('/subscription')}
      />

      {/* 8. Cupertino Clean Login Dialog */}
      <LoginDialog
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={() => {
          showToast('ورود با موفقیت انجام شد!');
          refreshStages();
        }}
      />

      {/* 9. Floating Apple Toast */}
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
