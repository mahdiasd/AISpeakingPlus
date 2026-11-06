import React from 'react';
import { Crown, CheckCircle2, Zap, Sparkles, MessageSquare, Headphones } from 'lucide-react';
import { CupertinoModal, CupertinoButton } from '../cupertino';

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
  const perks = [
    {
      icon: <Sparkles className="w-4 h-4 text-apple-yellow" />,
      title: 'دسترسی کامل به تمام ۳۰ مرحله',
      desc: 'آزادسازی سناریوهای حرفه‌ای در فرودگاه، دانشگاه، بانک و جلسات کاری در لندن',
    },
    {
      icon: <MessageSquare className="w-4 h-4 text-apple-blue" />,
      title: 'مکالمه زنده و نامحدود هوش مصنوعی',
      desc: 'بدون محدودیت دور مکالمه به همراه اصلاح گرامری در لحظه',
    },
    {
      icon: <Headphones className="w-4 h-4 text-apple-green" />,
      title: 'تلفظ فوق طبیعی بومی (Native British Audio)',
      desc: 'شنیدن تلفظ با لهجه بریتیش با دقت صوتی بالا و تحلیل اکسنت شما',
    },
    {
      icon: <Zap className="w-4 h-4 text-apple-purple" />,
      title: 'امتیازدهی و تحلیل نقاط ضعف',
      desc: 'کارنامه دقیق تلفظ، واژگان و ساختار گرامری پس از پایان هر مرحله',
    },
  ];

  return (
    <CupertinoModal
      isOpen={isOpen}
      onClose={onClose}
      title="ارتقا به اشتراک ویژه (VIP)"
      subtitle="قفل مراحل پیشرفته را باز کنید و مهارت مکالمه خود را ارتقا دهید"
    >
      <div className="space-y-4">
        {/* Banner */}
        <div className="relative p-4 rounded-2xl bg-gradient-to-br from-apple-yellow/20 via-apple-orange/15 to-transparent border border-apple-yellow/30 space-y-2 overflow-hidden">
          <div className="flex items-center gap-2">
            <div className="w-9 h-9 rounded-full bg-apple-yellow/20 flex items-center justify-center text-apple-yellow">
              <Crown className="w-5 h-5 fill-apple-yellow text-apple-yellow drop-shadow-[0_0_8px_rgba(255,204,0,0.6)]" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">اشتراک کامل مکالمه AI</h3>
              <span className="text-[11px] text-apple-yellow font-semibold">
                طرح ویژه یادگیری سریع انگلیسی
              </span>
            </div>
          </div>
        </div>

        {/* Perks List */}
        <div className="space-y-2.5">
          {perks.map((perk, i) => (
            <div
              key={i}
              className="flex items-start gap-3 p-3 rounded-2xl bg-white/5 border border-white/10"
            >
              <div className="p-2 rounded-xl bg-white/10 shrink-0">{perk.icon}</div>
              <div className="space-y-0.5">
                <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-apple-green" />
                  <span>{perk.title}</span>
                </h4>
                <p className="text-[11px] text-apple-gray-400 leading-relaxed">{perk.desc}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Pricing Plan Card */}
        <div className="p-3.5 rounded-2xl bg-white/5 border border-apple-yellow/40 flex items-center justify-between">
          <div className="space-y-0.5">
            <span className="text-xs text-apple-gray-400 block">طرح یک‌ماهه نامحدود:</span>
            <span className="text-base font-extrabold text-apple-yellow font-mono">
              ۱۸۹,۰۰۰ تومان
            </span>
          </div>

          <span className="px-2.5 py-1 rounded-full bg-apple-yellow/20 text-apple-yellow text-[11px] font-bold">
            ۵۰٪ تخفیف افتتاحیه
          </span>
        </div>

        {/* Subscribe CTA */}
        <div className="pt-2">
          <CupertinoButton
            variant="filled"
            size="lg"
            fullWidth
            icon={<Crown className="w-4 h-4 fill-white" />}
            onClick={() => {
              onClose();
              if (onSubscribe) onSubscribe();
              else alert('به زودی به درگاه پرداخت متصل می‌شود.');
            }}
          >
            فعال‌سازی اشتراک ویژه
          </CupertinoButton>
        </div>
      </div>
    </CupertinoModal>
  );
};
