import React from 'react';
import { Menu, LogOut, User, Shield, Sun, Moon } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { Badge } from '../ui/Badge';

interface TopBarProps {
  onToggleSidebar: () => void;
}

export const AdminTopBar: React.FC<TopBarProps> = ({ onToggleSidebar }) => {
  const { admin, logout } = useAuth();
  const { resolvedTheme, toggleTheme } = useTheme();

  const isSuperAdmin = admin?.role === 'ROLE_SUPER_ADMIN';

  return (
    <header className="sticky top-0 z-30 h-16 bg-white/90 dark:bg-[#0E1422]/90 backdrop-blur-xl border-b border-slate-200 dark:border-slate-800/80 px-4 sm:px-6 flex items-center justify-between">
      {/* Mobile Toggle & Brand Context */}
      <div className="flex items-center gap-3">
        <button
          onClick={onToggleSidebar}
          className="lg:hidden text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800/60 transition-colors"
          aria-label="باز کردن منو"
        >
          <Menu className="w-5 h-5" />
        </button>
        <div className="hidden sm:flex items-center gap-2 text-xs sm:text-sm text-slate-500 dark:text-slate-400 font-medium">
          <span className="w-2 h-2 rounded-full bg-indigo-500" />
          <span>سامانه مکالمه هوشمند زبان انگلیسی &bull; مرکز کنترل ادمین</span>
        </div>
      </div>

      {/* Admin Profile & Actions */}
      <div className="flex items-center gap-2.5 sm:gap-3">
        {/* Light / Dark Mode Toggle */}
        <button
          onClick={toggleTheme}
          title={resolvedTheme === 'dark' ? 'تغییر به حالت روشن (Light Mode)' : 'تغییر به حالت تاریک (Dark Mode)'}
          className="p-2 rounded-xl text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800/70 border border-slate-200 dark:border-slate-800 transition-all duration-150 shadow-xs"
          aria-label="تغییر تم"
        >
          {resolvedTheme === 'dark' ? (
            <Sun className="w-4 h-4 text-amber-400 animate-in spin-in-90 duration-300" />
          ) : (
            <Moon className="w-4 h-4 text-indigo-600 animate-in spin-in-90 duration-300" />
          )}
        </button>

        {/* Role Badge */}
        {admin && (
          <Badge
            variant={isSuperAdmin ? 'purple' : 'info'}
            size="sm"
            className="hidden md:inline-flex"
          >
            <Shield className="w-3.5 h-3.5 ml-1" />
            {isSuperAdmin ? 'مدیر ارشد' : 'مدیر سیستم'}
          </Badge>
        )}

        {/* Profile Card */}
        <div className="flex items-center gap-2.5 bg-slate-50 dark:bg-[#0B0F17]/80 py-1 px-3 rounded-xl border border-slate-200 dark:border-slate-800/90 shadow-xs">
          <div className="w-8 h-8 rounded-lg bg-indigo-50 dark:bg-indigo-500/20 border border-indigo-200 dark:border-indigo-500/30 flex items-center justify-center text-indigo-600 dark:text-indigo-300 font-bold text-sm">
            {admin?.fullName ? admin.fullName.charAt(0) : <User className="w-4 h-4" />}
          </div>
          <div className="text-right hidden md:block">
            <p className="text-sm font-semibold text-slate-800 dark:text-slate-200 leading-tight">
              {admin?.fullName || admin?.username || 'کاربر مدیر'}
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 leading-tight font-mono" dir="ltr">
              {admin?.username}
            </p>
          </div>
        </div>

        {/* Logout Button */}
        <button
          onClick={logout}
          title="خروج از حساب"
          className="flex items-center gap-1.5 text-xs sm:text-sm font-medium text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 bg-rose-50 dark:bg-rose-500/10 hover:bg-rose-100 dark:hover:bg-rose-500/20 border border-rose-200 dark:border-rose-500/20 px-3 py-1.5 rounded-xl transition-all active:scale-[0.98]"
        >
          <LogOut className="w-4 h-4" />
          <span className="hidden sm:inline">خروج</span>
        </button>
      </div>
    </header>
  );
};
