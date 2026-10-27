import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { DashboardStats, AuditLogItem } from '../types';
import { Button } from '../components/ui/Button';
import {
  Users,
  Layers,
  Crown,
  ArrowUpRight,
  Plus,
  History,
  Activity,
  Clock,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { admin } = useAuth();
  const navigate = useNavigate();
  const { error } = useToast();

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentLogs, setRecentLogs] = useState<AuditLogItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      setIsLoading(true);
      try {
        const [statsData, logsData] = await Promise.all([
          api.getDashboardStats(),
          api.getAuditLogs(6),
        ]);
        setStats(statsData);
        setRecentLogs(logsData);
      } catch (err: any) {
        error(err.message || 'خطا در دریافت اطلاعات داشبورد');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, [error]);

  const cards = [
    {
      title: 'کل زبان‌آموزان',
      value: stats?.totalUsers ?? '—',
      subtitle: `${stats?.activeUsers ?? 0} کاربر فعال`,
      icon: Users,
      tag: 'کاربران',
    },
    {
      title: 'اشتراک‌های ویژه فعال',
      value: stats?.activeSubscriptions ?? '—',
      subtitle: 'دسترسی صوتی نامحدود',
      icon: Crown,
      tag: 'اشتراک ویژه',
    },
    {
      title: 'مراحل مکالمه منتشر شده',
      value: stats?.publishedStages ?? '—',
      subtitle: `از مجموع ${stats?.totalStages ?? 0} سناریو`,
      icon: Layers,
      tag: 'محتوا',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="relative overflow-hidden rounded-2xl bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] p-5 sm:p-6 shadow-2xs">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-xl sm:text-2xl font-bold text-slate-900 dark:text-[#f1f3f5] tracking-tight">
              درود، {admin?.fullName || 'مدیر'}
            </h1>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <Button
              onClick={() => navigate('/admin/stages/new')}
              icon={<Plus className="w-4 h-4" />}
              size="md"
            >
              طراحی مرحله جدید
            </Button>
            <Button
              variant="secondary"
              onClick={() => navigate('/admin/users')}
              icon={<Users className="w-4 h-4" />}
              size="md"
            >
              فهرست کاربران
            </Button>
          </div>
        </div>
      </div>

      {/* KPI Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {cards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <div
              key={idx}
              className="relative rounded-2xl bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] p-5 shadow-2xs hover:border-slate-300 dark:hover:border-[#383b44] transition-all duration-150"
            >
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-[#9ca3af]">
                    {card.title}
                  </span>
                  <div className="text-2xl sm:text-3xl font-bold text-slate-900 dark:text-[#f1f3f5] mt-2 tracking-tight tabular-nums font-mono">
                    {isLoading ? (
                      <div className="w-16 h-8 bg-slate-200 dark:bg-[#282a30] animate-pulse rounded-md" />
                    ) : (
                      card.value
                    )}
                  </div>
                </div>
                <div className="w-10 h-10 rounded-xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-indigo-600 dark:text-indigo-400 shrink-0">
                  <Icon className="w-5 h-5" />
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 dark:border-[#2d3037] flex items-center justify-between text-xs text-slate-500 dark:text-[#9ca3af]">
                <span className="tabular-nums">{card.subtitle}</span>
                <span className="text-xs bg-slate-100 dark:bg-[#282a30] px-2 py-0.5 rounded-md text-slate-700 dark:text-[#d1d5db] font-medium border border-transparent dark:border-[#343741]">
                  {card.tag}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Two Column Section: Recent Audit Trail (8 cols) & Quick Shortcuts (4 cols) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Recent Audit Logs (8 cols) */}
        <div className="lg:col-span-8 bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-5 sm:p-6 shadow-2xs space-y-4">
          <div className="flex items-center justify-between pb-3.5 border-b border-slate-100 dark:border-[#2d3037]">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-indigo-600 dark:text-indigo-400">
                <History className="w-4 h-4" />
              </div>
              <h2 className="text-base font-bold text-slate-900 dark:text-[#f1f3f5] tracking-tight">
                آخرین وقایع حسابرسی
              </h2>
            </div>
            <Button
              size="sm"
              variant="ghost"
              onClick={() => navigate('/admin/audit-logs')}
              icon={<ArrowUpRight className="w-4 h-4" />}
            >
              مشاهده همه
            </Button>
          </div>

          {isLoading ? (
            <div className="py-16 flex justify-center">
              <div className="w-7 h-7 border-3 border-indigo-500/20 border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin" />
            </div>
          ) : recentLogs.length === 0 ? (
            <div className="text-center py-12 text-sm text-slate-500 dark:text-[#9ca3af]">
              هنوز رویدادی در تاریخچه عملیات ثبت نشده است.
            </div>
          ) : (
            <div className="divide-y divide-slate-100 dark:divide-[#2d3037]">
              {recentLogs.map((log) => (
                <div key={log.id} className="py-3 flex items-center justify-between gap-4 hover:bg-slate-50 dark:hover:bg-[#282a30] px-2.5 rounded-xl transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-lg bg-slate-100 dark:bg-[#24262b] border border-slate-200 dark:border-[#32353e] flex items-center justify-center text-indigo-600 dark:text-indigo-400 shrink-0">
                      <Activity className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-sm font-semibold text-slate-800 dark:text-[#e5e7eb]">
                        {log.action} &bull; <span className="text-slate-500 dark:text-[#9ca3af] font-normal">{log.targetType}</span>
                      </div>
                      <div className="text-xs text-slate-500 dark:text-[#9ca3af] mt-0.5">
                        توسط {log.adminName || 'مدیر سیستم'}
                      </div>
                    </div>
                  </div>

                  <div className="text-xs text-slate-500 dark:text-[#9ca3af] flex items-center gap-1 shrink-0 font-mono tabular-nums" dir="ltr">
                    <Clock className="w-3.5 h-3.5 text-slate-400" />
                    {new Date(log.createdAt).toLocaleTimeString('fa-IR', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Quick Links (4 cols) */}
        <div className="lg:col-span-4 space-y-4">
          <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-5 sm:p-6 shadow-2xs space-y-4">
            <h3 className="text-xs font-bold text-slate-500 dark:text-[#9ca3af] uppercase tracking-wider">
              میانبرهای پرکاربرد
            </h3>
            <div className="space-y-2.5">
              <button
                onClick={() => navigate('/admin/stages/new')}
                className="w-full flex items-center justify-between p-3.5 rounded-xl bg-slate-50 dark:bg-[#24262b] hover:bg-slate-100 dark:hover:bg-[#282a30] border border-slate-200 dark:border-[#32353e] transition-all text-sm font-medium text-slate-800 dark:text-[#f1f3f5] group text-right"
              >
                <div className="flex items-center gap-2.5">
                  <Plus className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                  <span>طراحی سناریو و مرحله مکالمه</span>
                </div>
                <ArrowUpRight className="w-4 h-4 text-slate-400 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors" />
              </button>

              <button
                onClick={() => navigate('/admin/users')}
                className="w-full flex items-center justify-between p-3.5 rounded-xl bg-slate-50 dark:bg-[#24262b] hover:bg-slate-100 dark:hover:bg-[#282a30] border border-slate-200 dark:border-[#32353e] transition-all text-sm font-medium text-slate-800 dark:text-[#f1f3f5] group text-right"
              >
                <div className="flex items-center gap-2.5">
                  <Users className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                  <span>مدیریت کاربران و اهدای اشتراک</span>
                </div>
                <ArrowUpRight className="w-4 h-4 text-slate-400 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors" />
              </button>

              <button
                onClick={() => navigate('/admin/audit-logs')}
                className="w-full flex items-center justify-between p-3.5 rounded-xl bg-slate-50 dark:bg-[#24262b] hover:bg-slate-100 dark:hover:bg-[#282a30] border border-slate-200 dark:border-[#32353e] transition-all text-sm font-medium text-slate-800 dark:text-[#f1f3f5] group text-right"
              >
                <div className="flex items-center gap-2.5">
                  <History className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                  <span>تاریخچه لاگ‌های امنیتی</span>
                </div>
                <ArrowUpRight className="w-4 h-4 text-slate-400 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
