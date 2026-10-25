import React from 'react';
import { Play, Sparkles, Shield } from 'lucide-react';
import { Link } from 'react-router-dom';

interface LandingFooterProps {
  onLaunchApp: () => void;
}

export const LandingFooter: React.FC<LandingFooterProps> = ({ onLaunchApp }) => {
  return (
    <footer className="relative bg-[#070A10] border-t border-slate-800/80 pt-16 pb-12 overflow-hidden">
      <div className="max-w-7xl mx-auto px-5 sm:px-8">
        {/* Final CTA Box */}
        <div className="relative rounded-3xl p-8 sm:p-12 mb-16 overflow-hidden bg-gradient-to-r from-indigo-950/80 via-[#111726] to-cyan-950/60 border border-indigo-500/30 text-center shadow-2xl">
          <div className="absolute -top-24 -right-24 w-60 h-60 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-24 -left-24 w-60 h-60 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none" />

          <h3 className="text-2xl sm:text-4xl font-bold text-white mb-4">
            آماده‌اید ترس از صحبت کردن به انگلیسی را کنار بگذارید؟
          </h3>
          <p className="text-slate-300 text-sm sm:text-base max-w-2xl mx-auto mb-8 leading-relaxed">
            تنها با چند دقیقه تمرین روزانه با پارتنر هوش مصنوعی، روان‌تر، سریع‌تر و با اعتمادبه‌نفس بالا به زبان انگلیسی صحبت کنید.
          </p>

          <button
            onClick={onLaunchApp}
            className="px-8 py-3.5 bg-white hover:bg-slate-100 text-black font-bold text-base rounded-full shadow-xl shadow-black/50 hover:scale-105 transition-all duration-300 inline-flex items-center gap-2.5 cursor-pointer"
          >
            <Play className="w-4 h-4 fill-black text-black" />
            <span>شروع رایگان (مرحله اول و دوم)</span>
          </button>
        </div>

        {/* Links & Brand */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6 pb-8 border-b border-slate-800/60">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-indigo-600 to-cyan-400 p-[1px]">
              <div className="w-full h-full bg-[#0B0F17] rounded-[7px] flex items-center justify-center">
                <Sparkles className="w-4 h-4 text-cyan-300" />
              </div>
            </div>
            <div>
              <span className="font-inter font-bold text-lg text-white">AISpeakingPlus</span>
              <p className="text-[11px] text-slate-400">سامانه هوشمند تقویت مهارت مکالمه انگلیسی</p>
            </div>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-5 sm:gap-6 text-xs sm:text-sm text-slate-400">
            <a href="#features" className="hover:text-white transition-colors">امکانات</a>
            <a href="#scenarios" className="hover:text-white transition-colors">سناریوها</a>
            <a href="#voice" className="hover:text-white transition-colors">صداهای طبیعی</a>
            <a href="#pricing" className="hover:text-white transition-colors">تعرفه‌ها</a>
            <a href="#how-it-works" className="hover:text-white transition-colors">نحوه کار</a>
            <a href="#faq" className="hover:text-white transition-colors">پرسش‌های متداول</a>
            <button onClick={onLaunchApp} className="text-cyan-400 hover:text-cyan-300 transition-colors font-medium cursor-pointer">
              ورود به برنامه
            </button>
          </div>
        </div>

        {/* Bottom copyright & admin access */}
        <div className="pt-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <div>
            &copy; {new Date().getFullYear()} AISpeakingPlus &bull; تمامی حقوق محفوظ است.
          </div>
          <Link
            to="/admin/login"
            className="flex items-center gap-1.5 text-slate-600 hover:text-slate-400 transition-colors"
          >
            <Shield className="w-3.5 h-3.5" />
            <span>ورود مدیران سیستم</span>
          </Link>
        </div>
      </div>
    </footer>
  );
};
