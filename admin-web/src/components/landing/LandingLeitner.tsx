import React, { useState } from 'react';
import { BookOpen, Layers, Volume2, Plus, Check, BellRing, Sparkles } from 'lucide-react';

interface WordSample {
  id: string;
  word: string;
  phonetic: string;
  pos: string;
  meaningFa: string;
  exampleEn: string;
  exampleFa: string;
  boxLabel: string;
}

const WORD_SAMPLES: WordSample[] = [
  {
    id: 'beverage',
    word: 'beverage',
    phonetic: "/'bevərɪdʒ/",
    pos: 'اسم',
    meaningFa: 'نوشیدنی',
    exampleEn: 'Would you like a hot beverage with your meal?',
    exampleFa: 'کنار غذات نوشیدنی گرم می‌خوای؟',
    boxLabel: 'جعبه ۱ • مرور فردا',
  },
  {
    id: 'reservation',
    word: 'reservation',
    phonetic: "/ˌrezər'veɪʃn/",
    pos: 'اسم',
    meaningFa: 'رزرو، پیش‌خرید',
    exampleEn: 'I have a room reservation under my name.',
    exampleFa: 'من یک رزرو اتاق به نام خودم دارم.',
    boxLabel: 'جعبه ۲ • مرور ۳ روز دیگر',
  },
  {
    id: 'flatmate',
    word: 'flatmate',
    phonetic: "/'flætmeɪt/",
    pos: 'اسم',
    meaningFa: 'هم‌خانه، هم‌اتاقی',
    exampleEn: 'My new flatmate is really friendly and helpful.',
    exampleFa: 'هم‌اتاقی جدیدم خیلی صمیمی و خوش‌برخورد است.',
    boxLabel: 'جعبه ۳ • مرور ۷ روز دیگر',
  },
];

const LEITNER_BOXES = [
  { box: '۱', interval: '۱ روز', active: true },
  { box: '۲', interval: '۳ روز', active: true },
  { box: '۳', interval: '۷ روز', active: false },
  { box: '۴', interval: '۱۴ روز', active: false },
  { box: '۵', interval: '۳۰ روز', active: false },
];

export const LandingLeitner: React.FC = () => {
  const [selectedId, setSelectedId] = useState<string>(WORD_SAMPLES[0].id);
  const [addedIds, setAddedIds] = useState<Record<string, boolean>>({ beverage: true });

  const activeSample = WORD_SAMPLES.find((item) => item.id === selectedId) ?? WORD_SAMPLES[0];
  const isAdded = Boolean(addedIds[activeSample.id]);

  const handleToggleAdd = () => {
    setAddedIds((prev) => ({ ...prev, [activeSample.id]: !prev[activeSample.id] }));
  };

  return (
    <section
      id="leitner"
      className="scroll-mt-24 py-20 sm:py-24 max-w-7xl mx-auto px-5 sm:px-8 [content-visibility:auto] [contain-intrinsic-size:640px]"
    >
      <div className="flex flex-col lg:flex-row items-center justify-between gap-10 sm:gap-14">
        {/* Right/Text Column in RTL */}
        <div className="lg:w-1/2">
          <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full apple-pill text-cyan-300 text-xs font-semibold mb-4">
            <Layers className="w-3.5 h-3.5 text-cyan-400" />
            <span>دیکشنری وسط مکالمه + جعبه لایتنر</span>
          </div>

          <h2 className="text-2xl sm:text-4xl font-extrabold text-white mb-5 leading-tight">
            کلمه‌های تازه رو همون‌جا یاد بگیر و <span className="text-cyan-300">دیگه فراموش نکن</span>
          </h2>

          <p className="text-slate-400 text-sm sm:text-base leading-relaxed mb-6">
            وسط گفتگو هر وقت به یک کلمه یا عبارت جدید خوردی، لازم نیست بری سراغ دیکشنری‌های دیگه. همون لحظه روش تپ کن تا معنیش رو تو همون جمله ببینی، تلفظش رو بشنوی و بفرستیش تو جعبه لایتنر ۵ خانه‌ای خودت.
          </p>

          <div className="space-y-3.5">
            <div className="flex items-start gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold shrink-0 mt-0.5">
                ✓
              </div>
              <span>معنی دقیق فارسی متناسب با جمله، همراه با فونتیک و پخش تلفظ صوتی</span>
            </div>
            <div className="flex items-start gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold shrink-0 mt-0.5">
                ✓
              </div>
              <span>مرور زمان‌بندی‌شده در ۵ جعبه (۱، ۳، ۷، ۱۴ و ۳۰ روز) با امکان افزودن دستی کلمه</span>
            </div>
            <div className="flex items-start gap-3 text-sm text-slate-300">
              <div className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 flex items-center justify-center text-xs font-bold shrink-0 mt-0.5">
                ✓
              </div>
              <span>یادآور روزانه برای این‌که وقت مرور کارت‌های امروزت یادت نره</span>
            </div>
          </div>
        </div>

        {/* Interactive Preview Card */}
        <div className="lg:w-1/2 w-full">
          <div className="rounded-3xl apple-glass p-5 sm:p-7 shadow-2xl space-y-5">
            {/* Word Selector Pills */}
            <div>
              <div className="flex items-center justify-between text-xs text-slate-400 mb-3">
                <span className="flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
                  <span>روی یک کلمه تپ کن تا پیش‌نمایش رو ببینی:</span>
                </span>
              </div>
              <div className="flex flex-wrap gap-2 dir-ltr">
                {WORD_SAMPLES.map((item) => {
                  const isSelected = item.id === activeSample.id;
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => setSelectedId(item.id)}
                      className={`px-3.5 py-1.5 rounded-full text-xs sm:text-sm font-inter font-semibold transition-all cursor-pointer ${
                        isSelected
                          ? 'bg-cyan-400 text-slate-950 shadow-md shadow-cyan-500/25'
                          : 'bg-white/5 text-slate-300 border border-white/10 hover:bg-white/10'
                      }`}
                    >
                      {item.word}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Dictionary Card */}
            <div className="rounded-2xl bg-[#0B0F17]/75 border border-white/10 p-4 sm:p-5 space-y-3.5">
              <div className="flex items-start justify-between gap-3">
                <div className="text-left dir-ltr">
                  <div className="flex items-center gap-2">
                    <span className="text-lg sm:text-xl font-inter font-extrabold text-white">
                      {activeSample.word}
                    </span>
                    <span className="text-[11px] px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                      {activeSample.pos}
                    </span>
                  </div>
                  <div className="text-xs font-mono text-slate-400 mt-1 flex items-center gap-2">
                    <span>{activeSample.phonetic}</span>
                    <Volume2 className="w-3.5 h-3.5 text-cyan-400" />
                  </div>
                </div>

                <button
                  type="button"
                  onClick={handleToggleAdd}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer shrink-0 ${
                    isAdded
                      ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                      : 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 hover:bg-cyan-500/30'
                  }`}
                >
                  {isAdded ? <Check className="w-3.5 h-3.5" /> : <Plus className="w-3.5 h-3.5" />}
                  <span>{isAdded ? 'تو لایتنر ثبت شد' : 'افزودن به لایتنر'}</span>
                </button>
              </div>

              <div className="pt-2 border-t border-white/10">
                <div className="text-sm font-bold text-cyan-300 mb-1.5">
                  معنی: {activeSample.meaningFa}
                </div>
                <p className="text-xs font-inter text-slate-300 dir-ltr text-left">
                  {activeSample.exampleEn}
                </p>
                <p className="text-xs text-slate-400 mt-1">{activeSample.exampleFa}</p>
              </div>
            </div>

            {/* 5 Leitner Boxes Progress */}
            <div className="space-y-2.5">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span className="flex items-center gap-1.5 font-medium text-slate-300">
                  <BookOpen className="w-3.5 h-3.5 text-indigo-400" />
                  <span>۵ جعبه مرور لایتنر</span>
                </span>
                <span className="flex items-center gap-1 text-[11px] text-slate-400">
                  <BellRing className="w-3 h-3 text-amber-400" />
                  <span>{activeSample.boxLabel}</span>
                </span>
              </div>

              <div className="grid grid-cols-5 gap-2">
                {LEITNER_BOXES.map((item) => (
                  <div
                    key={item.box}
                    className={`rounded-xl p-2 text-center border transition-colors ${
                      item.active
                        ? 'bg-indigo-500/15 border-indigo-400/40 text-white'
                        : 'bg-white/[0.03] border-white/10 text-slate-400'
                    }`}
                  >
                    <div className="text-xs font-bold">جعبه {item.box}</div>
                    <div className="text-[10px] opacity-80 mt-0.5">{item.interval}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
