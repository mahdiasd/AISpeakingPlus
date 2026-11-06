import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import confetti from 'canvas-confetti';
import {
  Layers,
  Sparkles,
  User,
  LogOut,
  Star,
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
} from 'lucide-react';
import {
  CupertinoNavBar,
  CupertinoCard,
  CupertinoButton,
  CupertinoSegmentedControl,
  CupertinoSwitch,
  CupertinoSkeleton,
} from '../components/cupertino';
import { HomeHeaderStats } from '../components/home/HomeHeaderStats';
import { ActiveStageHero } from '../components/home/ActiveStageHero';
import { StagesTimeline } from '../components/home/StagesTimeline';
import { StageBriefingModal } from '../components/home/StageBriefingModal';
import { LoginDialog } from '../components/auth/LoginDialog';
import { SubscriptionModal } from '../components/home/SubscriptionModal';
import { useStages } from '../hooks/useStages';
import { useAuth } from '../hooks/useAuth';
import { Stage } from '../types/stage';
import { getAvatarUrl } from '../utils/avatar';

type HomeTab = 'hero' | 'timeline';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<HomeTab>('hero');
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [isSubscriptionModalOpen, setIsSubscriptionModalOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Apple Design Setting Toggles
  const [glassMode, setGlassMode] = useState(true);
  const [soundFeedback, setSoundFeedback] = useState(true);

  // Data Hooks
  const {
    stages,
    currentStage,
    stats,
    phaseGroups,
    seasonTitleFa,
    isLoading,
    error,
    selectedStage,
    setSelectedStage,
    refreshStages,
    recordGuestCompletion,
  } = useStages();

  const {
    profile,
    isLoggedIn,
    logout,
  } = useAuth();

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

    // Trigger celebration & navigate
    confetti({
      particleCount: 50,
      spread: 60,
      origin: { y: 0.6 },
    });

    navigate(`/chat/${stage.id}`);
  };

  return (
    <div className="flex flex-col min-h-screen bg-[#0c0c0e] text-white selection:bg-apple-blue selection:text-white pb-24">
      {/* 1. Cupertino Top App Bar */}
      <CupertinoNavBar
        title="AI Speaking Plus"
        subtitle={seasonTitleFa}
        endSlot={
          <div className="flex items-center gap-2">
            {/* Stars Count Badge in Header */}
            <div className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-apple-yellow/15 border border-apple-yellow/30 text-apple-yellow select-none">
              <Star className="w-3.5 h-3.5 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_6px_rgba(255,204,0,0.6)]" />
              <span className="text-xs font-bold font-mono">{stats.totalStars}</span>
            </div>

            {/* Profile / Auth Avatar Button */}
            {isLoggedIn ? (
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  onClick={() => navigate('/profile')}
                  title="مشاهده پروفایل کاربری"
                  className="flex items-center gap-1.5 px-2 py-1 rounded-full bg-white/10 hover:bg-white/15 border border-white/15 text-xs text-white active:scale-95 transition-all"
                >
                  <img
                    src={getAvatarUrl(profile?.avatar)}
                    alt="avatar"
                    className="w-5 h-5 rounded-full object-cover border border-white/20"
                  />
                  <span className="text-[11px] font-medium max-w-[80px] truncate hidden sm:inline">
                    {profile?.nickName || 'پروفایل'}
                  </span>
                </button>
                <button
                  type="button"
                  onClick={logout}
                  title="خروج از حساب"
                  className="w-8 h-8 rounded-full bg-white/10 hover:bg-red-500/20 border border-white/15 hover:border-red-500/30 flex items-center justify-center text-apple-gray-300 hover:text-red-400 active:scale-95 transition-all"
                >
                  <LogOut className="w-3.5 h-3.5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  onClick={() => setIsAuthModalOpen(true)}
                  className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-apple-blue hover:bg-blue-600 text-xs font-semibold text-white active:scale-95 transition-transform shadow-sm"
                >
                  <User className="w-3.5 h-3.5" />
                  <span>ورود</span>
                </button>
                <button
                  type="button"
                  onClick={() => navigate('/profile')}
                  title="پروفایل کاربری"
                  className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/15 border border-white/15 flex items-center justify-center text-apple-gray-300 active:scale-95 transition-transform"
                >
                  <User className="w-4 h-4" />
                </button>
              </div>
            )}
          </div>
        }
      />

      {/* 2. Main Scrollable Container */}
      <main className="flex-1 w-full max-w-lg mx-auto p-4 space-y-4">
        {/* Navigation Mode Segmented Control */}
        <CupertinoSegmentedControl<HomeTab>
          value={activeTab}
          onChange={setActiveTab}
          items={[
            {
              id: 'hero',
              label: 'داستان و مرحله فعال',
              icon: <Sparkles className="w-3.5 h-3.5" />,
            },
            {
              id: 'timeline',
              label: `نقشه مسیر (${stats.totalStages} مرحله)`,
              icon: <Layers className="w-3.5 h-3.5" />,
            },
          ]}
        />

        {/* Loading Skeleton State */}
        {isLoading && stages.length === 0 && (
          <div className="space-y-4">
            <CupertinoSkeleton className="h-44 w-full" />
            <CupertinoSkeleton className="h-64 w-full" />
            <div className="space-y-2">
              <CupertinoSkeleton className="h-20 w-full" />
              <CupertinoSkeleton className="h-20 w-full" />
            </div>
          </div>
        )}

        {/* Error State */}
        {!isLoading && error && stages.length === 0 && (
          <CupertinoCard variant="glass" className="text-center py-8 space-y-4">
            <AlertTriangle className="w-12 h-12 text-apple-red mx-auto" />
            <div className="space-y-1">
              <h3 className="text-base font-bold text-white">خطا در بارگذاری اطلاعات</h3>
              <p className="text-xs text-apple-gray-400">{error}</p>
            </div>
            <CupertinoButton
              variant="filled"
              size="md"
              icon={<RefreshCw className="w-4 h-4" />}
              onClick={refreshStages}
            >
              تلاش دوباره
            </CupertinoButton>
          </CupertinoCard>
        )}

        {/* Content When Ready */}
        {stages.length > 0 && (
          <>
            {/* Header Progress Stats */}
            <HomeHeaderStats stats={stats} seasonTitleFa={seasonTitleFa} />

            {/* Tab 1: Hero View */}
            {activeTab === 'hero' && (
              <div className="space-y-4">
                <ActiveStageHero
                  stage={currentStage}
                  onStartStage={handleStartStage}
                  onOpenDetails={setSelectedStage}
                  onRequireAuth={() => setIsAuthModalOpen(true)}
                  onRequireSubscription={() => setIsSubscriptionModalOpen(true)}
                />

                {/* Guest Demo Progress Action (Quick Testing) */}
                {!isLoggedIn && currentStage?.id === 'stage-01-inflight-london' && (
                  <CupertinoCard variant="solid" className="p-3.5 flex items-center justify-between gap-3">
                    <div className="space-y-0.5">
                      <span className="text-xs font-bold text-white flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-apple-green" />
                        <span>تست پیشرفت مرحله ۱ (کاربر مهمان)</span>
                      </span>
                      <p className="text-[11px] text-apple-gray-400">
                        ثبت ۳ ستاره برای مرحله ۱ و آزمایش رفتار گیت مرحله ۲
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={() => {
                        recordGuestCompletion('stage-01-inflight-london', 3, 98);
                        confetti({ particleCount: 40, spread: 50 });
                        showToast('مرحله ۱ با ۳ ستاره با موفقیت ثبت شد!');
                      }}
                      className="px-3 py-1.5 rounded-xl bg-apple-green text-black font-bold text-xs shrink-0 active:scale-95 transition-transform"
                    >
                      ثبت امتیاز
                    </button>
                  </CupertinoCard>
                )}

                {/* Apple Settings Style Polish Section */}
                <div className="space-y-2 pt-1">
                  <h3 className="text-xs font-semibold text-apple-gray-400 px-2">
                    تنظیمات رابط کاربری و عملکرد اپل (Apple HIG)
                  </h3>

                  <CupertinoCard variant="solid" className="p-0 overflow-hidden divide-y divide-white/10">
                    <div className="flex items-center justify-between p-4">
                      <div className="space-y-0.5">
                        <span className="text-sm font-medium">افکت شیشه‌ای اپل (Liquid Glass)</span>
                        <p className="text-xs text-apple-gray-400">
                          بلور سخت‌افزاری بلادرنگ با رندرینگ ۶۰ تا ۱۲۰ فریم بر ثانیه
                        </p>
                      </div>
                      <CupertinoSwitch checked={glassMode} onChange={setGlassMode} />
                    </div>

                    <div className="flex items-center justify-between p-4">
                      <div className="space-y-0.5">
                        <span className="text-sm font-medium">بازخورد صوتی زنده (Tactile Audio)</span>
                        <p className="text-xs text-apple-gray-400">
                          افکت‌های صوتی فیزیکی WWDC حین لمس کلیدها
                        </p>
                      </div>
                      <CupertinoSwitch checked={soundFeedback} onChange={setSoundFeedback} />
                    </div>
                  </CupertinoCard>
                </div>
              </div>
            )}

            {/* Tab 2: Full Journey Curriculum Timeline */}
            {activeTab === 'timeline' && (
              <StagesTimeline
                phaseGroups={phaseGroups}
                currentActiveStageId={currentStage?.id || null}
                onSelectStage={setSelectedStage}
              />
            )}
          </>
        )}
      </main>

      {/* Floating Bottom Toast Notification */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div
            initial={{ opacity: 0, y: 30, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.95 }}
            transition={{ type: 'spring', stiffness: 450, damping: 28 }}
            className="fixed bottom-6 inset-x-4 max-w-sm mx-auto z-50 pointer-events-none"
          >
            <div className="apple-glass bg-[#1a1c26]/95 border border-white/20 text-white text-xs px-4 py-3 rounded-2xl shadow-apple-lg flex items-center gap-2 pointer-events-auto">
              <Sparkles className="w-4 h-4 text-apple-blue shrink-0" />
              <span className="leading-relaxed">{toastMessage}</span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Stage Briefing Modal */}
      <StageBriefingModal
        stage={selectedStage}
        isOpen={Boolean(selectedStage)}
        onClose={() => setSelectedStage(null)}
        onStartStage={handleStartStage}
        onRequireAuth={() => setIsAuthModalOpen(true)}
        onRequireSubscription={() => setIsSubscriptionModalOpen(true)}
      />

      {/* Cupertino Login & Registration Dialog */}
      <LoginDialog
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={() => {
          showToast('ورود با موفقیت انجام شد!');
          refreshStages();
        }}
      />

      {/* Subscription Paywall Modal */}
      <SubscriptionModal
        isOpen={isSubscriptionModalOpen}
        onClose={() => setIsSubscriptionModalOpen(false)}
        onSubscribe={() => {
          setIsSubscriptionModalOpen(false);
          navigate('/subscription');
        }}
      />
    </div>
  );
};

export default HomePage;
