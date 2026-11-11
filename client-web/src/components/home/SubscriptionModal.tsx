import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Crown, CheckCircle2, Sparkles, MessageSquare, Headphones, ArrowLeft } from 'lucide-react';
import { motion } from 'motion/react';
import { CupertinoModal } from '../cupertino';

export interface SubscriptionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubscribe?: () => void;
}

export const SubscriptionModal: React.FC<SubscriptionModalProps> = ({
  isOpen,
  onClose,
  onSubscribe,
}) => {
  const navigate = useNavigate();

  const perks = [
    {
      icon: <Sparkles className="w-4 h-4 text-apple-yellow" />,
      title: 'دسترسی نامحدود به تمامی ۳۰ مرحله',
      desc: 'آزادسازی سناریوهای حرفه‌ای در فرودگاه، دانشگاه، بانک و جلسات کاری در لندن',
    },
    {
      icon: <MessageSquare className="w-4 h-4 text-apple-purple" />,
      title: 'مکالمه بدون وقفه با هوش مصنوعی',
      desc: 'بدون محدودیت دور مکالمه به همراه اصلاح گرامری بلادرنگ',
    },
    {
      icon: <Headphones className="w-4 h-4 text-apple-blue" />,
      title: 'تصحیح لهجه و تلفظ بومی',
      desc: 'شنیدن تلفظ با لهجه استاندارد بریتانیایی و بازخورد هوشمند جملات',
    },
  ];

  const handleProceed = () => {
    onClose();
    if (onSubscribe) {
      onSubscribe();
    } else {
      navigate('/subscription');
    }
  };

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={onClose}
      title="اشتراک ویژه (VIP)"
      subtitle="دسترسی نامحدود به تمامی مراحل و مکالمه بدون وقفه با هوش مصنوعی"
    >
      <div className="space-y-4 text-right" dir="rtl">
        {/* Golden Hero Crown Badge */}
        <div className="relative p-4 rounded-[22px] apple-glass bg-gradient-to-br from-apple-yellow/20 via-amber-500/10 to-transparent border border-apple-yellow/40 space-y-2 overflow-hidden shadow-apple-sm">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-apple-yellow to-amber-500 flex items-center justify-center text-black shrink-0 shadow-[0_0_16px_rgba(255,204,0,0.4)]">
              <Crown className="w-6 h-6 fill-black text-black" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">عضویت ویژه طلایی (VIP)</h3>
              <span className="text-[11px] text-apple-yellow font-semibold">
                کلید تسلط روان بر مکالمه انگلیسی با جدیدترین مدل‌های هوش مصنوعی
              </span>
            </div>
          </div>
        </div>

        {/* Benefits List */}
        <div className="space-y-2.5">
          {perks.map((perk, i) => (
            <div
              key={i}
              className="flex items-start gap-3 p-3 rounded-2xl apple-glass border border-white/10"
            >
              <div className="p-2 rounded-xl bg-white/10 shrink-0">{perk.icon}</div>
              <div className="space-y-0.5">
                <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-apple-green shrink-0" />
                  <span>{perk.title}</span>
                </h4>
                <p className="text-[11px] text-apple-gray-300 leading-relaxed font-sans">{perk.desc}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Highlighted Popular Plan */}
        <div className="p-3.5 rounded-2xl apple-glass-card border border-apple-yellow/50 flex items-center justify-between">
          <div className="space-y-0.5">
            <span className="text-xs text-apple-gray-300 block">طرح پیشنهادی ۳ ماهه:</span>
            <div className="flex items-baseline gap-1.5 font-mono">
              <span className="text-xs text-apple-gray-400 line-through font-sans">
                {(299000).toLocaleString('fa-IR')}
              </span>
              <span className="text-base font-extrabold text-apple-yellow">
                {(115000).toLocaleString('fa-IR')}
              </span>
              <span className="text-xs text-apple-gray-300 font-sans">تومان</span>
            </div>
          </div>

          <span className="px-2.5 py-1 rounded-full bg-apple-yellow/20 border border-apple-yellow/40 text-apple-yellow text-[11px] font-bold">
            محبوب‌ترین (۶۲٪ تخفیف)
          </span>
        </div>

        {/* Subscribe CTA Button */}
        <div className="pt-2">
          <motion.button
            type="button"
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.97 }}
            onClick={handleProceed}
            className="w-full inline-flex items-center justify-center gap-2 py-3 px-4 rounded-2xl bg-gradient-to-r from-apple-yellow via-amber-400 to-amber-500 text-black font-extrabold text-sm shadow-[0_4px_20px_rgba(255,204,0,0.35)] transition-all"
          >
            <Crown className="w-4 h-4 fill-black text-black" />
            <span>مشاهده همه طرح‌ها و خرید اشتراک</span>
            <ArrowLeft className="w-4 h-4 text-black mr-1" />
          </motion.button>
        </div>
      </div>
    </CupertinoModal>
  );
};
