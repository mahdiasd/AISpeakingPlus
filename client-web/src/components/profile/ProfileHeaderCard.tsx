import React from 'react';
import { motion } from 'motion/react';
import { Camera, Edit3, Smartphone, Sparkles } from 'lucide-react';
import { UserProfile } from '../../types/user';
import { getAvatarUrl } from '../../utils/avatar';
import { getCefrInfo } from '../../utils/cefr';

interface ProfileHeaderCardProps {
  profile: UserProfile | null;
  isGuest: boolean;
  onAvatarClick: () => void;
  onEditNameClick: () => void;
  onLevelClick: () => void;
}

export const ProfileHeaderCard: React.FC<ProfileHeaderCardProps> = ({
  profile,
  isGuest,
  onAvatarClick,
  onEditNameClick,
  onLevelClick,
}) => {
  const avatarUrl = getAvatarUrl(profile?.avatar);
  const displayName = profile?.nickName || (isGuest ? 'زبان‌آموز مهمان' : profile?.phoneNumber || 'زبان‌آموز مهمان');
  const cefr = getCefrInfo(profile?.languageLevel);

  return (
    <div className="relative overflow-hidden rounded-[26px] apple-glass-card p-5 shadow-apple-card">
      {/* Specular Ambient Glow */}
      <div className="absolute -top-16 -right-16 w-48 h-48 bg-apple-blue/15 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-16 -left-16 w-48 h-48 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="relative z-10 flex items-center justify-between gap-4">
        {/* Right side in RTL: Avatar with Camera Edit Pill */}
        <div className="relative flex-shrink-0">
          <motion.button
            type="button"
            onClick={onAvatarClick}
            whileHover={{ scale: 1.04 }}
            whileTap={{ scale: 0.94 }}
            className="relative block w-20 h-20 rounded-2xl overflow-hidden bg-gradient-to-br from-white/10 to-white/5 border-2 border-white/20 shadow-apple-glow group focus:outline-none"
            title="تغییر تصویر پروفایل"
          >
            <img
              src={avatarUrl}
              alt={displayName}
              className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
            />
            {/* Camera Overlay on Hover */}
            <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
              <Camera className="w-5 h-5 text-white drop-shadow" />
            </div>
          </motion.button>

          {/* Edit Badge Floating Corner */}
          <button
            type="button"
            onClick={onAvatarClick}
            aria-label="ویرایش عکس"
            className="absolute -bottom-1 -left-1 w-7 h-7 rounded-full bg-apple-blue text-white flex items-center justify-center border-2 border-[#0F1015] shadow-md active:scale-90 transition-transform"
          >
            <Camera className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Info Column (Left in RTL) */}
        <div className="flex-1 min-w-0 space-y-2">
          {/* Name & Tactile Edit Button */}
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-bold text-white truncate max-w-[190px]" title={displayName}>
              {displayName}
            </h2>
            <motion.button
              type="button"
              onClick={onEditNameClick}
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              className="w-7 h-7 rounded-full bg-white/10 hover:bg-white/15 border border-white/15 flex items-center justify-center text-apple-gray-300 hover:text-white transition-colors"
              title="ویرایش نام"
            >
              <Edit3 className="w-3.5 h-3.5" />
            </motion.button>
          </div>

          {/* Status Pills: Phone / Guest + CEFR Level */}
          <div className="flex flex-wrap items-center gap-2">
            {isGuest ? (
              <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-apple-yellow/15 border border-apple-yellow/30 text-apple-yellow text-xs font-medium">
                <span className="w-1.5 h-1.5 rounded-full bg-apple-yellow animate-pulse" />
                <span>حالت مهمان (موقتی)</span>
              </div>
            ) : (
              <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-white/10 border border-white/15 text-apple-gray-200 text-xs font-mono">
                <Smartphone className="w-3 h-3 text-apple-blue" />
                <span dir="ltr">{profile?.phoneNumber}</span>
                <span className="w-1.5 h-1.5 rounded-full bg-apple-green" />
              </div>
            )}

            {/* CEFR Level Pill */}
            <motion.button
              type="button"
              onClick={onLevelClick}
              whileTap={{ scale: 0.95 }}
              className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full ${cefr.badgeBg} border ${cefr.badgeBorder} ${cefr.badgeText} text-xs font-semibold hover:brightness-110 transition-all`}
              title="تغییر سطح زبان انگلیسی"
            >
              <Sparkles className="w-3 h-3" />
              <span>سطح {cefr.code}</span>
            </motion.button>
          </div>
        </div>
      </div>
    </div>
  );
};
