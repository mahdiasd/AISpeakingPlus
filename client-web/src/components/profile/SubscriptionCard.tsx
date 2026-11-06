import React from 'react';
import { motion } from 'motion/react';
import { Crown, Sparkles, ChevronLeft, Zap, CheckCircle2 } from 'lucide-react';
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

  if (isSubscriber) {
    return (
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-amber-950/40 via-apple-surface to-apple-surface border border-apple-yellow/40 p-4 shadow-apple-card backdrop-blur-xl">
        <div className="flex items-center justify-between gap-3">
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
                {subscription?.remainingDays != null
                  ? `${subscription.remainingDays} روز از دوره اشتراک شما باقی مانده است`
                  : 'دسترسی نامحدود به تمامی امکانات فعال است'}
              </p>
            </div>
          </div>

          <motion.button
            type="button"
            onClick={onUpgradeClick}
            whileHover={{ scale: 1.03 }}
            whileTap={{ scale: 0.95 }}
            className="px-3.5 py-1.5 rounded-xl bg-white/10 hover:bg-white/15 border border-white/20 text-xs font-semibold text-white whitespace-nowrap transition-colors"
          >
            تمدید
          </motion.button>
        </div>
      </div>
    );
  }

  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-amber-500/10 via-apple-surface to-apple-surface border border-amber-500/30 p-4 shadow-apple-card backdrop-blur-xl">
      {/* Golden Specular Glow */}
      <div className="absolute -top-12 -left-12 w-36 h-36 bg-amber-500/15 rounded-full blur-2xl pointer-events-none" />

      <div className="relative z-10 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-start gap-3">
          <div className="w-11 h-11 rounded-2xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400 flex-shrink-0 mt-0.5">
            <Crown className="w-6 h-6 drop-shadow-[0_0_8px_rgba(251,191,36,0.6)]" />
          </div>

          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <h4 className="text-sm font-bold text-white">ارتقا به اشتراک ویژه (VIP)</h4>
              <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            </div>
            <p className="text-xs text-apple-gray-300 leading-relaxed max-w-sm">
              دسترسی نامحدود به ۳۰ مرحله داستانی، سناریوهای پیشرفته و بازخورد اختصاصی هوش مصنوعی.
            </p>
            <div className="flex items-center gap-3 pt-1 text-[11px] text-apple-gray-400">
              <span className="flex items-center gap-1">
                <CheckCircle2 className="w-3 h-3 text-apple-green" /> بدون وقفه
              </span>
              <span className="flex items-center gap-1">
                <CheckCircle2 className="w-3 h-3 text-apple-green" /> تمرین تلفظ هوشمند
              </span>
            </div>
          </div>
        </div>

        <motion.button
          type="button"
          onClick={onUpgradeClick}
          whileHover={{ scale: 1.03 }}
          whileTap={{ scale: 0.96 }}
          className="w-full sm:w-auto inline-flex items-center justify-center gap-1.5 px-4 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-yellow-500 text-black font-bold text-xs shadow-apple-glow active:brightness-95 transition-all flex-shrink-0"
        >
          <Zap className="w-3.5 h-3.5 fill-black" />
          <span>فعال‌سازی اشتراک ویژه</span>
          <ChevronLeft className="w-3.5 h-3.5" />
        </motion.button>
      </div>
    </div>
  );
};
