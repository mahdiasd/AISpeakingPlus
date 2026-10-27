import React, { useState, useEffect } from 'react';
import {
  Bell,
  Send,
  Smartphone,
  ExternalLink,
  AlertTriangle,
  CheckCircle2,
  Copy,
  Check,
  RefreshCw,
  Image as ImageIcon,
  MessageSquare,
  Link as LinkIcon,
  Code2,
  Laptop,
  Radio,
  XCircle,
} from 'lucide-react';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { Stage, AdminNotificationPayload, AdminNotificationResponse } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { ImageDropzone } from '../components/stages/ImageDropzone';
import {
  setupBrowserNotifications,
  isNotificationSupported,
  getStoredFcmToken,
} from '../utils/firebasePush';

interface SentNotificationRecord {
  id: string;
  title: string;
  body: string;
  target: string;
  targetType: string;
  screen: string;
  stageId?: string;
  sentAt: string;
}

export const NotificationsPage: React.FC = () => {
  const { error: toastError, success: toastSuccess } = useToast();

  // Form State
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [targetType, setTargetType] = useState<'topic' | 'token'>('topic');
  const [targetValue, setTargetValue] = useState('aispeaking_plus_v1');
  const [screen, setScreen] = useState('none');
  const [stageId, setStageId] = useState('');
  const [customUrl, setCustomUrl] = useState('');

  // UI State
  const [isSending, setIsSending] = useState(false);
  const [fullError, setFullError] = useState<string | null>(null);
  const [lastSuccess, setLastSuccess] = useState<AdminNotificationResponse | null>(null);
  const [stages, setStages] = useState<Stage[]>([]);
  const [loadingStages, setLoadingStages] = useState(false);
  const [showJsonPreview, setShowJsonPreview] = useState(false);
  const [copiedPayload, setCopiedPayload] = useState(false);
  const [sentHistory, setSentHistory] = useState<SentNotificationRecord[]>([]);

  // Browser Notification Status State
  const [browserSupported, setBrowserSupported] = useState(false);
  const [browserPermission, setBrowserPermission] = useState<string>('default');
  const [browserToken, setBrowserToken] = useState<string | null>(null);
  const [isEnablingBrowser, setIsEnablingBrowser] = useState(false);
  const [isDirectTesting, setIsDirectTesting] = useState(false);
  const [copiedBrowserToken, setCopiedBrowserToken] = useState(false);

  // Initialize browser notification status
  useEffect(() => {
    const supported = isNotificationSupported();
    setBrowserSupported(supported);
    if (supported) {
      setBrowserPermission(Notification.permission);
      const savedToken = getStoredFcmToken();
      if (savedToken) {
        setBrowserToken(savedToken);
      }
    }
  }, []);

  // Load available stages for quick deep link selection
  useEffect(() => {
    async function loadStages() {
      setLoadingStages(true);
      try {
        const data = await api.getStages('ALL');
        setStages(data);
        if (data.length > 0 && !stageId) {
          setStageId(data[0].id);
        }
      } catch (err) {
        console.warn('Failed to load stages for notification picker:', err);
      } finally {
        setLoadingStages(false);
      }
    }
    loadStages();
  }, []);

  // Compute destination description for preview
  const getDestinationLabel = () => {
    switch (screen) {
      case 'none':
        return 'بدون لینک (فقط اعلان)';
      case 'chat': {
        const found = stages.find((s) => s.id === stageId);
        return found ? `مرحله: ${found.titleFa || found.title} (${stageId})` : `مرحله چت: ${stageId || 'انتخاب نشده'}`;
      }
      case 'subscription':
        return 'صفحه خرید و ارتقای اشتراک (VIP)';
      case 'stages':
        return 'کاتالوگ لیست مراحل';
      case 'profile':
        return 'صفحه پروفایل کاربر';
      case 'main':
        return 'صفحه اصلی (نقشه ماجراجویی)';
      case 'url':
        return `لینک سفارشی: ${customUrl || 'تنظیم نشده'}`;
      default:
        return 'بدون لینک (فقط اعلان)';
    }
  };

  // Build the current payload object for preview & submission
  const currentPayload: AdminNotificationPayload = {
    title: title.trim(),
    body: body.trim(),
    imageUrl: imageUrl.trim() || undefined,
    targetType,
    targetValue: targetValue.trim() || 'aispeaking_plus_v1',
    screen: screen !== 'none' && screen !== 'url' ? screen : undefined,
    stageId: screen === 'chat' && stageId ? stageId.trim() : undefined,
    url: screen === 'url' && customUrl ? customUrl.trim() : undefined,
  };

  const handleEnableBrowserPush = async () => {
    setIsEnablingBrowser(true);
    try {
      const res = await setupBrowserNotifications((payload) => {
        toastSuccess(`پیام جدید دریافت شد: ${payload.notification?.title || payload.data?.title || 'نوتیفیکیشن'}`);
      });
      setBrowserPermission(res.permission);
      if (res.token) {
        setBrowserToken(res.token);
        toastSuccess('نوتیفیکیشن مرورگر فعال و در کانال aispeaking_plus_v1 ثبت شد!');
      } else if (res.error) {
        toastError(res.error);
      }
    } catch (err: any) {
      toastError(err?.message || 'خطا در فعال‌سازی نوتیفیکیشن مرورگر');
    } finally {
      setIsEnablingBrowser(false);
    }
  };

  const handleCopyBrowserToken = () => {
    if (!browserToken) return;
    navigator.clipboard.writeText(browserToken);
    setCopiedBrowserToken(true);
    setTimeout(() => setCopiedBrowserToken(false), 2000);
  };

  const handleDirectTestToThisBrowser = async () => {
    if (!browserToken) {
      toastError('ابتدا نوتیفیکیشن این مرورگر را فعال کنید تا توکن دستگاه دریافت شود');
      return;
    }
    setIsDirectTesting(true);
    try {
      const testPayload: AdminNotificationPayload = {
        title: 'تست مستقیم به مرورگر شما 🔔',
        body: 'اگر این اعلان را می‌بینید، ارتباط و تحویل پوش نوتیفیکیشن کاملاً سالم است!',
        targetType: 'token',
        targetValue: browserToken,
        screen: 'none',
      };
      const res = await api.sendPushNotification(testPayload);
      setLastSuccess(res);
      toastSuccess('نوتیفیکیشن آزمایشی با موفقیت ارسال شد!');
    } catch (err: any) {
      console.error('Direct test error:', err);
      const errMsg = err?.message || 'خطا در ارسال تست مستقیم به این دستگاه';
      setFullError(errMsg);
      toastError(errMsg);
    } finally {
      setIsDirectTesting(false);
    }
  };

  const handleCopyPayload = () => {
    navigator.clipboard.writeText(JSON.stringify(currentPayload, null, 2));
    setCopiedPayload(true);
    setTimeout(() => setCopiedPayload(false), 2000);
  };

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    setFullError(null);
    setLastSuccess(null);

    if (!title.trim()) {
      toastError('لطفاً عنوان نوتیفیکیشن را وارد کنید');
      return;
    }
    if (!body.trim()) {
      toastError('لطفاً متن پیام نوتیفیکیشن را وارد کنید');
      return;
    }
    if (targetType === 'token' && !targetValue.trim()) {
      toastError('لطفاً توکن دستگاه مقصد را وارد کنید');
      return;
    }

    setIsSending(true);

    try {
      const res = await api.sendPushNotification(currentPayload);
      setLastSuccess(res);
      toastSuccess('نوتیفیکیشن با موفقیت به فایربیس ارسال شد!');

      // Add to session history
      const record: SentNotificationRecord = {
        id: res.messageId,
        title: currentPayload.title,
        body: currentPayload.body,
        target: res.target,
        targetType: res.targetType,
        screen,
        stageId: screen === 'chat' ? stageId : undefined,
        sentAt: new Date().toLocaleTimeString('fa-IR'),
      };
      setSentHistory((prev) => [record, ...prev]);
    } catch (err: any) {
      console.error('Failed to send notification:', err);
      const errMsg = err?.message || 'خطای ناشناخته در هنگام ارسال نوتیفیکیشن';
      setFullError(errMsg);
      toastError(errMsg);
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="space-y-4 pb-12 max-w-7xl mx-auto">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white dark:bg-[#202125] p-5 rounded-2xl border border-slate-200 dark:border-[#2d3037] shadow-2xs">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] flex items-center justify-center text-indigo-600 dark:text-indigo-400 shrink-0">
            <Bell className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-900 dark:text-[#f1f3f5]">ارسال پوش‌نوتیفیکیشن</h1>
              <Badge variant="info" size="sm">FCM v1</Badge>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Badge variant="success" size="md" className="flex items-center gap-1.5 py-1 px-2.5">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            کانال فعال: <code className="font-mono text-xs">aispeaking_plus_v1</code>
          </Badge>
        </div>
      </div>

      {/* Full Error Alert Container */}
      {fullError && (
        <div className="p-5 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900/60 text-rose-900 dark:text-rose-200 space-y-2 animate-in fade-in duration-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 font-bold text-base">
              <AlertTriangle className="w-5 h-5 text-rose-600 dark:text-rose-400 shrink-0" />
              <span>خطای سرور / فایربیس هنگام ارسال نوتیفیکیشن</span>
            </div>
            <button
              onClick={() => setFullError(null)}
              className="text-xs text-rose-600 dark:text-rose-400 hover:underline cursor-pointer"
            >
              بستن
            </button>
          </div>
          <div className="bg-white/80 dark:bg-black/50 p-3.5 rounded-xl border border-rose-200/60 dark:border-rose-900/40 font-mono text-xs text-rose-800 dark:text-rose-300 whitespace-pre-wrap break-all leading-relaxed select-all">
            {fullError}
          </div>
          <p className="text-xs text-rose-700 dark:text-rose-300/80">
            نکته: در صورت خطای احراز هویت فایربیس، وضعیت کلید سرویس اکانت (<code className="font-mono">service-account-key.json</code>) را در سرور بررسی نمایید.
          </p>
        </div>
      )}

      {/* Success Notification Alert */}
      {lastSuccess && (
        <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/50 text-emerald-900 dark:text-emerald-200 flex items-start justify-between gap-3 animate-in fade-in duration-200">
          <div className="flex items-start gap-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 dark:text-emerald-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-sm">نوتیفیکیشن با موفقیت ثبت و به فایربیس تحویل داده شد.</p>
              <div className="mt-1 flex items-center gap-2 text-xs font-mono text-emerald-800 dark:text-emerald-300">
                <span>شناسه پیام فایربیس:</span>
                <span className="bg-emerald-100 dark:bg-emerald-900/50 px-2 py-0.5 rounded border border-emerald-200 dark:border-emerald-800 break-all">
                  {lastSuccess.messageId}
                </span>
              </div>
            </div>
          </div>
          <button
            onClick={() => setLastSuccess(null)}
            className="text-xs text-emerald-700 hover:underline shrink-0"
          >
            بستن
          </button>
        </div>
      )}

      {/* Main Grid: Form on Left/Right, Live Phone Preview on Opposite */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Form (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          <form onSubmit={handleSend} className="bg-white dark:bg-[#202125] p-5 sm:p-6 rounded-2xl border border-slate-200 dark:border-[#2d3037] shadow-2xs space-y-5">
            <div className="border-b border-slate-100 dark:border-[#2d3037] pb-3.5">
              <h2 className="text-base font-bold text-slate-900 dark:text-[#f1f3f5] flex items-center gap-2">
                <MessageSquare className="w-4 h-4 text-indigo-500" />
                محتوا و جزئیات پیام
              </h2>
            </div>

            {/* Title */}
            <div>
              <Input
                label="عنوان نوتیفیکیشن *"
                placeholder="مثال: مرحله جدید باز شد! ✈️"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            {/* Body */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                متن پیام نوتیفیکیشن *
              </label>
              <textarea
                className="w-full rounded-xl bg-white border border-slate-300 dark:border-[#363842] text-slate-900 placeholder-slate-400 text-sm px-3.5 py-2.5 transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-offset-white dark:focus:ring-offset-[#18191b] focus:border-indigo-600 dark:focus:border-indigo-500 dark:bg-[#24262b] dark:text-[#f1f3f5] dark:placeholder-[#858a94] min-h-[90px]"
                placeholder="مثال: مکالمه در هتل را تمرین کن و مهارتت رو ارتقا بده..."
                value={body}
                onChange={(e) => setBody(e.target.value)}
                required
              />
            </div>

            {/* Image URL with Dropzone / Input */}
            <div className="space-y-2">
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 select-none">
                تصویر نوتیفیکیشن (اختیاری)
              </label>
              <div className="flex gap-2">
                <Input
                  placeholder="https://example.com/banner.png یا آپلود از پایین"
                  value={imageUrl}
                  onChange={(e) => setImageUrl(e.target.value)}
                  leftIcon={<ImageIcon className="w-4 h-4" />}
                />
                {imageUrl && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setImageUrl('')}
                    className="shrink-0 text-xs"
                  >
                    پاک کردن
                  </Button>
                )}
              </div>
              <div className="mt-2">
                <ImageDropzone
                  label="یا تصویر جدید را آپلود کنید"
                  value={imageUrl}
                  onChange={(uploadedUrl) => setImageUrl(uploadedUrl)}
                  helperText="فرمت‌های WebP، PNG یا JPG (حداکثر ۵ مگابایت)"
                  aspectRatio="wide"
                />
              </div>
            </div>

            {/* Target Audience Section */}
            <div className="pt-4 border-t border-slate-100 dark:border-slate-800 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
                    <Send className="w-4 h-4 text-indigo-500" />
                    مخاطبان هدف (Target)
                  </h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    برای جلوگیری از ارسال به کاربران نسخه قبلی، کانال پیش‌فرض روی نسخه پلاس تنظیم شده است
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <label
                  className={`flex items-start gap-3 p-3.5 rounded-xl border cursor-pointer transition-all ${
                    targetType === 'topic' && targetValue === 'aispeaking_plus_v1'
                      ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-500/10 dark:border-indigo-500/50'
                      : 'border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700'
                  }`}
                  onClick={() => {
                    setTargetType('topic');
                    setTargetValue('aispeaking_plus_v1');
                  }}
                >
                  <input
                    type="radio"
                    name="targetMode"
                    checked={targetType === 'topic' && targetValue === 'aispeaking_plus_v1'}
                    onChange={() => {
                      setTargetType('topic');
                      setTargetValue('aispeaking_plus_v1');
                    }}
                    className="mt-0.5 text-indigo-600 focus:ring-indigo-500"
                  />
                  <div>
                    <span className="text-sm font-semibold text-slate-900 dark:text-slate-100 block">
                      کانال کاربران پلاس (پیش‌فرض)
                    </span>
                    <span className="text-xs text-slate-500 dark:text-slate-400 font-mono mt-0.5 block">
                      topic: aispeaking_plus_v1
                    </span>
                  </div>
                </label>

                <label
                  className={`flex items-start gap-3 p-3.5 rounded-xl border cursor-pointer transition-all ${
                    targetType === 'token'
                      ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-500/10 dark:border-indigo-500/50'
                      : 'border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700'
                  }`}
                  onClick={() => {
                    setTargetType('token');
                    if (targetValue === 'aispeaking_plus_v1') setTargetValue('');
                  }}
                >
                  <input
                    type="radio"
                    name="targetMode"
                    checked={targetType === 'token'}
                    onChange={() => {
                      setTargetType('token');
                      if (targetValue === 'aispeaking_plus_v1') setTargetValue('');
                    }}
                    className="mt-0.5 text-indigo-600 focus:ring-indigo-500"
                  />
                  <div>
                    <span className="text-sm font-semibold text-slate-900 dark:text-slate-100 block">
                      تک‌دستگاه اختصاصی (تست)
                    </span>
                    <span className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 block">
                      ارسال به یک توکن مشخص دستگاه یا وب
                    </span>
                  </div>
                </label>
              </div>

              {/* Token Input if Token Mode is selected */}
              {targetType === 'token' && (
                <div className="mt-2 animate-in fade-in">
                  <Input
                    label="توکن دستگاه مقصد (FCM Registration Token) *"
                    placeholder="توکن بلند کلاینت را اینجا وارد کنید..."
                    value={targetValue}
                    onChange={(e) => setTargetValue(e.target.value)}
                    required
                  />
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1">
                    در وب، می‌توانید توکن خود را با دستور <code className="font-mono bg-slate-100 dark:bg-slate-800 px-1 py-0.5 rounded">localStorage.getItem('fcm_web_token')</code> در کنسول مرورگر کپی کنید.
                  </p>
                </div>
              )}
            </div>

            {/* Deep Link Action Section */}
            <div className="pt-4 border-t border-slate-100 dark:border-slate-800 space-y-4">
              <div>
                <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
                  <LinkIcon className="w-4 h-4 text-indigo-500" />
                  مقصد کلیک کاربر (Deep Link)
                </h3>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  کاربر با کلیک روی نوتیفیکیشن مستقیماً به این صفحه یا مرحله هدایت خواهد شد
                </p>
              </div>

              {/* Destination Screen Picker */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                  صفحه مقصد (اختیاری)
                </label>
                <select
                  value={screen}
                  onChange={(e) => setScreen(e.target.value)}
                  className="w-full rounded-xl bg-white border border-slate-300 dark:border-[#363842] text-slate-900 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:bg-[#24262b] dark:text-[#f1f3f5]"
                >
                  <option value="none">بدون لینک (فقط نمایش پیام - بدون کلیک)</option>
                  <option value="chat">تمرین مرحله چت (Stage Chat)</option>
                  <option value="subscription">صفحه خرید و ارتقای اشتراک (Subscription / VIP)</option>
                  <option value="stages">کاتالوگ لیست مراحل (Stages List)</option>
                  <option value="profile">صفحه پروفایل کاربر (Profile)</option>
                  <option value="main">صفحه اصلی (نقشه ماجراجویی - Journey Map)</option>
                  <option value="url">لینک سفارشی درون‌برنامه یا وب (Custom URL)</option>
                </select>
              </div>

              {/* Notice when screen is 'none' */}
              {screen === 'none' && (
                <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 text-xs text-slate-500 dark:text-slate-400 space-y-1 animate-in fade-in">
                  <div className="font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                    <span>💡</span>
                    <span>پیام اطلاع‌رسانی عمومی (بدون مقصد کلیک)</span>
                  </div>
                  <p>
                    این پیام تنها در نوار اعلان نمایش داده می‌شود و کلیک روی آن هیچ صفحه خاصی را باز نمی‌کند.
                  </p>
                </div>
              )}

              {/* Stage Picker if Screen is 'chat' */}
              {screen === 'chat' && (
                <div className="space-y-2 animate-in fade-in">
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 select-none">
                    انتخاب مرحله مورد نظر *
                  </label>
                  {loadingStages ? (
                    <div className="text-xs text-slate-400 flex items-center gap-2 py-2">
                      <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                      در حال دریافت لیست مراحل...
                    </div>
                  ) : (
                    <div className="space-y-2">
                      <select
                        value={stageId}
                        onChange={(e) => setStageId(e.target.value)}
                        className="w-full rounded-xl bg-white border border-slate-300 dark:border-[#363842] text-slate-900 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:bg-[#24262b] dark:text-[#f1f3f5]"
                      >
                        {stages.map((stg) => (
                          <option key={stg.id} value={stg.id}>
                            مرحله {stg.orderIndex}: {stg.titleFa || stg.title} ({stg.id})
                          </option>
                        ))}
                      </select>
                      <Input
                        placeholder="یا شناسه مرحله را دستی وارد کنید (مثال: stage-01-inflight-london)"
                        value={stageId}
                        onChange={(e) => setStageId(e.target.value)}
                      />
                    </div>
                  )}
                </div>
              )}

              {/* Custom URL Input if Screen is 'url' */}
              {screen === 'url' && (
                <div className="animate-in fade-in">
                  <Input
                    label="آدرس کامل دیپ‌لینک *"
                    placeholder="مثال: aispeaking://chat?stageId=stage-01 یا /#subscription"
                    value={customUrl}
                    onChange={(e) => setCustomUrl(e.target.value)}
                    required
                  />
                </div>
              )}
            </div>

            {/* Submit Button */}
            <div className="pt-5 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between gap-3">
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => setShowJsonPreview(!showJsonPreview)}
                className="text-xs flex items-center gap-1.5"
              >
                <Code2 className="w-3.5 h-3.5" />
                {showJsonPreview ? 'مخفی کردن پی‌لود JSON' : 'مشاهده پی‌لود JSON'}
              </Button>

              <Button
                type="submit"
                variant="primary"
                size="md"
                isLoading={isSending}
                icon={<Send className="w-4 h-4 ml-1" />}
                className="px-6"
              >
                {isSending ? 'در حال ارسال به فایربیس...' : 'ارسال پوش‌نوتیفیکیشن'}
              </Button>
            </div>
          </form>

          {/* Collapsible JSON Preview */}
          {showJsonPreview && (
            <div className="bg-slate-900 text-slate-100 p-4 rounded-2xl border border-slate-800 shadow-sm space-y-2 animate-in fade-in">
              <div className="flex items-center justify-between">
                <span className="text-xs font-mono font-semibold text-slate-400">FCM Request Payload</span>
                <button
                  type="button"
                  onClick={handleCopyPayload}
                  className="flex items-center gap-1 text-xs text-indigo-400 hover:text-indigo-300 font-mono"
                >
                  {copiedPayload ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  {copiedPayload ? 'کپی شد' : 'کپی JSON'}
                </button>
              </div>
              <pre className="text-xs font-mono p-3 bg-black/60 rounded-xl overflow-x-auto text-emerald-400 leading-relaxed dir-ltr">
                {JSON.stringify(currentPayload, null, 2)}
              </pre>
            </div>
          )}
        </div>

        {/* Right Column: Mobile Notification Mockup & Session History (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          {/* Browser Notification Status & Quick Direct Test Card */}
          <div className="bg-white dark:bg-[#202125] p-5 rounded-2xl border border-slate-200 dark:border-[#2d3037] shadow-2xs space-y-3.5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Laptop className="w-4 h-4 text-indigo-500" />
                <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                  وضعیت نوتیفیکیشن در این مرورگر
                </h3>
              </div>
              {browserPermission === 'granted' ? (
                <Badge variant="success" size="sm" className="gap-1">
                  <CheckCircle2 className="w-3 h-3" />
                  فعال و آماده
                </Badge>
              ) : browserPermission === 'denied' ? (
                <Badge variant="danger" size="sm" className="gap-1">
                  <XCircle className="w-3 h-3" />
                  مسدود شده
                </Badge>
              ) : (
                <Badge variant="warning" size="sm" className="gap-1">
                  <Radio className="w-3 h-3 animate-pulse" />
                  نیاز به فعال‌سازی
                </Badge>
              )}
            </div>

            {!browserSupported && (
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800 text-xs text-amber-800 dark:text-amber-200">
                ⚠️ نوتیفیکیشن در این مرورگر یا محیط غیرامن (غیر HTTPS و غیر localhost) پشتیبانی نمی‌شود.
              </div>
            )}

            <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
              {browserPermission === 'granted'
                ? 'این مرورگر در سرویس FCM ثبت شده و در کانال aispeaking_plus_v1 عضو است.'
                : 'برای دریافت اعلان‌ها روی این دستگاه، روی دکمه زیر کلیک کنید و در پاپ‌آپ مرورگر Allow را انتخاب کنید.'}
            </p>

            {/* Token display if available */}
            {browserToken && (
              <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 text-xs space-y-1.5 font-mono">
                <div className="flex items-center justify-between text-[11px] text-slate-500 dark:text-slate-400">
                  <span>توکن FCM این مرورگر:</span>
                  <button
                    type="button"
                    onClick={handleCopyBrowserToken}
                    className="text-indigo-600 dark:text-indigo-400 hover:underline flex items-center gap-1 cursor-pointer font-sans"
                  >
                    {copiedBrowserToken ? <Check className="w-3 h-3 text-emerald-500" /> : <Copy className="w-3 h-3" />}
                    <span>{copiedBrowserToken ? 'کپی شد' : 'کپی'}</span>
                  </button>
                </div>
                <div className="text-[10px] text-slate-700 dark:text-slate-300 break-all select-all font-mono line-clamp-2">
                  {browserToken}
                </div>
              </div>
            )}

            <div className="flex flex-col sm:flex-row gap-2 pt-1">
              {browserPermission !== 'granted' ? (
                <Button
                  type="button"
                  variant="primary"
                  size="sm"
                  onClick={handleEnableBrowserPush}
                  isLoading={isEnablingBrowser}
                  className="w-full text-xs"
                  icon={<Bell className="w-3.5 h-3.5" />}
                >
                  فعال‌سازی نوتیفیکیشن در این مرورگر
                </Button>
              ) : (
                <>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleDirectTestToThisBrowser}
                    isLoading={isDirectTesting}
                    className="flex-1 text-xs"
                    icon={<Send className="w-3.5 h-3.5 text-indigo-500" />}
                  >
                    ارسال تست مستقیم به همین مرورگر
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={handleEnableBrowserPush}
                    isLoading={isEnablingBrowser}
                    className="text-xs shrink-0"
                    title="تلاش مجدد برای دریافت توکن و ثبت در کانال"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                  </Button>
                </>
              )}
            </div>
          </div>

          {/* Mobile Preview Mockup */}
          <div className="bg-white dark:bg-[#202125] p-5 sm:p-6 rounded-2xl border border-slate-200 dark:border-[#2d3037] shadow-2xs space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-base font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
                <Smartphone className="w-4 h-4 text-indigo-500" />
                پیش‌نمایش اعلان در موبایل
              </h2>
              <span className="text-xs text-slate-400">زنده (Live)</span>
            </div>

            {/* Phone Lock Screen Notification Card */}
            <div className="relative p-4 rounded-2xl bg-slate-900/90 dark:bg-black/80 backdrop-blur-md border border-slate-700/60 text-white shadow-xl space-y-2.5 transition-all">
              {/* App Badge Header */}
              <div className="flex items-center justify-between text-xs text-slate-400">
                <div className="flex items-center gap-2">
                  <div className="w-5 h-5 rounded-md bg-indigo-600 flex items-center justify-center text-white font-bold text-[10px]">
                    AI
                  </div>
                  <span className="font-semibold text-slate-200 text-xs">AI Speaking Plus</span>
                  <span>•</span>
                  <span>الان</span>
                </div>
                <span className="w-1.5 h-1.5 rounded-full bg-indigo-400" />
              </div>

              {/* Notification Content */}
              <div>
                <h4 className="font-bold text-sm text-slate-100 leading-tight">
                  {title.trim() || 'عنوان اعلان اینجا قرار می‌گیرد...'}
                </h4>
                <p className="text-xs text-slate-300 mt-1 line-clamp-2 leading-relaxed">
                  {body.trim() || 'متن کامل پیام نوتیفیکیشن در این بخش نمایش داده می‌شود.'}
                </p>
              </div>

              {/* Notification Image Preview if provided */}
              {imageUrl.trim() && (
                <div className="mt-2 rounded-xl overflow-hidden border border-slate-700/60 max-h-40 bg-black/40">
                  <img
                    src={imageUrl.trim()}
                    alt="Notification Media"
                    className="w-full h-auto object-cover max-h-36"
                    onError={(e) => {
                      (e.target as HTMLElement).style.display = 'none';
                    }}
                  />
                </div>
              )}

              {/* Click Action Badge inside preview */}
              {screen === 'none' ? (
                <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-[11px] text-slate-400">
                  <span className="flex items-center gap-1">
                    <Bell className="w-3 h-3 text-slate-400" />
                    بدون عملکرد کلیک (فقط اعلان پیام)
                  </span>
                  <span className="text-[10px] text-slate-500">بدون تغییر صفحه</span>
                </div>
              ) : (
                <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-[11px] text-indigo-300">
                  <span className="flex items-center gap-1">
                    <ExternalLink className="w-3 h-3" />
                    {getDestinationLabel()}
                  </span>
                  <span className="text-[10px] text-slate-500">برای باز شدن کلیک کنید</span>
                </div>
              )}
            </div>

            <p className="text-xs text-slate-500 dark:text-slate-400 text-center leading-relaxed">
              این پیش‌نمایش به صورت لحظه‌ای با اطلاعات فرم هماهنگ است و ظاهر نوتیفیکیشن در نوار اعلان کاربر را نشان می‌دهد.
            </p>
          </div>

          {/* Session Sent Notifications History */}
          <div className="bg-white dark:bg-[#202125] p-5 sm:p-6 rounded-2xl border border-slate-200 dark:border-[#2d3037] shadow-2xs space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                پیام‌های ارسال‌شده در این نشست
              </h3>
              <Badge variant="neutral" size="sm">{sentHistory.length}</Badge>
            </div>

            {sentHistory.length === 0 ? (
              <div className="py-8 text-center text-slate-400 text-xs">
                هنوز پیامی در این نشست ارسال نشده است.
              </div>
            ) : (
              <div className="space-y-2.5 max-h-72 overflow-y-auto pr-1">
                {sentHistory.map((item, idx) => (
                  <div
                    key={idx}
                    className="p-3 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/40 text-xs space-y-1"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-slate-800 dark:text-slate-200">{item.title}</span>
                      <span className="text-[10px] text-slate-400 font-mono">{item.sentAt}</span>
                    </div>
                    <p className="text-slate-500 dark:text-slate-400 line-clamp-1">{item.body}</p>
                    <div className="flex items-center gap-2 text-[10px] font-mono text-indigo-600 dark:text-indigo-400 pt-1">
                      <span>مقصد: {item.target}</span>
                      <span>•</span>
                      <span>صفحه: {item.screen}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
