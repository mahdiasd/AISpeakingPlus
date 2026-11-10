import React from 'react';
import { Play, Shield } from 'lucide-react';
import { Link } from 'react-router-dom';

interface LandingFooterProps {
  onLaunchApp: () => void;
}

export const LandingFooter: React.FC<LandingFooterProps> = ({ onLaunchApp }) => {
  return (
    <footer className="relative bg-[#070A10] border-t border-slate-800/80 pt-16 pb-12 overflow-hidden">
      <div className="max-w-7xl mx-auto px-5 sm:px-8">
        {/* Final CTA Box */}
        <div className="relative rounded-3xl p-8 sm:p-12 mb-16 overflow-hidden apple-glass text-center shadow-2xl">
          <div className="absolute -top-24 -right-24 w-60 h-60 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-24 -left-24 w-60 h-60 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none" />

          <h3 className="text-2xl sm:text-4xl font-extrabold text-white mb-4">
            آماده‌اید ترس از صحبت کردن به انگلیسی را کنار بگذارید؟
          </h3>
          <p className="text-slate-300 text-sm sm:text-base max-w-2xl mx-auto mb-8 leading-relaxed">
            تنها با چند دقیقه تمرین روزانه با پارتنر هوش مصنوعی، روان‌تر، سریع‌تر و با اعتمادبه‌نفس بالا به زبان انگلیسی صحبت کنید.
          </p>

          <button
            onClick={onLaunchApp}
            className="apple-btn-primary px-9 py-4 font-extrabold text-base rounded-full inline-flex items-center gap-2.5 cursor-pointer shadow-xl"
          >
            <Play className="w-4 h-4 fill-black text-black" />
            <span>شروع رایگان (مرحله اول و دوم)</span>
          </button>
        </div>

        {/* Links & Brand */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6 pb-8 border-b border-white/10">
          <div className="flex items-center gap-3">
            <img
              src="/logo.png"
              alt="AISpeaking"
              className="w-8 h-8 rounded-xl object-contain shadow-sm"
            />
            <div>
              <span className="font-inter font-extrabold text-lg text-white">AISpeaking</span>
              <p className="text-[11px] text-slate-400">سامانه هوشمند تقویت مهارت مکالمه انگلیسی</p>
            </div>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-5 sm:gap-6 text-xs sm:text-sm text-slate-400">
            <a href="#features" className="hover:text-white transition-colors">امکانات</a>
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
            &copy; {new Date().getFullYear()} AISpeaking &bull; تمامی حقوق محفوظ است.
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
