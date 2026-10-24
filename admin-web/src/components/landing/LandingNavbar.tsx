import React, { useState, useEffect } from 'react';
import { Menu, X, Sparkles, ArrowLeft } from 'lucide-react';

interface LandingNavbarProps {
  onLaunchApp: () => void;
}

export const LandingNavbar: React.FC<LandingNavbarProps> = ({ onLaunchApp }) => {
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navLinks = [
    { label: 'امکانات پلتفرم', href: '#features' },
    { label: 'سناریوهای داستانی', href: '#scenarios' },
    { label: 'هوش صوتی Kokoro', href: '#voice' },
    { label: 'نحوه کار', href: '#how-it-works' },
    { label: 'پرسش‌های متداول', href: '#faq' },
  ];

  return (
    <header
      className={`fixed top-0 inset-x-0 z-50 transition-all duration-300 animate-fade-in-down ${
        isScrolled
          ? 'bg-[#0B0F17]/85 backdrop-blur-md border-b border-white/10 py-3.5 shadow-lg shadow-black/30'
          : 'bg-gradient-to-b from-black/60 to-transparent py-5'
      }`}
    >
      <div className="max-w-7xl mx-auto px-5 sm:px-8 flex items-center justify-between">
        {/* Logo */}
        <a href="#" className="flex items-center gap-2.5 group">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 p-[1px] shadow-sm shadow-indigo-500/30">
            <div className="w-full h-full bg-[#0B0F17] rounded-[7px] flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-cyan-300 group-hover:scale-110 transition-transform" />
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="font-inter font-bold text-lg sm:text-xl text-white tracking-tight">
              AISpeaking<span className="text-cyan-400">Plus</span>
            </span>
            <span className="text-[10px] font-inter font-semibold px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-400/30">
              AI VOICE
            </span>
          </div>
        </a>

        {/* Desktop Menu Items */}
        <nav className="hidden md:flex items-center gap-7">
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              className="text-[14px] font-normal text-white/90 hover:text-white transition-colors duration-200"
            >
              {link.label}
            </a>
          ))}
        </nav>

        {/* Action Button & Hamburger */}
        <div className="flex items-center gap-3">
          <button
            onClick={onLaunchApp}
            className="hidden sm:inline-flex items-center gap-2 border border-white/80 text-white hover:bg-white hover:text-black transition-all duration-300 font-normal text-[14px] px-5 py-2 rounded-full cursor-pointer shadow-sm group"
          >
            <span>ورود به برنامه</span>
            <ArrowLeft className="w-3.5 h-3.5 group-hover:-translate-x-1 transition-transform" />
          </button>

          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 text-white/90 hover:text-white rounded-lg bg-white/5 border border-white/10"
            aria-label="منوی موبایل"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-[#0B0F17]/95 backdrop-blur-xl border-b border-white/10 px-6 py-5 space-y-4 animate-fade-in-down">
          <nav className="flex flex-col space-y-3">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                onClick={() => setMobileMenuOpen(false)}
                className="text-[15px] font-normal text-white/90 hover:text-white py-1.5 border-b border-white/5"
              >
                {link.label}
              </a>
            ))}
          </nav>

          <button
            onClick={() => {
              setMobileMenuOpen(false);
              onLaunchApp();
            }}
            className="w-full mt-2 flex items-center justify-center gap-2 bg-white text-black font-medium text-[15px] py-2.5 rounded-xl cursor-pointer shadow-md hover:bg-slate-100 transition-colors"
          >
            <span>ورود به اپلیکیشن و شروع مکالمه</span>
            <ArrowLeft className="w-4 h-4" />
          </button>
        </div>
      )}
    </header>
  );
};
