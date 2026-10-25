import React, { useState } from 'react';
import { Plane, Hotel, Coffee, Briefcase, Play, Bot, User } from 'lucide-react';

interface LandingScenariosProps {
  onLaunchApp: () => void;
}

export const LandingScenarios: React.FC<LandingScenariosProps> = ({ onLaunchApp }) => {
  const scenarios = [
    {
      id: 'airport',
      title: 'کنترل گذرنامه و ورود به لندن',
      titleEn: 'London Heathrow Airport Immigration',
      icon: Plane,
      category: 'سفر و گردشگری',
      partner: 'Officer Davis (لهجه بریتیش)',
      goal: 'توضیح هدف از سفر، ارائه کارت ورود و هتل اقامت به افسر مرزی',
      dialogue: [
        {
          speaker: 'ai',
          name: 'Officer Davis',
          text: 'Good morning! Welcome to London. May I see your passport and landing card, please?',
        },
        {
          speaker: 'user',
          name: 'شما (مسافر)',
          text: 'Good morning officer. Here is my passport and the landing card. I am staying in London for a one-week conference.',
        },
        {
          speaker: 'ai',
          name: 'Officer Davis',
          text: 'Excellent. Where will you be staying during your visit?',
        },
      ],
    },
    {
      id: 'hotel',
      title: 'پذیرش و درخواست اتاق در هتل',
      titleEn: 'Boutique Hotel Front Desk',
      icon: Hotel,
      category: 'اقامت و رزرو',
      partner: 'Sarah (لهجه امریکن)',
      goal: 'اعلام شماره رزرو، درخواست اتاق آرام در طبقات بالا و گرفتن رمز وای‌فای',
      dialogue: [
        {
          speaker: 'ai',
          name: 'Sarah (Receptionist)',
          text: 'Hello, welcome to The Grand Hotel! How can I assist you this evening?',
        },
        {
          speaker: 'user',
          name: 'شما (مهمان)',
          text: 'Hi Sarah. I have a reservation under the name Mahdi. Could I please have a quiet room on a higher floor?',
        },
        {
          speaker: 'ai',
          name: 'Sarah (Receptionist)',
          text: 'Certainly! Let me check the available suites for you right away.',
        },
      ],
    },
    {
      id: 'cafe',
      title: 'سفارش در کافه شلوغ و مکالمه کوتاه',
      titleEn: 'Downtown Coffee Shop',
      icon: Coffee,
      category: 'زندگی روزمره',
      partner: 'Michael (لهجه امریکن)',
      goal: 'سفارش قهوه با شیر گیاهی، کیک و نحوه پرداخت',
      dialogue: [
        {
          speaker: 'ai',
          name: 'Michael (Barista)',
          text: 'Hey there! What can I get started for you today?',
        },
        {
          speaker: 'user',
          name: 'شما (مشتری)',
          text: 'Hi! Could I please get a medium oat milk cappuccino with extra foam, and one blueberry muffin to go?',
        },
        {
          speaker: 'ai',
          name: 'Michael (Barista)',
          text: 'You got it! Would you like that warmed up?',
        },
      ],
    },
    {
      id: 'interview',
      title: 'مصاحبه شغلی شرکت بین‌المللی',
      titleEn: 'Senior Software Job Interview',
      icon: Briefcase,
      category: 'کار و حرفه',
      partner: 'Emily (لهجه بریتیش رسمی)',
      goal: 'پاسخ به سوال درباره چالش‌های فنی و شیوه همکاری با تیم‌های توزیع‌شده',
      dialogue: [
        {
          speaker: 'ai',
          name: 'Emily (Interviewer)',
          text: 'Welcome to the interview. Could you tell me about a time you resolved a critical production issue under pressure?',
        },
        {
          speaker: 'user',
          name: 'شما (کارجو)',
          text: 'In my previous project, we faced a high-latency issue in the API gateway. I coordinated the root-cause analysis and rolled out a resilient fallback cache.',
        },
        {
          speaker: 'ai',
          name: 'Emily (Interviewer)',
          text: 'Impressive! How did you communicate that update to your non-technical stakeholders?',
        },
      ],
    },
  ];

  const [activeTab, setActiveTab] = useState(scenarios[0].id);
  const currentScenario = scenarios.find((s) => s.id === activeTab) || scenarios[0];
  const CurrentIcon = currentScenario.icon;

  return (
    <section id="scenarios" className="py-20 bg-[#0E1422]/50 border-y border-slate-800/80">
      <div className="max-w-7xl mx-auto px-5 sm:px-8">
        <div className="text-center max-w-3xl mx-auto mb-14">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-semibold mb-3">
            <span>تمرین موقعیت‌های واقعی زندگی</span>
          </div>
          <h2 className="text-2xl sm:text-4xl font-bold text-white mb-3">
            سناریوهای داستانی با کاراکترهای زنده
          </h2>
          <p className="text-slate-400 text-sm sm:text-base">
            مکالمه انگلیسی را در صحنه‌های واقعی تجربه کنید؛ با کاراکترهایی که می‌شنوند، درک می‌کنند و مثل انسان پاسخ می‌دهند.
          </p>
        </div>

        {/* Scenario Tabs */}
        <div className="flex items-center justify-start sm:justify-center gap-2 overflow-x-auto pb-4 mb-8 no-scrollbar">
          {scenarios.map((s) => {
            const TabIcon = s.icon;
            const isActive = s.id === activeTab;
            return (
              <button
                key={s.id}
                onClick={() => setActiveTab(s.id)}
                className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs sm:text-sm font-medium transition-all whitespace-nowrap cursor-pointer ${
                  isActive
                    ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30 border border-indigo-400/40'
                    : 'bg-[#111726] text-slate-400 hover:text-white border border-slate-800'
                }`}
              >
                <TabIcon className="w-4 h-4" />
                <span>{s.title}</span>
              </button>
            );
          })}
        </div>

        {/* Active Scenario Card Preview */}
        <div className="max-w-4xl mx-auto rounded-2xl bg-[#111726] border border-slate-800 p-6 sm:p-8 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-800/80">
            <div className="flex items-center gap-3.5">
              <div className="w-12 h-12 rounded-xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
                <CurrentIcon className="w-6 h-6" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-lg sm:text-xl font-bold text-white">
                    {currentScenario.title}
                  </h3>
                  <span className="text-[11px] font-mono text-cyan-400 px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20">
                    {currentScenario.category}
                  </span>
                </div>
                <p className="text-xs text-slate-400 mt-1 font-inter">
                  {currentScenario.titleEn} &bull; پارتنر:{' '}
                  <span className="text-slate-300 font-medium">{currentScenario.partner}</span>
                </p>
              </div>
            </div>

            <button
              onClick={onLaunchApp}
              className="inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-full bg-white text-black hover:bg-slate-100 font-medium text-xs sm:text-sm shadow-md transition-all hover:scale-105 cursor-pointer self-start sm:self-auto"
            >
              <Play className="w-3.5 h-3.5 fill-black text-black" />
              <span>شروع این سناریو در اپلیکیشن</span>
            </button>
          </div>

          {/* Dialogue Simulator Preview */}
          <div className="mt-6 space-y-3.5">
            <div className="text-xs text-slate-500 font-semibold mb-2">
              هدف سناریو: <span className="text-slate-400 font-normal">{currentScenario.goal}</span>
            </div>

            {currentScenario.dialogue.map((item, idx) => {
              const isAi = item.speaker === 'ai';
              return (
                <div
                  key={idx}
                  className={`flex items-start gap-3 p-3.5 rounded-xl border ${
                    isAi
                      ? 'bg-slate-900/60 border-slate-800/90 text-right'
                      : 'bg-indigo-950/25 border-indigo-500/30'
                  }`}
                >
                  <div
                    className={`w-7 h-7 rounded-lg flex items-center justify-center shrink-0 ${
                      isAi
                        ? 'bg-cyan-500/15 text-cyan-400'
                        : 'bg-indigo-500/20 text-indigo-300'
                    }`}
                  >
                    {isAi ? <Bot className="w-4 h-4" /> : <User className="w-4 h-4" />}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center justify-between text-[11px] text-slate-400 mb-1">
                      <span className="font-semibold text-slate-300">{item.name}</span>
                      <span className="font-mono text-[10px] text-slate-500">Live Voice</span>
                    </div>
                    <p className="text-sm text-slate-200 font-inter dir-ltr text-left leading-relaxed">
                      {item.text}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
};
