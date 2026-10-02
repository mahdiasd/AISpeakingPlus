import React, { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useTheme } from '../context/ThemeContext';
import { ShieldCheck, Mail, Lock, Eye, EyeOff, ArrowLeft, Sun, Moon } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export const LoginPage: React.FC = () => {
  const { login, isAuthenticated, isLoading } = useAuth();
  const { success, error } = useToast();
  const { resolvedTheme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const [username, setUsername] = useState('admin@aispeaking.ir');
  const [password, setPassword] = useState('Admin@123456!');
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formErrors, setFormErrors] = useState<{ username?: string; password?: string }>({});

  if (!isLoading && isAuthenticated) {
    return <Navigate to="/admin/dashboard" replace />;
  }

  const validate = (): boolean => {
    const errors: { username?: string; password?: string } = {};
    if (!username.trim()) {
      errors.username = 'لطفاً نام کاربری یا ایمیل را وارد کنید';
    }
    if (!password) {
      errors.password = 'لطفاً رمز عبور را وارد کنید';
    } else if (password.length < 6) {
      errors.password = 'رمز عبور باید حداقل ۶ نویسه باشد';
    }
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      await login(username.trim(), password);
      success('ورود به سامانه با موفقیت انجام شد');
      navigate('/admin/dashboard', { replace: true });
    } catch (err: any) {
      error(err.message || 'نام کاربری یا رمز عبور اشتباه است');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-[#0B0F17] flex flex-col justify-center items-center px-4 relative overflow-hidden transition-colors duration-150">
      {/* Theme toggle in corner */}
      <div className="absolute top-6 left-6 z-20">
        <button
          onClick={toggleTheme}
          title={resolvedTheme === 'dark' ? 'تغییر به حالت روشن' : 'تغییر به حالت تاریک'}
          className="p-2.5 rounded-xl bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white shadow-xs transition-colors"
        >
          {resolvedTheme === 'dark' ? (
            <Sun className="w-5 h-5 text-amber-400" />
          ) : (
            <Moon className="w-5 h-5 text-indigo-600" />
          )}
        </button>
      </div>

      {/* Subtle Ambient Lighting */}
      <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[36rem] h-[36rem] bg-indigo-500/5 dark:bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Login Card */}
      <div className="relative w-full max-w-md bg-white dark:bg-[#111726]/95 backdrop-blur-xl border border-slate-200 dark:border-slate-800 rounded-3xl p-8 sm:p-10 shadow-xl dark:shadow-[0_24px_60px_rgba(0,0,0,0.8),inset_0_1px_0_0_rgba(255,255,255,0.06)]">
        {/* Header / Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-indigo-50 dark:bg-indigo-500/15 border border-indigo-200 dark:border-indigo-500/30 shadow-xs mb-4 text-indigo-600 dark:text-indigo-400">
            <ShieldCheck className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
            پنل مدیریت AISpeakingPlus
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1.5">
            برای دسترسی به پنل مدیریت وارد حساب کاربری خود شوید
          </p>

          <div className="mt-4 p-3 rounded-xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 text-xs text-indigo-800 dark:text-indigo-300 text-center font-medium">
            اطلاعات ورود پیش‌فرض SuperAdmin جهت تست بارگذاری شده است.
          </div>
        </div>

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Input
              label="نام کاربری یا ایمیل"
              type="text"
              placeholder="admin@aispeaking.ir"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              error={formErrors.username}
              leftIcon={<Mail className="w-4 h-4" />}
              autoComplete="username"
              autoFocus
            />
          </div>

          <div>
            <div className="relative">
              <Input
                label="رمز عبور"
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                error={formErrors.password}
                leftIcon={<Lock className="w-4 h-4" />}
                rightIcon={
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="pointer-events-auto text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                }
                autoComplete="current-password"
              />
            </div>
          </div>

          <div className="pt-2">
            <Button
              type="submit"
              className="w-full py-2.5"
              size="lg"
              isLoading={isSubmitting}
              icon={<ArrowLeft className="w-4 h-4 rotate-180" />}
            >
              ورود به سامانه
            </Button>
          </div>
        </form>

        {/* Footer info */}
        <div className="mt-8 pt-6 border-t border-slate-200 dark:border-slate-800 text-center">
          <p className="text-xs text-slate-400 dark:text-slate-500 font-mono" dir="ltr">
            AISpeaking Core &bull; Secure Admin Portal v2.0
          </p>
        </div>
      </div>
    </div>
  );
};
