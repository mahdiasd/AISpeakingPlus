import React, { useState, useEffect } from 'react';
import { api } from '../../api/client';
import { useToast } from '../../context/ToastContext';
import { UserDetail, UserStatus } from '../../types';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { GrantSubscriptionModal } from './GrantSubscriptionModal';
import {
  User,
  Phone,
  Trophy,
  Calendar,
  ShieldAlert,
  ShieldCheck,
  Gift,
  Clock,
  Award,
} from 'lucide-react';

interface UserDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: string | null;
  onUserUpdated: () => void;
}

export const UserDetailModal: React.FC<UserDetailModalProps> = ({
  isOpen,
  onClose,
  userId,
  onUserUpdated,
}) => {
  const { success, error } = useToast();
  const [user, setUser] = useState<UserDetail | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [suspendReason, setSuspendReason] = useState('');
  const [showSuspendInput, setShowSuspendInput] = useState(false);
  const [grantModalOpen, setGrantModalOpen] = useState(false);

  const fetchUser = async (id: string) => {
    setIsLoading(true);
    try {
      const data = await api.getUser(id);
      setUser(data);
    } catch (err: any) {
      error(err.message || 'خطا در دریافت اطلاعات کاربر');
      onClose();
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && userId) {
      fetchUser(userId);
      setShowSuspendInput(false);
      setSuspendReason('');
    }
  }, [isOpen, userId]);

  const handleStatusChange = async (newStatus: UserStatus) => {
    if (!user) return;
    if (newStatus === 'SUSPENDED' && !showSuspendInput) {
      setShowSuspendInput(true);
      return;
    }

    setIsUpdatingStatus(true);
    try {
      await api.updateUserStatus(user.id, newStatus, suspendReason.trim() || undefined);
      success(newStatus === 'ACTIVE' ? 'حساب کاربر با موفقیت فعال شد' : 'حساب کاربر با موفقیت تعلیق شد');
      await fetchUser(user.id);
      onUserUpdated();
      setShowSuspendInput(false);
      setSuspendReason('');
    } catch (err: any) {
      error(err.message || 'خطا در تغییر وضعیت حساب');
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  if (!isOpen) return null;

  return (
    <>
      <Modal
        isOpen={isOpen && !grantModalOpen}
        onClose={onClose}
        title="پروفایل و پرونده کاربر"
        maxWidth="2xl"
      >
        {isLoading || !user ? (
          <div className="flex flex-col items-center justify-center py-16 gap-3">
            <div className="w-8 h-8 border-3 border-indigo-500/20 border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin" />
            <p className="text-sm font-medium text-slate-500 dark:text-slate-400">در حال دریافت پرونده کاربر...</p>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Top Identity Card */}
            <div className="flex flex-col sm:flex-row items-center sm:items-start gap-4 p-5 rounded-2xl bg-slate-50 dark:bg-[#0E1422] border border-slate-200 dark:border-slate-800 shadow-xs">
              <div className="w-16 h-16 rounded-2xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 flex items-center justify-center text-indigo-700 dark:text-indigo-400 font-bold text-2xl shrink-0 shadow-xs">
                {user.nickName ? user.nickName.charAt(0) : <User className="w-8 h-8" />}
              </div>

              <div className="flex-1 text-center sm:text-right space-y-1.5">
                <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
                  <h3 className="text-lg font-bold text-slate-900 dark:text-slate-100">
                    {user.firstName || user.lastName ? `${user.firstName || ''} ${user.lastName || ''}` : user.nickName}
                  </h3>
                  <Badge variant={user.status === 'ACTIVE' ? 'success' : 'danger'} pulse={user.status === 'ACTIVE'}>
                    {user.status === 'ACTIVE' ? 'حساب فعال' : 'حساب تعلیق شده'}
                  </Badge>
                  {user.activeSubscription && (
                    <Badge variant="purple">اشتراک فعال</Badge>
                  )}
                </div>

                <div className="flex flex-wrap items-center justify-center sm:justify-start gap-4 text-xs sm:text-sm text-slate-500 dark:text-slate-400 pt-1">
                  <span className="flex items-center gap-1 font-mono text-slate-700 dark:text-slate-300 font-medium" dir="ltr">
                    <Phone className="w-4 h-4 text-indigo-500" />
                    {user.mobile}
                  </span>
                  <span className="flex items-center gap-1">
                    <Trophy className="w-4 h-4 text-amber-500" />
                    امتیاز: <span className="font-mono font-bold tabular-nums text-slate-800 dark:text-slate-200">{user.score}</span>
                  </span>
                  <span className="flex items-center gap-1">
                    <Calendar className="w-4 h-4 text-slate-400" />
                    عضویت: <span className="font-mono tabular-nums">{new Date(user.createdAt).toLocaleDateString('fa-IR')}</span>
                  </span>
                </div>
              </div>
            </div>

            {/* Suspended Reason Banner if Suspended */}
            {user.status === 'SUSPENDED' && (
              <div className="p-4 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-800/50 text-sm text-rose-800 dark:text-rose-200 flex items-start gap-3">
                <ShieldAlert className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold text-rose-900 dark:text-rose-300">این حساب کاربری مسدود شده است.</p>
                  <p className="mt-1 text-rose-700 dark:text-rose-400 leading-relaxed text-xs">
                    علت تعلیق: {user.suspendedReason || 'علتی توسط ادمین ثبت نشده است.'}
                  </p>
                </div>
              </div>
            )}

            {/* Subscription Section */}
            <div className="rounded-2xl bg-slate-50 dark:bg-[#0E1422] border border-slate-200 dark:border-slate-800 p-5 space-y-3.5 shadow-xs">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Award className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                  <span className="text-sm font-semibold text-slate-900 dark:text-slate-200">وضعیت و سوابق اشتراک</span>
                </div>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setGrantModalOpen(true)}
                  icon={<Gift className="w-4 h-4 text-indigo-500" />}
                >
                  اعطای دستی اشتراک
                </Button>
              </div>

              {user.activeSubscription ? (
                <div className="p-4 rounded-xl bg-white dark:bg-[#0B0F17]/60 border border-slate-200 dark:border-slate-800 space-y-2.5">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-slate-900 dark:text-slate-100 font-mono">
                        طرح {user.activeSubscription.planType}
                      </span>
                      {user.activeSubscription.grantSource === 'MANUAL_ADMIN' ? (
                        <Badge variant="purple" size="sm">
                          اهدایی ادمین ({user.activeSubscription.grantedByAdminName || 'مدیر سیستم'})
                        </Badge>
                      ) : (
                        <Badge variant="info" size="sm">خرید درگاه بانکی</Badge>
                      )}
                    </div>
                    <Badge variant="success" size="sm">معتبر</Badge>
                  </div>

                  <div className="grid grid-cols-2 gap-2 text-xs text-slate-500 dark:text-slate-400 pt-1 font-mono">
                    <div className="flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      <span>شروع: {new Date(user.activeSubscription.startedAt).toLocaleDateString('fa-IR')}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-emerald-500" />
                      <span>انقضا: {new Date(user.activeSubscription.expiresAt).toLocaleDateString('fa-IR')}</span>
                    </div>
                  </div>

                  {user.activeSubscription.grantReason && (
                    <div className="mt-2 pt-2 border-t border-slate-100 dark:border-slate-800 text-xs text-slate-600 dark:text-slate-300">
                      <span className="text-slate-400">یادداشت و علت ثبت: </span>
                      <span className="italic">{user.activeSubscription.grantReason}</span>
                    </div>
                  )}
                </div>
              ) : (
                <div className="p-3.5 rounded-xl bg-white dark:bg-[#0B0F17]/40 border border-slate-200 dark:border-slate-800 text-sm text-slate-500 dark:text-slate-400 flex items-center justify-between">
                  <span>این کاربر در حال حاضر اشتراک فعالی ندارد.</span>
                  <span className="text-xs text-amber-600 dark:text-amber-400 font-mono font-medium">پلن پایه (رایگان)</span>
                </div>
              )}
            </div>

            {/* Suspend Account Input Box */}
            {showSuspendInput && (
              <div className="p-4 rounded-xl bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-800/40 space-y-3">
                <label className="block text-xs sm:text-sm font-medium text-rose-800 dark:text-rose-300">
                  علت تعلیق حساب کاربر (برای کاربر و در لاگ ادمین ثبت می‌شود):
                </label>
                <textarea
                  rows={2}
                  value={suspendReason}
                  onChange={(e) => setSuspendReason(e.target.value)}
                  placeholder="مثال: نقض قوانین مکالمه صوتی، توهین یا سوءاستفاده"
                  className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-rose-300 dark:border-rose-800/60 text-slate-900 dark:text-slate-100 text-sm p-3 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500/20 transition-colors"
                  autoFocus
                />
                <div className="flex justify-end gap-2">
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => setShowSuspendInput(false)}
                    disabled={isUpdatingStatus}
                  >
                    انصراف
                  </Button>
                  <Button
                    size="sm"
                    variant="danger"
                    onClick={() => handleStatusChange('SUSPENDED')}
                    isLoading={isUpdatingStatus}
                    icon={<ShieldAlert className="w-4 h-4" />}
                  >
                    تأیید تعلیق حساب
                  </Button>
                </div>
              </div>
            )}

            {/* Bottom Actions */}
            <div className="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-800">
              <div>
                {user.status === 'ACTIVE' ? (
                  !showSuspendInput && (
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => handleStatusChange('SUSPENDED')}
                      isLoading={isUpdatingStatus}
                      icon={<ShieldAlert className="w-4 h-4" />}
                    >
                      تعلیق حساب کاربر
                    </Button>
                  )
                ) : (
                  <Button
                    size="sm"
                    variant="primary"
                    onClick={() => handleStatusChange('ACTIVE')}
                    isLoading={isUpdatingStatus}
                    icon={<ShieldCheck className="w-4 h-4" />}
                  >
                    فعال‌سازی مجدد حساب
                  </Button>
                )}
              </div>

              <Button variant="ghost" onClick={onClose}>
                بستن
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* Grant Subscription Modal Triggered from here */}
      {user && (
        <GrantSubscriptionModal
          isOpen={grantModalOpen}
          onClose={() => setGrantModalOpen(false)}
          user={user}
          onSuccess={() => {
            fetchUser(user.id);
            onUserUpdated();
          }}
        />
      )}
    </>
  );
};
