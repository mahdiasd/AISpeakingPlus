import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Mic,
  Sparkles,
  Shield,
  Layers,
  ArrowLeft,
  Headphones,
  Award,
} from 'lucide-react';
import { Button } from '../components/ui/Button';

export const AppHomePage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[#0B0F17] text-slate-100 flex flex-col selection:bg-indigo-500 selection:text-white relative overflow-x-hidden">
      {/* Background Ambient Glows */}
      <div className="absolute top-0 right-1/4 w-[32rem] h-[32rem] bg-indigo-500/5 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute top-1/3 left-10 w-[28rem] h-[28rem] bg-indigo-500/5 rounded-full blur-3xl pointer-events-none" />

      {/* Navigation Header */}
      <header className="sticky top-0 z-30 bg-[#0B0F17]/80 backdrop-blur-xl border-b border-slate-800/80 px-6 py-3.5 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-b from-indigo-500 to-indigo-700 flex items-center justify-center text-white shadow-sm shadow-indigo-500/20 border border-indigo-400/20">
            <Mic className="w-4 h-4 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-semibold text-sm tracking-tight text-white">AISpeaking</span>
              <span className="text-[10px] font-mono font-medium bg-indigo-500/10 text-indigo-400 px-1.5 py-0.5 rounded border border-indigo-500/20">
                PRO
              </span>
            </div>
            <p className="text-[11px] text-slate-400">سامانه آموزش و ارزیابی مکالمه زبان انگلیسی</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate('/admin')}
            icon={<Shield className="w-3.5 h-3.5 text-indigo-400" />}
          >
            ورود به پنل مدیریت
          </Button>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 py-16 sm:py-24 max-w-5xl mx-auto text-center z-10">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-950/30 border border-indigo-500/20 text-indigo-300 text-xs font-medium mb-6">
          <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
          <span>نسل جدید تمرین مکالمه بر پایه سناریوهای داستانی و واقع‌گرایانه</span>
        </div>

        <h1 className="text-3xl sm:text-5xl lg:text-6xl font-black text-slate-100 tracking-tight leading-tight max-w-3xl mb-6">
          با هوش مصنوعی صحبت کن، <span className="bg-gradient-to-r from-indigo-400 via-indigo-300 to-slate-200 bg-clip-text text-transparent">روان و بااعتمادبه‌نفس</span> انگلیسی یاد بگیر!
        </h1>

        <p className="text-sm sm:text-base text-slate-400 max-w-2xl mb-10 leading-relaxed font-normal">
          سامانه هوشمند AISpeakingPlus با مدل‌های صوتی فوق‌طبیعی Kokoro TTS و موتور ارزیابی تلفظ و معنا، کاربر را در موقعیت‌های طبیعی روزمره هدایت می‌کند.
        </p>

        {/* Feature Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 w-full text-right my-6">
          <div className="p-5 rounded-2xl bg-[#111726] border border-slate-800/80 shadow-sm space-y-2 hover:border-slate-700/80 transition-all">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-3">
              <Headphones className="w-4 h-4" />
            </div>
            <h3 className="text-sm font-semibold text-slate-100">صدای فوق‌طبیعی Kokoro TTS</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              مکالمه با ۱۱ کاراکتر صوتی با لهجه‌های اصیل آمریکایی و بریتانیایی بدون تأخیر و وقفه شبکه.
            </p>
          </div>

          <div className="p-5 rounded-2xl bg-[#111726] border border-slate-800/80 shadow-sm space-y-2 hover:border-slate-700/80 transition-all">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-3">
              <Layers className="w-4 h-4" />
            </div>
            <h3 className="text-sm font-semibold text-slate-100">مراحل داستانی گام‌به‌گام</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              ماموریت‌های زنده و سناریوهای هدفمند مکالمه که در پنل مدیریت طراحی، تست و به‌روزرسانی می‌شوند.
            </p>
          </div>

          <div className="p-5 rounded-2xl bg-[#111726] border border-slate-800/80 shadow-sm space-y-2 hover:border-slate-700/80 transition-all">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-3">
              <Award className="w-4 h-4" />
            </div>
            <h3 className="text-sm font-semibold text-slate-100">بازخورد و امتیازدهی بلادرنگ</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              ارزیابی تلفظ، گرامر و صحت ادای جملات بلافاصله پس از اتمام هر دور مکالمه صوتی کاربر.
            </p>
          </div>
        </div>

        {/* Action Button to Admin or Launch */}
        <div className="pt-6 flex flex-col sm:flex-row items-center justify-center gap-4">
          <Button
            size="lg"
            onClick={() => navigate('/admin')}
            icon={<ArrowLeft className="w-4 h-4 rotate-180" />}
          >
            ورود به پنل مدیریت (/admin)
          </Button>
        </div>
      </main>

      {/* Footer */}
      <footer className="py-6 border-t border-slate-800/80 text-center text-xs text-slate-500">
        AISpeakingPlus &bull; سامانه مکالمه هوشمند زبان انگلیسی
      </footer>
    </div>
  );
};
