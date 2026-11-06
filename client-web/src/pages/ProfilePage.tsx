import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import { RefreshCw, CheckCircle2, AlertCircle } from 'lucide-react';
import { CupertinoNavBar, CupertinoSkeleton } from '../components/cupertino';
import { ProfileHeaderCard } from '../components/profile/ProfileHeaderCard';
import { GuestBannerCard } from '../components/profile/GuestBannerCard';
import { SubscriptionCard } from '../components/profile/SubscriptionCard';
import { AchievementStatsCard } from '../components/profile/AchievementStatsCard';
import { ThemeAppearanceCard, ThemeMode } from '../components/profile/ThemeAppearanceCard';
import { SupportAndAboutCard } from '../components/profile/SupportAndAboutCard';
import { AppleSignOutButton } from '../components/profile/AppleSignOutButton';
import { AvatarPickerModal } from '../components/profile/AvatarPickerModal';
import { EditNickNameModal } from '../components/profile/EditNickNameModal';
import { LevelPickerModal } from '../components/profile/LevelPickerModal';
import { SupportModal } from '../components/profile/SupportModal';
import { SignOutConfirmModal } from '../components/profile/SignOutConfirmModal';
import { SubscriptionModal } from '../components/home/SubscriptionModal';
import { LoginDialog } from '../components/auth/LoginDialog';
import { useAuth } from '../hooks/useAuth';
import { useStages } from '../hooks/useStages';

export const ProfilePage: React.FC = () => {
  const navigate = useNavigate();

  // Auth & Stats Hooks
  const {
    profile,
    isLoggedIn,
    isLoadingProfile,
    updateProfile,
    logout,
    refreshProfile,
  } = useAuth();

  const { stats } = useStages();

  // Dialog State
  const [showAvatarPicker, setShowAvatarPicker] = useState(false);
  const [showEditName, setShowEditName] = useState(false);
  const [showLevelPicker, setShowLevelPicker] = useState(false);
  const [showSupportModal, setShowSupportModal] = useState(false);
  const [showSignOutConfirm, setShowSignOutConfirm] = useState(false);
  const [showSubscriptionModal, setShowSubscriptionModal] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);

  // Appearance & Preference Toggles
  const [themeMode, setThemeMode] = useState<ThemeMode>('dark');
  const [glassEffect, setGlassEffect] = useState(true);
  const [soundFeedback, setSoundFeedback] = useState(true);

  // Saving & Feedback
  const [isSaving, setIsSaving] = useState(false);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const showToast = (text: string, type: 'success' | 'error' = 'success') => {
    setToastMessage({ text, type });
    setTimeout(() => {
      setToastMessage((prev) => (prev?.text === text ? null : prev));
    }, 3500);
  };

  // Profile Update Handlers
  const handleSaveAvatar = async (avatarId: string) => {
    setIsSaving(true);
    try {
      await updateProfile({ avatar: avatarId });
      showToast('تصویر پروفایل با موفقیت به‌روزرسانی شد');
    } catch {
      showToast('خطا در به‌روزرسانی تصویر پروفایل', 'error');
    } finally {
      setIsSaving(false);
    }
  };

  const handleSaveNickName = async (newNickName: string) => {
    setIsSaving(true);
    try {
      await updateProfile({ nickName: newNickName });
      showToast('نام نمایشی با موفقیت تغییر کرد');
    } catch {
      showToast('خطا در ذخیره نام نمایشی', 'error');
    } finally {
      setIsSaving(false);
    }
  };

  const handleSaveLanguageLevel = async (newLevel: string) => {
    setIsSaving(true);
    try {
      await updateProfile({ languageLevel: newLevel });
      showToast(`سطح زبان انگلیسی به ${newLevel} تغییر یافت`);
    } catch {
      showToast('خطا در تغییر سطح زبان', 'error');
    } finally {
      setIsSaving(false);
    }
  };

  const handleConfirmSignOut = async () => {
    setShowSignOutConfirm(false);
    await logout();
    showToast('با موفقیت از حساب کاربری خارج شدید');
    navigate('/');
  };

  // Derive stats (prefer server profile if logged in, fallback to local stage stats for guests)
  const totalStars = isLoggedIn && profile ? profile.totalStars : stats.totalStars;
  const completedStages = isLoggedIn && profile ? profile.completedStagesCount : stats.completedStagesCount;
  const score = isLoggedIn && profile ? profile.score : totalStars * 50;
  const languageLevel = isLoggedIn && profile ? profile.languageLevel : 'A1';

  return (
    <div className="min-h-screen bg-apple-bg text-white flex flex-col font-sans select-none pb-12" dir="rtl">
      {/* 1. Cupertino Top App Bar with RTL Back Button (pointing right >) */}
      <CupertinoNavBar
        title="پروفایل کاربری"
        subtitle={isLoggedIn ? (profile?.nickName || 'کاربر متصل') : 'حالت مهمان'}
        onBack={() => navigate('/')}
        backLabel="بازگشت"
        endSlot={
          <button
            type="button"
            onClick={() => {
              refreshProfile();
              showToast('اطلاعات کاربر به‌روزرسانی شد');
            }}
            title="به‌روزرسانی اطلاعات"
            className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/15 border border-white/15 flex items-center justify-center text-white active:scale-90 transition-transform shadow-sm"
          >
            <RefreshCw className={`w-4 h-4 ${isLoadingProfile ? 'animate-spin text-apple-blue' : ''}`} />
          </button>
        }
      />

      {/* 2. Main Scrollable Container */}
      <main className="flex-1 w-full max-w-lg mx-auto p-4 space-y-4">
        {/* Loading Skeleton */}
        {isLoadingProfile && !profile ? (
          <div className="space-y-4">
            <CupertinoSkeleton className="w-full h-32 rounded-3xl" />
            <div className="grid grid-cols-2 gap-3">
              <CupertinoSkeleton className="h-24 rounded-2xl" />
              <CupertinoSkeleton className="h-24 rounded-2xl" />
              <CupertinoSkeleton className="h-24 rounded-2xl" />
              <CupertinoSkeleton className="h-24 rounded-2xl" />
            </div>
            <CupertinoSkeleton className="w-full h-28 rounded-2xl" />
          </div>
        ) : (
          <>
            {/* Profile Header (Avatar, Name, Phone/Guest status, CEFR pill) */}
            <ProfileHeaderCard
              profile={profile}
              isGuest={!isLoggedIn}
              onAvatarClick={() => {
                if (isLoggedIn) setShowAvatarPicker(true);
                else setShowLoginModal(true);
              }}
              onEditNameClick={() => setShowEditName(true)}
              onLevelClick={() => {
                if (isLoggedIn) setShowLevelPicker(true);
                else setShowLoginModal(true);
              }}
            />

            {/* Guest Banner Card (Shown only when unauthenticated) */}
            {!isLoggedIn && (
              <GuestBannerCard onLoginClick={() => setShowLoginModal(true)} />
            )}

            {/* Subscription Card */}
            <SubscriptionCard
              subscription={profile?.subscription}
              onUpgradeClick={() => navigate('/subscription')}
            />

            {/* Learning & Achievement Stats (2x2 Apple Squircles) */}
            <AchievementStatsCard
              totalStars={totalStars}
              score={score}
              languageLevel={languageLevel}
              completedStages={completedStages}
              totalStages={stats.totalStages || 30}
              onLevelClick={() => {
                if (isLoggedIn) setShowLevelPicker(true);
                else setShowLoginModal(true);
              }}
            />

            {/* Theme & Visual Appearance Settings */}
            <ThemeAppearanceCard
              themeMode={themeMode}
              onThemeModeChange={setThemeMode}
              glassEffect={glassEffect}
              onGlassEffectChange={setGlassEffect}
              soundFeedback={soundFeedback}
              onSoundFeedbackChange={setSoundFeedback}
            />

            {/* Support Channels & Version Info */}
            <SupportAndAboutCard
              onSupportClick={() => setShowSupportModal(true)}
              onIntroVideoClick={() => navigate('/intro')}
            />

            {/* Sign Out Button (Destructive Crimson, shown when logged in) */}
            {isLoggedIn && (
              <div className="pt-2">
                <AppleSignOutButton onClick={() => setShowSignOutConfirm(true)} />
              </div>
            )}
          </>
        )}
      </main>

      {/* Floating Apple Toast Notification */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div
            initial={{ opacity: 0, y: 30, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.95 }}
            transition={{ type: 'spring', damping: 22, stiffness: 350 }}
            className={`fixed bottom-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 px-4 py-3 rounded-2xl shadow-2xl backdrop-blur-xl border text-xs font-semibold max-w-sm ${
              toastMessage.type === 'error'
                ? 'bg-red-950/90 border-red-500/40 text-red-200'
                : 'bg-apple-surface-elevated/95 border-apple-border text-white shadow-apple-glow'
            }`}
          >
            {toastMessage.type === 'error' ? (
              <AlertCircle className="w-4 h-4 text-red-400 flex-shrink-0" />
            ) : (
              <CheckCircle2 className="w-4 h-4 text-apple-green flex-shrink-0" />
            )}
            <span>{toastMessage.text}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Interactive Modals */}
      <AvatarPickerModal
        isOpen={showAvatarPicker}
        currentAvatar={profile?.avatar || 'avatar_g1'}
        isSaving={isSaving}
        onSelect={handleSaveAvatar}
        onClose={() => setShowAvatarPicker(false)}
      />

      <EditNickNameModal
        isOpen={showEditName}
        currentNickName={profile?.nickName || ''}
        isSaving={isSaving}
        onSave={handleSaveNickName}
        onClose={() => setShowEditName(false)}
      />

      <LevelPickerModal
        isOpen={showLevelPicker}
        currentLevel={profile?.languageLevel}
        isSaving={isSaving}
        onSelect={handleSaveLanguageLevel}
        onClose={() => setShowLevelPicker(false)}
      />

      <SupportModal
        isOpen={showSupportModal}
        onClose={() => setShowSupportModal(false)}
      />

      <SignOutConfirmModal
        isOpen={showSignOutConfirm}
        onConfirm={handleConfirmSignOut}
        onClose={() => setShowSignOutConfirm(false)}
      />

      <SubscriptionModal
        isOpen={showSubscriptionModal}
        onClose={() => setShowSubscriptionModal(false)}
        onSubscribe={() => {
          setShowSubscriptionModal(false);
          navigate('/subscription');
        }}
      />

      <LoginDialog
        isOpen={showLoginModal}
        onClose={() => setShowLoginModal(false)}
        onSuccess={() => {
          setShowLoginModal(false);
          refreshProfile();
          showToast('ورود با موفقیت انجام شد');
        }}
      />
    </div>
  );
};
