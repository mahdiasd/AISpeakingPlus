import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { UserItem } from '../types';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { UserDetailModal } from '../components/users/UserDetailModal';
import { GrantSubscriptionModal } from '../components/users/GrantSubscriptionModal';
import {
  Users,
  Search,
  Filter,
  Trophy,
  Gift,
  Eye,
  ChevronLeft,
  ChevronRight,
  Crown,
} from 'lucide-react';

// Normalize Persian and Arabic numerals to English digits
const normalizeDigits = (str: string): string => {
  return str
    .replace(/[۰-۹]/g, (d) => String.fromCharCode(d.charCodeAt(0) - 1728))
    .replace(/[٠-٩]/g, (d) => String.fromCharCode(d.charCodeAt(0) - 1584));
};

export const UsersPage: React.FC = () => {
  const { error } = useToast();

  const [users, setUsers] = useState<UserItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalItems, setTotalItems] = useState<number>(0);
  const pageSize = 20;

  // Selected user for details or grant
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState<boolean>(false);
  const [grantModalUser, setGrantModalUser] = useState<UserItem | null>(null);

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    try {
      const normalizedSearch = normalizeDigits(searchQuery.trim());
      const res = await api.getUsers(currentPage, pageSize, normalizedSearch, statusFilter);
      setUsers(res.users);
      setTotalPages(res.totalPages);
      setTotalItems(res.totalItems);
    } catch (err: any) {
      error(err.message || 'خطا در بارگذاری لیست کاربران');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, searchQuery, statusFilter, error]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // Handle Search Input with digit normalization
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    setCurrentPage(1);
  };

  const handleStatusFilterChange = (st: string) => {
    setStatusFilter(st);
    setCurrentPage(1);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
              مدیریت کاربران و زبان‌آموزان
            </h1>
            <Badge variant="purple" size="md">
              {totalItems} کاربر ثبت‌شده
            </Badge>
          </div>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            جستجوی شماره همراه (پشتیبانی از ارقام فارسی)، تعلیق و اهدای اشتراک
          </p>
        </div>
      </div>

      {/* Filter and Search Toolbar */}
      <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-xs flex flex-col md:flex-row items-center justify-between gap-4">
        {/* Status Filter Buttons */}
        <div className="flex items-center gap-2 w-full md:w-auto overflow-x-auto pb-1 md:pb-0">
          <span className="text-xs font-semibold text-slate-400 dark:text-slate-500 ml-1 hidden sm:inline-flex items-center gap-1.5 uppercase tracking-wider">
            <Filter className="w-3.5 h-3.5 text-indigo-500" /> وضعیت:
          </span>
          {[
            { id: 'ALL', label: 'همه کاربران' },
            { id: 'ACTIVE', label: 'فعال' },
            { id: 'SUSPENDED', label: 'تعلیق شده' },
          ].map((item) => (
            <button
              key={item.id}
              onClick={() => handleStatusFilterChange(item.id)}
              className={`text-xs sm:text-sm px-3.5 py-1.5 rounded-xl font-medium transition-all ${
                statusFilter === item.id
                  ? 'bg-indigo-50 text-indigo-700 border border-indigo-200 shadow-xs dark:bg-indigo-500/15 dark:text-indigo-300 dark:border-indigo-500/30'
                  : 'bg-slate-100 dark:bg-[#0E1422] text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/70 dark:hover:bg-slate-800/60 border border-transparent'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>

        {/* Search Input Box */}
        <div className="relative w-full md:w-80">
          <input
            type="text"
            placeholder="جستجوی شماره همراه (۰۹۱۲...) یا نام..."
            value={searchQuery}
            onChange={handleSearchChange}
            className="w-full text-sm rounded-xl bg-slate-50 dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 pl-9 pr-4 py-2 text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:ring-indigo-500/25 dark:focus:border-indigo-500 transition-all"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-xs overflow-hidden">
        {isLoading ? (
          <div className="flex flex-col items-center justify-center py-24 gap-3">
            <div className="w-8 h-8 border-3 border-indigo-500/20 border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin" />
            <p className="text-sm font-medium text-slate-500 dark:text-slate-400">در حال بارگذاری لیست کاربران...</p>
          </div>
        ) : users.length === 0 ? (
          <div className="text-center py-20 px-4">
            <div className="w-14 h-14 rounded-2xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 text-indigo-600 dark:text-indigo-400 flex items-center justify-center mx-auto mb-4 shadow-xs">
              <Users className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-slate-800 dark:text-slate-200">کاربری یافت نشد</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1 max-w-sm mx-auto">
              با این مشخصات یا شماره همراه هیچ کاربری در پایگاه‌داده وجود ندارد.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-right border-collapse">
              <thead>
                <tr className="border-b border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-[#0E1422]/90 text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  <th className="py-4 px-4">کاربر</th>
                  <th className="py-4 px-4">شماره همراه</th>
                  <th className="py-4 px-4 text-center">وضعیت حساب</th>
                  <th className="py-4 px-4 text-center">وضعیت اشتراک</th>
                  <th className="py-4 px-4 text-center hidden sm:table-cell">امتیاز</th>
                  <th className="py-4 px-4 hidden md:table-cell">تاریخ عضویت</th>
                  <th className="py-4 px-4 text-left">عملیات</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60 text-sm">
                {users.map((user) => (
                  <tr
                    key={user.id}
                    className="hover:bg-slate-50/80 dark:hover:bg-[#151D30]/60 transition-colors group"
                  >
                    {/* User Identity */}
                    <td className="py-4 px-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-indigo-50 dark:bg-indigo-500/15 border border-indigo-200 dark:border-indigo-500/25 flex items-center justify-center text-indigo-700 dark:text-indigo-300 font-bold shrink-0 shadow-xs">
                          {user.nickName ? user.nickName.charAt(0) : 'U'}
                        </div>
                        <div>
                          <div className="font-bold text-slate-900 dark:text-slate-100 flex items-center gap-1.5 text-sm">
                            {user.nickName || 'کاربر بدون نام'}
                          </div>
                          <span className="text-xs text-slate-400 font-mono" dir="ltr">
                            {user.id.substring(0, 8)}...
                          </span>
                        </div>
                      </div>
                    </td>

                    {/* Mobile Number */}
                    <td className="py-4 px-4">
                      <span className="font-mono text-slate-800 dark:text-slate-200 bg-slate-100 dark:bg-[#0E1422] px-3 py-1 rounded-lg border border-slate-200 dark:border-slate-800/90 tabular-nums text-xs sm:text-sm" dir="ltr">
                        {user.mobile}
                      </span>
                    </td>

                    {/* Status Badge */}
                    <td className="py-4 px-4 text-center">
                      <Badge
                        variant={user.status === 'ACTIVE' ? 'success' : 'danger'}
                        pulse={user.status === 'ACTIVE'}
                      >
                        {user.status === 'ACTIVE' ? 'فعال' : 'تعلیق شده'}
                      </Badge>
                    </td>

                    {/* Subscription Status */}
                    <td className="py-4 px-4 text-center">
                      {user.hasActiveSubscription ? (
                        <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/25 px-2.5 py-0.5 rounded-md">
                          <Crown className="w-3.5 h-3.5 text-amber-500" />
                          <span>اشتراک ویژه</span>
                        </div>
                      ) : (
                        <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">پایه (رایگان)</span>
                      )}
                    </td>

                    {/* Score */}
                    <td className="py-4 px-4 text-center hidden sm:table-cell">
                      <div className="inline-flex items-center gap-1.5 font-mono font-semibold text-amber-700 dark:text-amber-400 bg-amber-50 dark:bg-amber-500/10 px-2.5 py-0.5 rounded-md border border-amber-200 dark:border-amber-500/20 tabular-nums text-xs">
                        <Trophy className="w-3.5 h-3.5 text-amber-500" />
                        {user.score}
                      </div>
                    </td>

                    {/* Created Date */}
                    <td className="py-4 px-4 hidden md:table-cell text-slate-500 dark:text-slate-400 text-xs font-mono tabular-nums">
                      {new Date(user.createdAt).toLocaleDateString('fa-IR')}
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-4 text-left">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => {
                            setGrantModalUser(user);
                          }}
                          className="p-2 rounded-xl text-slate-500 dark:text-slate-400 hover:text-amber-600 dark:hover:text-amber-300 hover:bg-amber-50 dark:hover:bg-amber-500/10 border border-transparent hover:border-amber-200 dark:hover:border-amber-500/20 transition-all"
                          title="اعطای اشتراک دستی"
                        >
                          <Gift className="w-4 h-4" />
                        </button>

                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => {
                            setSelectedUserId(user.id);
                            setDetailModalOpen(true);
                          }}
                          icon={<Eye className="w-4 h-4" />}
                        >
                          پرونده
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Bar */}
        {!isLoading && totalPages > 1 && (
          <div className="px-6 py-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-[#0E1422]/60 flex items-center justify-between text-xs sm:text-sm text-slate-600 dark:text-slate-400">
            <div className="tabular-nums">
              صفحه <span className="font-semibold text-slate-900 dark:text-slate-200">{currentPage}</span> از{' '}
              <span className="font-semibold text-slate-900 dark:text-slate-200">{totalPages}</span> (مجموع{' '}
              {totalItems} کاربر)
            </div>

            <div className="flex items-center gap-2">
              <Button
                size="sm"
                variant="outline"
                disabled={currentPage <= 1}
                onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
                icon={<ChevronRight className="w-4 h-4" />}
              >
                قبلی
              </Button>
              <Button
                size="sm"
                variant="outline"
                disabled={currentPage >= totalPages}
                onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
                icon={<ChevronLeft className="w-4 h-4" />}
              >
                بعدی
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* User Detail Modal */}
      <UserDetailModal
        isOpen={detailModalOpen}
        onClose={() => {
          setDetailModalOpen(false);
          setSelectedUserId(null);
        }}
        userId={selectedUserId}
        onUserUpdated={fetchUsers}
      />

      {/* Quick Grant Subscription Modal from Table Action */}
      <GrantSubscriptionModal
        isOpen={Boolean(grantModalUser)}
        onClose={() => setGrantModalUser(null)}
        user={grantModalUser}
        onSuccess={fetchUsers}
      />
    </div>
  );
};
