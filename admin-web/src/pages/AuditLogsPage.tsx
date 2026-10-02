import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { AuditLogItem } from '../types';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import {
  History,
  Search,
  Filter,
  Code2,
  Clock,
  Shield,
  RefreshCw,
  Copy,
  Check,
} from 'lucide-react';

export const AuditLogsPage: React.FC = () => {
  const { error } = useToast();
  const [logs, setLogs] = useState<AuditLogItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [actionFilter, setActionFilter] = useState<string>('ALL');
  const [selectedLog, setSelectedLog] = useState<AuditLogItem | null>(null);
  const [copied, setCopied] = useState<boolean>(false);

  const fetchLogs = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.getAuditLogs(100);
      setLogs(data);
    } catch (err: any) {
      error(err.message || 'خطا در دریافت تاریخچه عملیات');
    } finally {
      setIsLoading(false);
    }
  }, [error]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  const handleCopyDetails = (json: string) => {
    navigator.clipboard.writeText(json);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const formatJson = (raw: string) => {
    try {
      return JSON.stringify(JSON.parse(raw), null, 2);
    } catch {
      return raw;
    }
  };

  const getActionBadgeVariant = (action: string) => {
    if (action.includes('CREATE') || action.includes('GRANT')) return 'success';
    if (action.includes('SUSPEND') || action.includes('DELETE') || action.includes('CANCEL')) return 'danger';
    if (action.includes('UPDATE')) return 'warning';
    return 'purple';
  };

  const filteredLogs = logs.filter((log) => {
    if (actionFilter !== 'ALL' && !log.action.includes(actionFilter)) {
      return false;
    }
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      log.action.toLowerCase().includes(q) ||
      log.targetType.toLowerCase().includes(q) ||
      log.targetId.toLowerCase().includes(q) ||
      (log.adminName && log.adminName.toLowerCase().includes(q)) ||
      log.detailsJson.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
              تاریخچه عملیات و لاگ‌های حسابرسی
            </h1>
            <Badge variant="purple" size="md">
              {logs.length} رویداد ثبت‌شده
            </Badge>
          </div>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            ثبت غیرقابل تغییر (Audit Trail) تمامی اقدامات ادمین‌ها در سیستم
          </p>
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={fetchLogs}
          isLoading={isLoading}
          icon={<RefreshCw className="w-4 h-4" />}
        >
          تازه‌سازی
        </Button>
      </div>

      {/* Filter and Search */}
      <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-xs flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2 w-full md:w-auto overflow-x-auto pb-1 md:pb-0">
          <span className="text-xs font-semibold text-slate-400 dark:text-slate-500 ml-1 hidden sm:inline-flex items-center gap-1.5 uppercase tracking-wider">
            <Filter className="w-3.5 h-3.5 text-indigo-500" /> فیلتر:
          </span>
          {[
            { id: 'ALL', label: 'همه' },
            { id: 'STAGE', label: 'مراحل' },
            { id: 'USER', label: 'کاربران' },
            { id: 'SUBSCRIPTION', label: 'اشتراک‌ها' },
          ].map((item) => (
            <button
              key={item.id}
              onClick={() => setActionFilter(item.id)}
              className={`text-xs sm:text-sm px-3.5 py-1.5 rounded-xl font-medium transition-all ${
                actionFilter === item.id
                  ? 'bg-indigo-50 text-indigo-700 border border-indigo-200 shadow-xs dark:bg-indigo-500/15 dark:text-indigo-300 dark:border-indigo-500/30'
                  : 'bg-slate-100 dark:bg-[#0E1422] text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/70 dark:hover:bg-slate-800/60 border border-transparent'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>

        <div className="relative w-full md:w-80">
          <input
            type="text"
            placeholder="جستجو در لاگ‌ها، ادمین یا شناسه..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full text-sm rounded-xl bg-slate-50 dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 pl-9 pr-4 py-2 text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:ring-indigo-500/25 dark:focus:border-indigo-500 transition-all"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
        </div>
      </div>

      {/* Logs Table */}
      <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-xs overflow-hidden">
        {isLoading ? (
          <div className="flex flex-col items-center justify-center py-24 gap-3">
            <div className="w-8 h-8 border-3 border-indigo-500/20 border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin" />
            <p className="text-sm font-medium text-slate-500 dark:text-slate-400">در حال دریافت تاریخچه عملیات...</p>
          </div>
        ) : filteredLogs.length === 0 ? (
          <div className="text-center py-20 px-4">
            <div className="w-14 h-14 rounded-2xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 text-indigo-600 dark:text-indigo-400 flex items-center justify-center mx-auto mb-4 shadow-xs">
              <History className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-slate-800 dark:text-slate-200">لاگی با این مشخصات یافت نشد</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1 max-w-sm mx-auto">
              {searchQuery ? 'موردی با این عبارت جستجو یافت نشد.' : 'هنوز رکورد لاگی ثبت نشده است.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-right border-collapse">
              <thead>
                <tr className="border-b border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-[#0E1422]/90 text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  <th className="py-4 px-4">زمان ثبت</th>
                  <th className="py-4 px-4">مدیر اقدام‌کننده</th>
                  <th className="py-4 px-4">عنوان اقدام</th>
                  <th className="py-4 px-4">موجودیت و شناسه</th>
                  <th className="py-4 px-4 text-left">جزئیات فنی</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60 text-sm">
                {filteredLogs.map((log) => (
                  <tr
                    key={log.id}
                    className="hover:bg-slate-50/80 dark:hover:bg-[#151D30]/60 transition-colors"
                  >
                    {/* Timestamp */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="flex items-center gap-1.5 text-slate-700 dark:text-slate-300 font-mono tabular-nums text-xs sm:text-sm">
                        <Clock className="w-4 h-4 text-slate-400 shrink-0" />
                        <span>{new Date(log.createdAt).toLocaleDateString('fa-IR')}</span>
                        <span className="text-xs text-slate-400">
                          {new Date(log.createdAt).toLocaleTimeString('fa-IR', {
                            hour: '2-digit',
                            minute: '2-digit',
                            second: '2-digit',
                          })}
                        </span>
                      </div>
                    </td>

                    {/* Admin */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-lg bg-indigo-50 dark:bg-indigo-500/15 border border-indigo-200 dark:border-indigo-500/25 flex items-center justify-center text-indigo-700 dark:text-indigo-300 font-semibold text-xs shrink-0">
                          <Shield className="w-4 h-4" />
                        </div>
                        <div>
                          <div className="font-bold text-slate-900 dark:text-slate-200 text-sm">
                            {log.adminName || 'مدیر سیستم'}
                          </div>
                          {log.adminId && (
                            <span className="text-xs text-slate-400 font-mono" dir="ltr">
                              {log.adminId.substring(0, 8)}...
                            </span>
                          )}
                        </div>
                      </div>
                    </td>

                    {/* Action */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <Badge variant={getActionBadgeVariant(log.action)}>
                        {log.action}
                      </Badge>
                    </td>

                    {/* Target */}
                    <td className="py-4 px-4">
                      <div className="flex items-center gap-1.5 text-xs sm:text-sm">
                        <span className="font-semibold text-slate-800 dark:text-slate-300">{log.targetType}</span>
                        <span className="text-slate-400">&bull;</span>
                        <span className="font-mono text-slate-700 dark:text-slate-300 bg-slate-100 dark:bg-[#0E1422] px-2 py-0.5 rounded-md border border-slate-200 dark:border-slate-800 text-xs tabular-nums" dir="ltr">
                          {log.targetId}
                        </span>
                      </div>
                    </td>

                    {/* Details modal trigger */}
                    <td className="py-4 px-4 text-left">
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => setSelectedLog(log)}
                        icon={<Code2 className="w-4 h-4" />}
                      >
                        مشاهده JSON
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* JSON Details Inspector Modal */}
      {selectedLog && (
        <Modal
          isOpen={Boolean(selectedLog)}
          onClose={() => setSelectedLog(null)}
          title={`جزئیات لاگ: ${selectedLog.action}`}
          description={`ثبت شده در تاریخ ${new Date(selectedLog.createdAt).toLocaleString('fa-IR')}`}
          maxWidth="2xl"
        >
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-xs sm:text-sm text-slate-600 dark:text-slate-400">
                موجودیت هدف: <span className="text-slate-900 dark:text-slate-200 font-mono font-bold">{selectedLog.targetType} ({selectedLog.targetId})</span>
              </span>
              <button
                type="button"
                onClick={() => handleCopyDetails(formatJson(selectedLog.detailsJson))}
                className="text-xs sm:text-sm text-slate-700 dark:text-slate-300 hover:text-indigo-600 dark:hover:text-white p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800/60 flex items-center gap-1.5 border border-slate-200 dark:border-slate-700 transition-colors shadow-xs"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                <span>{copied ? 'کپی شد' : 'کپی JSON'}</span>
              </button>
            </div>

            <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 overflow-x-auto max-h-96 shadow-inner">
              <pre className="text-xs sm:text-sm font-mono text-emerald-400 leading-relaxed" dir="ltr">
                {formatJson(selectedLog.detailsJson)}
              </pre>
            </div>

            <div className="flex justify-end pt-2">
              <Button variant="ghost" onClick={() => setSelectedLog(null)}>
                بستن
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
