import React from 'react';
import { motion } from 'motion/react';
import { MessageCircleQuestion, ChevronLeft, Info, ExternalLink, Play } from 'lucide-react';

interface SupportAndAboutCardProps {
  onSupportClick: () => void;
  onIntroVideoClick?: () => void;
}

export const SupportAndAboutCard: React.FC<SupportAndAboutCardProps> = ({
  onSupportClick,
  onIntroVideoClick,
}) => {
  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2 px-1">
        <Info className="w-4 h-4 text-apple-blue" />
        <h3 className="text-sm font-bold text-white">پشتیبانی و اطلاعات</h3>
      </div>

      <div className="rounded-3xl bg-apple-surface border border-apple-border overflow-hidden divide-y divide-white/5 shadow-sm backdrop-blur-xl">
        {/* Intro Video Button */}
        {onIntroVideoClick && (
          <motion.button
            type="button"
            onClick={onIntroVideoClick}
            whileTap={{ scale: 0.98 }}
            className="w-full flex items-center justify-between p-4 hover:bg-white/5 transition-colors text-right"
          >
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-apple-purple/15 border border-apple-purple/30 flex items-center justify-center text-apple-purple">
                <Play className="w-5 h-5 fill-apple-purple/30" />
              </div>
              <div>
                <p className="text-xs font-semibold text-white">مشاهده ویدیوی معرفی اپلیکیشن</p>
                <p className="text-[11px] text-apple-gray-400">راهنمای شروع و آشنایی با مراحل</p>
              </div>
            </div>
            <ChevronLeft className="w-4 h-4 text-apple-gray-400" />
          </motion.button>
        )}

        {/* Support Channel Button */}
        <motion.button
          type="button"
          onClick={onSupportClick}
          whileTap={{ scale: 0.98 }}
          className="w-full flex items-center justify-between p-4 hover:bg-white/5 transition-colors text-right"
        >
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-apple-blue/15 border border-apple-blue/30 flex items-center justify-center text-apple-blue">
              <MessageCircleQuestion className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-semibold text-white">ارتباط با پشتیبانی و راهنما</p>
              <p className="text-[11px] text-apple-gray-400">پاسخگویی سریع در تلگرام و روبیکا</p>
            </div>
          </div>
          <ChevronLeft className="w-4 h-4 text-apple-gray-400" />
        </motion.button>

        {/* App Version Row */}
        <div className="flex items-center justify-between p-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-white/10 border border-white/15 flex items-center justify-center text-apple-gray-300">
              <ExternalLink className="w-4 h-4" />
            </div>
            <div>
              <p className="text-xs font-semibold text-white">نسخه اپلیکیشن</p>
              <p className="text-[11px] text-apple-gray-400">AI Speaking Plus Web 2.0 (Cupertino)</p>
            </div>
          </div>
          <span className="px-2.5 py-1 rounded-full bg-white/10 border border-white/15 text-apple-gray-300 text-xs font-mono font-semibold">
            v2.0.0
          </span>
        </div>
      </div>
    </div>
  );
};
