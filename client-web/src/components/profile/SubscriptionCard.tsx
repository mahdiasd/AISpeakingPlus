import React from 'react';
import { motion } from 'motion/react';
import { Crown, Sparkles, AlertCircle } from 'lucide-react';
import { SubscriptionSummary } from '../../types/user';

interface SubscriptionCardProps {
  subscription?: SubscriptionSummary | null;
  onUpgradeClick: () => void;
}

export const SubscriptionCard: React.FC<SubscriptionCardProps> = ({
  subscription,
  onUpgradeClick,
}) => {
  const isSubscriber = Boolean(subscription?.isSubscriber);
  const remainingDays = subscription?.remainingDays ?? 0;
  const isExpiringSoon = isSubscriber && remainingDays > 0 && remainingDays <= 3;

  if (isSubscriber) {
    return (
      <div className="relative overflow-hidden rounded-[24px] apple-glass-card p-4 shadow-apple-card">
        {/* Golden Specular Glow */}
        <div className="absolute top-0 right-0 w-36 h-36 bg-apple-yellow/15 rounded-full blur-2xl pointer-events-none" />

        <div className="relative z-10 space-y-3">
          <div className="flex items-center justify-between gap-3">
            {/* Right side in RTL: Crown squircle + Plan Details */}
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-apple-yellow/25 to-amber-500/20 border border-apple-yellow/50 flex items-center justify-center text-apple-yellow shadow-inner">
                <Crown className="w-6 h-6 fill-apple-yellow drop-shadow-[0_0_8px_rgba(255,204,0,0.6)]" />
              </div>

              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-sm font-bold text-white">
                    {subscription?.planTitleFa || 'اشتراک ویژه فعال'}
                  </h4>
                  <span className="px-2 py-0.5 rounded-full bg-apple-green/20 border border-apple-green/40 text-apple-green text-[10px] font-bold">
                    VIP
                  </span>
                </div>
                <p className="text-xs text-apple-gray-300 mt-0.5">
                  {remainingDays > 0
                    ? `${remainingDays.toLocaleString('fa-IR')} روز از دوره اشتراک شما باقی مانده است`
                    : 'دسترسی نامحدود به تمامی امکانات فعال است'}
                </p>
              </div>
            </div>

            {/* Left side in RTL: Action Button */}
            <motion.button
              type="button"
              onClick={onUpgradeClick}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.95 }}
              className="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/15 border border-white/20 text-xs font-semibold text-white whitespace-nowrap transition-colors"
            >
              تمدید
            </motion.button>
          </div>

          {/* Expiring Soon Alert */}
          {isExpiringSoon && (
            <div className="flex items-center justify-between gap-2 p-2.5 rounded-xl bg-red-500/15 border border-red-500/30 text-red-300 text-xs">
              <div className="flex items-center gap-1.5 font-medium">
                <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
                <span>اشتراک شما رو به پایان است!</span>
              </div>
              <motion.button
                type="button"
                onClick={onUpgradeClick}
                whileTap={{ scale: 0.95 }}
                className="px-2.5 py-1 rounded-lg bg-red-500/30 hover:bg-red-500/40 text-red-100 font-bold text-[11px] transition-colors"
              >
                تمدید سریع
              </motion.button>
            </div>
          )}
        </div>
      </div>
    );
  }

  // Free Plan Card (Matches Screenshot 07 & Compose SubscriptionCard)
  return (
    <div className="relative overflow-hidden rounded-[24px] apple-glass-card p-4 shadow-apple-card">
      <div className="relative z-10 flex items-center justify-between gap-4">
        {/* Right side in RTL: Crown icon + "طرح رایگان" + Subtitle */}
        <div className="flex-1 min-w-0 space-y-1.5">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-white/10 border border-white/15 flex items-center justify-center text-apple-gray-300 shrink-0">
              <Crown className="w-5 h-5 text-apple-gray-300" />
            </div>

            <div className="flex items-center gap-1.5">
              <h4 className="text-sm font-bold text-white">طرح رایگان</h4>
              <Sparkles className="w-3.5 h-3.5 text-apple-yellow" />
            </div>
          </div>

          <p className="text-[11px] text-apple-gray-300 leading-relaxed max-w-xs font-sans">
            دسترسی نامحدود به تمامی مراحل، سناریوها و مکالمه هوش مصنوعی
          </p>
        </div>

        {/* Left side in RTL: "خرید اشتراک" Button */}
        <motion.button
          type="button"
          onClick={onUpgradeClick}
          whileHover={{ scale: 1.04 }}
          whileTap={{ scale: 0.95 }}
          className="inline-flex items-center justify-center px-4 py-2.5 rounded-xl bg-gradient-to-r from-apple-yellow via-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-[0_4px_16px_rgba(255,204,0,0.3)] transition-all shrink-0"
        >
          خرید اشتراک
        </motion.button>
      </div>
    </div>
  );
};
