import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import { AlertCircle } from 'lucide-react';
import { useStages } from '../hooks/useStages';
import { useAuth } from '../context/AuthContext';
import { StagesTimeline } from '../components/home/StagesTimeline';
import { LoginDialog } from '../components/auth/LoginDialog';
import { Stage } from '../types/stage';

export const StagesPage: React.FC = () => {
  const navigate = useNavigate();
  const { stages, currentStage, phaseGroups, stats, isLoading, error, refreshStages } = useStages();
  const { isLoggedIn, profile } = useAuth();

  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage((curr) => (curr === msg ? null : curr));
    }, 3800);
  };

  const handleStageClick = (stage: Stage) => {
    if (stage.lockStatus === 'LOCKED_PREVIOUS_STAGE') {
      showToast('برای باز شدن این مرحله، ابتدا باید مرحله قبلی را به پایان برسانید.');
      return;
    }
    if (stage.lockStatus === 'LOCKED_REGISTRATION') {
      if (!isLoggedIn) {
        setIsAuthModalOpen(true);
      } else {
        showToast('این مرحله نیازمند به‌روزرسانی وضعیت حساب کاربری شماست.');
      }
      return;
    }
    if (stage.lockStatus === 'LOCKED_SUBSCRIPTION') {
      navigate('/subscription');
      return;
    }

    // Unlocked stage -> Navigate to chat
    navigate(`/chat/${stage.id}`);
  };

  const totalStars = isLoggedIn && profile ? profile.totalStars : stats.totalStars;

  return (
    <div
      className="relative min-h-screen w-full bg-[#07080f] text-white selection:bg-apple-blue selection:text-white overflow-x-hidden flex flex-col items-center justify-start"
      dir="rtl"
    >
      {/* 1. Atmospheric Scenic Wallpaper Background with Apple Frosted Glass Overlay */}
      {currentStage?.backgroundUrl ? (
        <div className="fixed inset-0 z-0 select-none pointer-events-none overflow-hidden">
          <img
            src={currentStage.backgroundUrl}
            alt="Scenic Background"
            className="w-full h-full object-cover object-center filter brightness-[0.70] contrast-[1.05] transition-all duration-700"
          />
          {/* Frosted glass & vignette dark veil */}
          <div className="absolute inset-0 bg-[#080A14]/80 backdrop-blur-[14px]" />
          {/* Subtle cartoon lighting blooms */}
          <div className="absolute -top-20 -right-20 w-96 h-96 rounded-full bg-apple-blue/15 blur-[120px] pointer-events-none" />
          <div className="absolute top-1/2 -left-20 w-96 h-96 rounded-full bg-apple-purple/15 blur-[130px] pointer-events-none" />
          <div className="absolute -bottom-20 right-1/4 w-80 h-80 rounded-full bg-apple-teal/15 blur-[110px] pointer-events-none" />
        </div>
      ) : (
        <div className="fixed inset-0 z-0 bg-gradient-to-b from-[#141728] via-[#090b14] to-[#040508]">
          <div className="absolute -top-20 -right-20 w-96 h-96 rounded-full bg-apple-blue/20 blur-[110px]" />
          <div className="absolute bottom-10 -left-10 w-96 h-96 rounded-full bg-apple-purple/20 blur-[120px]" />
        </div>
      )}

      {/* 2. Responsive Layout Container (Mobile Frame on Desktop) */}
      <div className="relative z-10 w-full min-h-screen flex flex-col lg:flex-row items-center lg:items-start justify-center lg:justify-between max-w-6xl mx-auto lg:px-8">
        {/* Main Stages Scroll Area */}
        <div className="w-full max-w-lg min-h-screen flex flex-col pb-16 lg:py-6">
          {/* Floating Apple Top Bar */}
          <StagesTimeline
            phaseGroups={phaseGroups}
            currentActiveStageId={currentStage?.id || null}
            onSelectStage={handleStageClick}
            onBack={() => navigate('/')}
            showHeaderBar={true}
            showChapterCard={true}
            totalStars={totalStars}
            maxStars={stats.maxStars}
            completedStages={stats.completedStagesCount}
            totalStages={stats.totalStages}
            progressPercentage={stats.progressPercentage}
            className="px-4 pt-3"
          />

          {/* Loading Indicator */}
          {isLoading && stages.length === 0 && (
            <div className="flex-1 flex flex-col items-center justify-center py-20 space-y-4">
              <div className="w-10 h-10 border-3 border-apple-blue border-t-transparent rounded-full animate-spin" />
              <span className="text-sm text-apple-gray-400">
                در حال بارگذاری مرحله‌های ماجراجویی…
              </span>
            </div>
          )}

          {/* Error View */}
          {error && stages.length === 0 && (
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center space-y-4">
              <AlertCircle className="w-12 h-12 text-apple-red/80" />
              <p className="text-sm text-apple-gray-300 max-w-xs">{error}</p>
              <button
                type="button"
                onClick={refreshStages}
                className="px-6 py-2.5 rounded-full bg-apple-blue text-white font-bold text-sm shadow-apple-sm active:scale-95 transition-all"
              >
                تلاش دوباره
              </button>
            </div>
          )}
        </div>

        {/* Desktop Companion Branding Showcase Panel (Matches screenshot 02) */}
        <div className="hidden lg:flex flex-col justify-center items-start max-w-md sticky top-24 self-start py-12 px-6 space-y-6">
          <div className="space-y-3">
            <div className="w-16 h-16 rounded-2xl bg-white/10 border border-white/20 p-2.5 shadow-apple-md backdrop-blur-md flex items-center justify-center">
              <img
                src="/assets/icons/app-icon.png"
                alt="AI Speaking"
                className="w-full h-full object-contain"
                onError={(e) => {
                  (e.target as HTMLElement).style.display = 'none';
                }}
              />
              <span className="font-extrabold text-2xl text-transparent bg-clip-text bg-gradient-to-r from-apple-blue to-apple-teal">
                AI
              </span>
            </div>
            <h2 className="text-3xl font-extrabold text-white tracking-tight">AI Speaking Plus</h2>
            <p className="text-base text-slate-300 leading-relaxed font-sans">
              مکالمه انگلیسی را در یک ماجراجویی واقعی تمرین کن؛ با هوش مصنوعی صحبت کن و ستاره جمع
              کن.
            </p>
          </div>

          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full apple-glass-pill text-xs text-sky-300 border border-sky-400/30">
            <span className="w-2 h-2 rounded-full bg-apple-blue animate-pulse" />
            <span>برای بهترین تجربه، روی موبایل باز کن</span>
          </div>
        </div>
      </div>

      {/* 3. Floating Apple Toast Notification (Bottom Center) */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div
            initial={{ opacity: 0, y: 24, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.95 }}
            transition={{ type: 'spring', stiffness: 450, damping: 25 }}
            className="fixed bottom-6 z-50 px-4 w-full max-w-sm pointer-events-none select-none"
          >
            <div className="p-3.5 rounded-2xl apple-glass-card bg-[#1c1c24]/90 border border-apple-red/40 text-center shadow-apple-lg">
              <span className="text-xs text-slate-100 font-medium leading-relaxed">
                {toastMessage}
              </span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 4. Login Dialog Modal */}
      <LoginDialog
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={() => {
          showToast('ورود با موفقیت انجام شد!');
          refreshStages();
        }}
      />
    </div>
  );
};

export default StagesPage;
