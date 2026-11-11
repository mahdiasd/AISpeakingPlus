import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import confetti from 'canvas-confetti';
import { Sparkles, CheckCircle2, Lock } from 'lucide-react';
import { CupertinoModal, CupertinoSkeleton } from '../components/cupertino';
import { HomeHeaderStats } from '../components/home/HomeHeaderStats';
import { ActiveStageHero } from '../components/home/ActiveStageHero';
import { StageBriefingModal } from '../components/home/StageBriefingModal';
import { StagesTimeline } from '../components/home/StagesTimeline';
import { LoginDialog } from '../components/auth/LoginDialog';
import { useStages } from '../hooks/useStages';
import { useAuth } from '../hooks/useAuth';
import { Stage } from '../types/stage';
import { getAssetUrl } from '../utils/avatar';

/**
 * HomePage Component
 * Matches Compose Multiplatform's `JourneyMapScreen`:
 * 1. Top HUD: HomeHeaderStats with Right User Avatar, Center Star Counter Pill («۰ ستاره»), and Left VIP Gold Gradient Button.
 * 2. Active Stage Hero: Floating glass card (.apple-glass-card with blur 14-16px) letting the London flight background shine through.
 * 3. Dynamic background wallpaper, responsive mobile & desktop views, modals & navigation.
 */
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

  // Active hero stage fallback (Flight to London by default)
  const heroStage = currentStage || stages[0];

  // Active phase group for curriculum preview
  const currentPhaseGroup =
    phaseGroups.find((g) => g.phaseNumber === heroStage?.phaseNumber) || phaseGroups[0];

  // Wallpaper URL resolution
  const backgroundWallpaper = heroStage?.backgroundUrl
    ? getAssetUrl(heroStage.backgroundUrl)
    : getAssetUrl('resources/stages/bg_1.webp');

  return (
    <div
      dir="rtl"
      className="relative h-[100dvh] max-h-[100dvh] w-full bg-[#07080f] text-white selection:bg-apple-blue selection:text-white overflow-hidden flex flex-col justify-between"
    >
      {/* 1. Dynamic Wallpaper Layer with London Scenery */}
      {heroStage ? (
        <div className="fixed inset-0 z-0 select-none pointer-events-none overflow-hidden">
          <img
            src={backgroundWallpaper}
            alt={heroStage.titleFa || 'پرواز به لندن'}
            className="w-full h-full object-cover object-center filter brightness-[0.88] contrast-[1.05] transition-all duration-700"
            onError={(e) => {
              // Fallback to default flight background if asset path fails
              const target = e.target as HTMLImageElement;
              if (!target.src.includes('bg_1.webp')) {
                target.src = getAssetUrl('resources/stages/bg_1.webp');
              }
            }}
          />
          {/* Dynamic Optical Ambient Orbs for Liquid Specular Refraction */}
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

      {/* Optical Top & Bottom Scrim Gradients (Keeps HUD readable while London scenery shines through) */}
      <div className="fixed top-0 inset-x-0 h-40 bg-gradient-to-b from-[#07080f]/90 via-[#07080f]/40 to-transparent pointer-events-none z-10" />
      <div className="fixed bottom-0 inset-x-0 h-[48vh] bg-gradient-to-t from-[#07080f]/95 via-[#07080f]/60 to-transparent pointer-events-none z-10" />

      {/* 2. Top HUD Bar (Unified HomeHeaderStats Component) */}
      <div className="fixed top-0 inset-x-0 z-30 w-full max-w-lg lg:max-w-4xl mx-auto pt-[max(env(safe-area-inset-top),14px)] lg:pt-6 px-4 lg:px-6 pointer-events-auto">
        <HomeHeaderStats
          totalStars={totalStars}
          isSubscriber={isSubscriber}
          remainingDays={profile?.subscription?.remainingDays ?? 0}
          avatarUrl={profile?.avatar}
          userName={profile?.nickName}
          onProfileClick={() => navigate('/profile')}
          onSubscribeClick={() => navigate('/subscription')}
          onLeaderboardClick={() =>
            showToast(`مجموع ستاره‌های کسب شده شما: ${totalStars.toLocaleString('fa-IR')} ستاره`)
          }
        />
      </div>

      {/* 3. MOBILE VIEW: Floating Bottom Glass Deck */}
      <section className="fixed bottom-0 inset-x-0 z-20 w-full max-w-lg mx-auto px-4 pb-[max(env(safe-area-inset-bottom),16px)] pointer-events-auto lg:hidden">
        {isLoading && !heroStage ? (
          <div className="apple-glass rounded-[28px] p-5 border border-white/20 space-y-3">
            <CupertinoSkeleton className="h-5 w-1/3 rounded-xl" />
            <CupertinoSkeleton className="h-7 w-3/4 rounded-xl" />
            <CupertinoSkeleton className="h-12 rounded-2xl" />
          </div>
        ) : heroStage ? (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ type: 'spring', stiffness: 400, damping: 28 }}
          >
            <ActiveStageHero
              stage={heroStage}
              totalStages={stages.length || 30}
              completedStagesCount={completedStagesCount}
              onStartStage={handleStartStage}
              onOpenTimeline={() => setIsTimelineOpen(true)}
              onRequireAuth={() => setIsAuthModalOpen(true)}
              onRequireSubscription={() => navigate('/subscription')}
            />
          </motion.div>
        ) : null}
      </section>

      {/* 4. DESKTOP / WIDESCREEN VIEW: Centered Elegant Showcase */}
      <main className="hidden lg:flex flex-col items-center justify-center flex-1 max-w-3xl w-full mx-auto px-6 relative z-20 my-auto pointer-events-auto">
        {isLoading && !heroStage ? (
          <div className="apple-glass-card rounded-[32px] p-8 w-full max-w-xl space-y-4">
            <CupertinoSkeleton className="h-6 w-1/4 rounded-xl" />
            <CupertinoSkeleton className="h-10 w-2/3 rounded-xl" />
            <CupertinoSkeleton className="h-20 rounded-2xl" />
            <CupertinoSkeleton className="h-14 rounded-2xl" />
          </div>
        ) : heroStage ? (
          <motion.div
            initial={{ opacity: 0, scale: 0.96 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ type: 'spring', stiffness: 380, damping: 28 }}
            className="w-full max-w-xl space-y-4"
          >
            <ActiveStageHero
              stage={heroStage}
              totalStages={stages.length || 30}
              completedStagesCount={completedStagesCount}
              onStartStage={handleStartStage}
              onOpenTimeline={() => setIsTimelineOpen(true)}
              onRequireAuth={() => setIsAuthModalOpen(true)}
              onRequireSubscription={() => navigate('/subscription')}
              className="max-w-xl"
            />

            {/* Desktop Quick Phase Stages Curriculum Strip */}
            {currentPhaseGroup && currentPhaseGroup.stages.length > 0 && (
              <div className="apple-glass rounded-2xl p-3.5 border border-white/15 space-y-2 select-none">
                <div className="flex items-center justify-between text-xs px-1">
                  <span className="text-apple-teal font-extrabold">
                    {currentPhaseGroup.phaseTitleFa}
                  </span>
                  <span className="font-mono text-slate-300 text-[11px]">
                    {currentPhaseGroup.completedStages.toLocaleString('fa-IR')} از{' '}
                    {currentPhaseGroup.totalStages.toLocaleString('fa-IR')} مرحله تکمیل شده
                  </span>
                </div>

                <div className="flex items-center gap-2 overflow-x-auto pb-0.5">
                  {currentPhaseGroup.stages.map((stg) => {
                    const isCurrent = stg.id === heroStage.id;
                    const isDone = (stg.progress?.stars || 0) > 0;
                    const isStgLocked = stg.lockStatus !== 'UNLOCKED';

                    return (
                      <button
                        key={stg.id}
                        type="button"
                        onClick={() => setSelectedStage(stg)}
                        className={`flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-semibold transition-all shrink-0 border cursor-pointer ${
                          isCurrent
                            ? 'bg-apple-blue text-white border-apple-blue/50 shadow-apple-sm'
                            : isDone
                            ? 'bg-apple-yellow/15 text-apple-yellow border-apple-yellow/30 hover:bg-apple-yellow/20'
                            : isStgLocked
                            ? 'bg-white/5 text-slate-400 border-white/10 opacity-70'
                            : 'bg-white/10 text-slate-200 border-white/10 hover:bg-white/15'
                        }`}
                        title={stg.titleFa}
                      >
                        <span className="font-mono text-[11px]">{stg.orderIndex}</span>
                        <span className="max-w-[120px] truncate">{stg.titleFa}</span>
                        {isDone && <CheckCircle2 className="w-3.5 h-3.5 text-apple-yellow shrink-0" />}
                        {isStgLocked && <Lock className="w-3 h-3 text-slate-400 shrink-0" />}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}
          </motion.div>
        ) : null}
      </main>

      {/* 5. Stages Curriculum Timeline Modal */}
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

      {/* 7. Clean Login Dialog */}
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
