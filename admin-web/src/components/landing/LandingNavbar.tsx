import React, { useState, useEffect } from 'react';
import { Menu, X, ArrowLeft } from 'lucide-react';

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
    { label: 'امکانات', href: '#features' },
    { label: 'صداهای طبیعی', href: '#voice' },
    { label: 'تعرفه‌ها', href: '#pricing' },
    { label: 'نحوه کار', href: '#how-it-works' },
    { label: 'پرسش‌های متداول', href: '#faq' },
  ];

  return (
    <header
      className={`fixed top-0 inset-x-0 z-50 transition-all duration-300 ${
        isScrolled
          ? 'bg-[#0B0F17]/75 backdrop-blur-2xl border-b border-white/10 py-3 shadow-[0_8px_32px_rgba(0,0,0,0.4),inset_0_1px_0_rgba(255,255,255,0.1)]'
          : 'bg-gradient-to-b from-black/80 via-black/30 to-transparent py-4 sm:py-5'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-8 flex items-center justify-between">
        {/* Logo */}
        <a href="#" className="flex items-center gap-2.5 group">
          <img
            src="/logo.png"
            alt="AISpeaking"
            className="w-8 h-8 sm:w-9 sm:h-9 rounded-xl object-contain shadow-sm group-hover:scale-105 transition-transform"
          />
          <div className="flex items-center">
            <span className="font-inter font-extrabold text-lg sm:text-xl text-white tracking-tight">
              AISpeaking
            </span>
          </div>
        </a>

        {/* Desktop Menu Items */}
        <nav className="hidden lg:flex items-center gap-7">
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              className="text-[14px] font-medium text-white/80 hover:text-white transition-colors duration-200"
            >
              {link.label}
            </a>
          ))}
        </nav>

        {/* Action Button & Hamburger */}
        <div className="flex items-center gap-3">
          <button
            onClick={onLaunchApp}
            className="hidden sm:inline-flex items-center gap-2 apple-btn-primary font-bold text-[13px] sm:text-[14px] px-5 py-2 rounded-full cursor-pointer group"
          >
            <span>ورود و شروع رایگان</span>
            <ArrowLeft className="w-3.5 h-3.5 group-hover:-translate-x-0.5 transition-transform" />
          </button>

          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="lg:hidden p-2 text-white/90 hover:text-white rounded-xl bg-white/10 border border-white/15 backdrop-blur-md active:scale-95 transition-transform"
            aria-label="منوی سایت"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="lg:hidden bg-[#0B0F17]/95 backdrop-blur-3xl border-b border-white/10 px-6 py-5 space-y-4 shadow-2xl">
          <nav className="flex flex-col space-y-1">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                onClick={() => setMobileMenuOpen(false)}
                className="text-[15px] font-medium text-white/90 hover:text-white py-2.5 border-b border-white/5"
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
            className="w-full mt-2 flex items-center justify-center gap-2 apple-btn-primary font-bold text-[15px] py-3 rounded-2xl cursor-pointer"
          >
            <span>ورود به برنامه (دو مرحله اول رایگان)</span>
            <ArrowLeft className="w-4 h-4" />
          </button>
        </div>
      )}
    </header>
  );
};
