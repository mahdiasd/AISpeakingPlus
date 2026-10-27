import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Layers,
  Users,
  History,
  ShieldCheck,
  AudioWaveform,
  TicketPercent,
  Bell,
  X,
} from 'lucide-react';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AdminSidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const navItems = [
    {
      to: '/admin/dashboard',
      label: 'داشبورد آماری',
      icon: LayoutDashboard,
    },
    {
      to: '/admin/stages',
      label: 'مراحل مکالمه',
      icon: Layers,
    },
    {
      to: '/admin/voice-lab',
      label: 'آزمایشگاه صوت (TTS & STT)',
      icon: AudioWaveform,
    },
    {
      to: '/admin/notifications',
      label: 'ارسال نوتیفیکیشن',
      icon: Bell,
    },
    {
      to: '/admin/users',
      label: 'مدیریت کاربران',
      icon: Users,
    },
    {
      to: '/admin/promo-codes',
      label: 'کدهای تخفیف',
      icon: TicketPercent,
    },
    {
      to: '/admin/audit-logs',
      label: 'تاریخچه عملیات',
      icon: History,
    },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden transition-opacity"
          onClick={onClose}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed top-0 bottom-0 right-0 z-40 w-64 bg-white/95 dark:bg-[#1b1c1f] backdrop-blur-xl border-l border-slate-200 dark:border-[#2b2d33] flex flex-col transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="flex items-center justify-between h-16 px-5 border-b border-slate-200 dark:border-[#2b2d33]">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-indigo-600 dark:text-indigo-400 shadow-2xs">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-bold text-slate-900 dark:text-slate-100 tracking-tight text-base">
                  AISpeaking
                </span>
                <span className="text-[11px] font-semibold bg-slate-100 text-slate-700 dark:bg-[#282a30] dark:text-[#f1f3f5] px-1.5 py-0.5 rounded border border-slate-200 dark:border-[#383b44]">
                  Plus
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-[#9ca3af]">پنل مدیریت مرکزی</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="lg:hidden text-slate-400 hover:text-slate-700 dark:hover:text-white p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-[#282a30] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Menu */}
        <div className="flex-1 px-3 py-6 space-y-1.5 overflow-y-auto">
          <div className="px-3 pb-2 text-xs font-semibold uppercase tracking-wider text-slate-400 dark:text-[#858a94] select-none">
            بخش‌های اصلی
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm transition-all duration-150 group select-none ${
                    isActive
                      ? 'bg-slate-100 text-slate-900 dark:bg-[#282a30] dark:text-[#f1f3f5] font-semibold border-r-2 border-indigo-600 dark:border-indigo-400 shadow-2xs'
                      : 'text-slate-600 dark:text-[#9ca3af] hover:text-slate-900 dark:hover:text-[#f1f3f5] hover:bg-slate-100/70 dark:hover:bg-[#24262b] border-r-2 border-transparent font-medium'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    <Icon
                      className={`w-5 h-5 transition-colors shrink-0 ${
                        isActive
                          ? 'text-indigo-600 dark:text-indigo-400'
                          : 'text-slate-400 dark:text-[#858a94] group-hover:text-slate-600 dark:group-hover:text-slate-300'
                      }`}
                    />
                    <span className="truncate">{item.label}</span>
                  </>
                )}
              </NavLink>
            );
          })}
        </div>

        {/* Sidebar Footer */}
        <div className="p-4 border-t border-slate-200 dark:border-[#2b2d33]">
          <div className="flex items-center justify-between text-xs text-slate-500 dark:text-[#858a94] px-1">
            <span>AISpeaking Plus</span>
            <span className="font-mono text-[11px] bg-slate-100 dark:bg-[#24262b] px-2 py-0.5 rounded border border-slate-200 dark:border-[#32353e]">v1.0.0</span>
          </div>
        </div>
      </aside>
    </>
  );
};
