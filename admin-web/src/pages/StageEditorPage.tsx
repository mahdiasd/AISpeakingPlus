import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { StageStatus, StageUpsertPayload } from '../types';
import { VoiceDropdown } from '../components/stages/VoiceDropdown';
import { ImageDropzone } from '../components/stages/ImageDropzone';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import {
  Save,
  ArrowRight,
  Code2,
  Sliders,
  Copy,
  Check,
  AlertTriangle,
  RotateCcw,
  Sparkles,
  ChevronDown,
  ChevronUp,
  Image as ImageIcon,
  User,
} from 'lucide-react';

const DEFAULT_STAGE: StageUpsertPayload = {
  id: '',
  orderIndex: 1,
  title: '',
  titleFa: '',
  briefing: '',
  briefingFa: '',
  targetObjective: '',
  targetObjectiveFa: '',
  characterBehavior: '',
  backgroundUrl: '',
  characterName: '',
  characterAvatarUrl: '',
  characterGender: 'Woman',
  voiceId: 'af_sarah',
  initialSpeaker: 'Model',
  maxTurns: 12,
  status: 'DRAFT',
  shiftSubsequent: false,
};

export const StageEditorPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id && id !== 'new');
  const navigate = useNavigate();
  const { success, error } = useToast();

  const [formData, setFormData] = useState<StageUpsertPayload>(DEFAULT_STAGE);
  const [jsonText, setJsonText] = useState<string>('');
  const [jsonError, setJsonError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [copied, setCopied] = useState(false);
  const [activeTab, setActiveTab] = useState<'both' | 'form' | 'json'>('both');
  const [showOptionalAvatar, setShowOptionalAvatar] = useState(false);

  // Guard to prevent circular loops
  const syncSourceRef = useRef<'form' | 'json' | null>(null);

  // Load existing stage if editing
  useEffect(() => {
    if (isEditing && id) {
      setIsLoading(true);
      api
        .getStage(id)
        .then((stage) => {
          const payload: StageUpsertPayload = {
            id: stage.id,
            orderIndex: stage.orderIndex,
            title: stage.title,
            titleFa: stage.titleFa,
            briefing: stage.briefing,
            briefingFa: stage.briefingFa,
            targetObjective: stage.targetObjective,
            targetObjectiveFa: stage.targetObjectiveFa || '',
            characterBehavior: stage.characterBehavior || '',
            backgroundUrl: stage.backgroundUrl || '',
            characterName: stage.characterName,
            characterAvatarUrl: stage.characterAvatarUrl || '',
            characterGender: stage.characterGender,
            voiceId: stage.voiceId || 'af_sarah',
            initialSpeaker: stage.initialSpeaker,
            maxTurns: stage.maxTurns,
            status: stage.status,
            shiftSubsequent: false,
          };
          setFormData(payload);
          setJsonText(JSON.stringify(payload, null, 2));
          if (stage.characterAvatarUrl) {
            setShowOptionalAvatar(true);
          }
        })
        .catch((err) => {
          error(err.message || 'خطا در بارگذاری مشخصات مرحله');
          navigate('/admin/stages');
        })
        .finally(() => setIsLoading(false));
    } else {
      setJsonText(JSON.stringify(DEFAULT_STAGE, null, 2));
    }
  }, [id, isEditing, navigate]);

  // Sync Form -> JSON
  const updateFormField = useCallback(<K extends keyof StageUpsertPayload>(field: K, value: StageUpsertPayload[K]) => {
    setFormData((prev) => {
      const next = { ...prev, [field]: value };
      syncSourceRef.current = 'form';
      setJsonText(JSON.stringify(next, null, 2));
      setJsonError(null);
      return next;
    });
  }, []);

  // Sync JSON -> Form
  const handleJsonChange = (newText: string) => {
    setJsonText(newText);
    syncSourceRef.current = 'json';

    try {
      const parsed = JSON.parse(newText);
      setJsonError(null);
      setFormData((prev) => ({
        ...prev,
        ...parsed,
      }));
      if (parsed.characterAvatarUrl) {
        setShowOptionalAvatar(true);
      }
    } catch (err: any) {
      setJsonError(err.message || 'فرمت JSON نامعتبر است');
    }
  };

  const handleCopyJson = () => {
    navigator.clipboard.writeText(jsonText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleFormatJson = () => {
    try {
      const parsed = JSON.parse(jsonText);
      setJsonText(JSON.stringify(parsed, null, 2));
      setJsonError(null);
    } catch (err: any) {
      setJsonError('امکان مرتب‌سازی وجود ندارد؛ ابتدا خطای JSON را برطرف کنید');
    }
  };

  const handleSubmit = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();

    if (jsonError) {
      error('لطفاً خطای ساختار JSON را پیش از ذخیره برطرف کنید');
      return;
    }

    if (!formData.id.trim()) {
      error('شناسه یکتا (ID) برای مرحله الزامی است');
      return;
    }

    if (!formData.title.trim()) {
      error('عنوان انگلیسی مرحله الزامی است');
      return;
    }

    if (!formData.titleFa.trim()) {
      error('عنوان فارسی مرحله الزامی است');
      return;
    }

    if (!formData.targetObjective.trim()) {
      error('هدف داستانی انگلیسی مرحله الزامی است');
      return;
    }

    if (!formData.targetObjectiveFa.trim()) {
      error('هدف داستانی فارسی مرحله الزامی است');
      return;
    }

    setIsSaving(true);
    try {
      await api.upsertStage(formData);
      success(isEditing ? 'مرحله با موفقیت به‌روزرسانی شد' : 'مرحله جدید با موفقیت ایجاد شد');
      navigate('/admin/stages');
    } catch (err: any) {
      error(err.message || 'خطا در ذخیره مرحله');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-24 gap-3">
        <div className="w-10 h-10 border-4 border-indigo-500/20 border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin" />
        <p className="text-sm font-medium text-slate-500 dark:text-slate-400">در حال بارگذاری اطلاعات مرحله...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Breadcrumb & Action Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-5 border-b border-slate-200 dark:border-slate-800">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/admin/stages')}
            className="p-2.5 rounded-xl bg-white dark:bg-[#111726] hover:bg-slate-100 dark:hover:bg-[#161F33] text-slate-600 dark:text-slate-300 border border-slate-300 dark:border-slate-800 transition-colors shadow-xs"
            title="بازگشت به لیست مراحل"
          >
            <ArrowRight className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
                {isEditing ? `ویرایش مرحله: ${formData.titleFa || formData.title}` : 'ایجاد مرحله مکالمه جدید'}
              </h1>
              <Badge variant={formData.status === 'PUBLISHED' ? 'success' : formData.status === 'DRAFT' ? 'warning' : 'neutral'} size="md">
                {formData.status === 'PUBLISHED' ? 'منتشر شده' : formData.status === 'DRAFT' ? 'پیش‌نویس' : 'آرشیو'}
              </Badge>
            </div>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-0.5">
              ویرایشگر دوطرفه فرم بصری و کد خام JSON با همگام‌ساز زنده
            </p>
          </div>
        </div>

        {/* View Switcher & Save Button */}
        <div className="flex items-center gap-3 self-end sm:self-auto">
          {/* View Mode Toggle */}
          <div className="hidden lg:flex bg-slate-100 dark:bg-[#0E1422] border border-slate-200 dark:border-slate-800 p-1 rounded-xl text-xs">
            <button
              onClick={() => setActiveTab('both')}
              className={`px-3.5 py-1.5 rounded-lg transition-all ${
                activeTab === 'both'
                  ? 'bg-white dark:bg-indigo-500/15 text-indigo-700 dark:text-indigo-300 font-semibold shadow-xs border border-slate-200/80 dark:border-indigo-500/30'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              نمای دوطرفه
            </button>
            <button
              onClick={() => setActiveTab('form')}
              className={`px-3.5 py-1.5 rounded-lg transition-all ${
                activeTab === 'form'
                  ? 'bg-white dark:bg-indigo-500/15 text-indigo-700 dark:text-indigo-300 font-semibold shadow-xs border border-slate-200/80 dark:border-indigo-500/30'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              فقط فرم
            </button>
            <button
              onClick={() => setActiveTab('json')}
              className={`px-3.5 py-1.5 rounded-lg transition-all ${
                activeTab === 'json'
                  ? 'bg-white dark:bg-indigo-500/15 text-indigo-700 dark:text-indigo-300 font-semibold shadow-xs border border-slate-200/80 dark:border-indigo-500/30'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              فقط JSON
            </button>
          </div>

          <Button
            type="button"
            onClick={() => handleSubmit()}
            isLoading={isSaving}
            icon={<Save className="w-4 h-4" />}
            size="md"
          >
            {isEditing ? 'ذخیره تغییرات' : 'ایجاد مرحله'}
          </Button>
        </div>
      </div>

      {/* Editor Grid Container */}
      <div className={`grid gap-6 ${activeTab === 'both' ? 'lg:grid-cols-12' : 'grid-cols-1'}`}>
        {/* RIGHT / MAIN: Visual Interactive Form */}
        {(activeTab === 'both' || activeTab === 'form') && (
          <div className={`${activeTab === 'both' ? 'lg:col-span-7' : 'w-full'} space-y-6`}>
            {/* Basic Info Card */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs space-y-5">
              <div className="flex items-center gap-2.5 pb-4 border-b border-slate-200 dark:border-slate-800">
                <Sliders className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                <h2 className="text-base font-bold text-slate-900 dark:text-slate-100 tracking-tight">
                  مشخصات اصلی و ترتیبی مرحله
                </h2>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <div>
                  <Input
                    label="شناسه یکتا (Stage ID)"
                    placeholder="stage-01-airport"
                    value={formData.id}
                    onChange={(e) => updateFormField('id', e.target.value)}
                    disabled={isEditing}
                    helperText={isEditing ? 'شناسه مرحله پس از ایجاد غیرقابل ویرایش است' : 'شناسه با حروف انگلیسی کوچک و خط تیره'}
                    dir="ltr"
                  />
                </div>

                <div>
                  <Input
                    label="شماره ترتیب نمایش (Order Index)"
                    type="number"
                    min={1}
                    value={formData.orderIndex}
                    onChange={(e) => updateFormField('orderIndex', parseInt(e.target.value) || 1)}
                    dir="ltr"
                  />
                </div>

                <div>
                  <Input
                    label="عنوان انگلیسی (Title)"
                    placeholder="Tehran Airport Departure"
                    value={formData.title}
                    onChange={(e) => updateFormField('title', e.target.value)}
                    dir="ltr"
                  />
                </div>

                <div>
                  <Input
                    label="عنوان فارسی (Title FA)"
                    placeholder="خروج از فرودگاه امام تهران"
                    value={formData.titleFa}
                    onChange={(e) => updateFormField('titleFa', e.target.value)}
                  />
                </div>
              </div>

              {/* Status and Shift Subsequent */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 pt-2">
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
                    وضعیت انتشار مرحله
                  </label>
                  <select
                    value={formData.status}
                    onChange={(e) => updateFormField('status', e.target.value as StageStatus)}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all"
                  >
                    <option value="DRAFT">پیش‌نویس (DRAFT - فقط ادمین)</option>
                    <option value="PUBLISHED">منتشر شده (PUBLISHED - در دسترس کاربران)</option>
                    <option value="ARCHIVED">بایگانی شده (ARCHIVED)</option>
                  </select>
                </div>

                <div className="flex items-center gap-3 pt-6">
                  <input
                    id="shiftSubsequent"
                    type="checkbox"
                    checked={formData.shiftSubsequent || false}
                    onChange={(e) => updateFormField('shiftSubsequent', e.target.checked)}
                    className="w-4 h-4 rounded text-indigo-600 bg-slate-100 dark:bg-slate-800 border-slate-300 dark:border-slate-700 focus:ring-indigo-500"
                  />
                  <label htmlFor="shiftSubsequent" className="text-sm text-slate-700 dark:text-slate-300 cursor-pointer select-none">
                    جابجایی خودکار مراحل بعدی (Shift Subsequent)
                  </label>
                </div>
              </div>
            </div>

            {/* Scenario & Objectives Card */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs space-y-5">
              <h2 className="text-base font-bold text-slate-900 dark:text-slate-100 pb-4 border-b border-slate-200 dark:border-slate-800 tracking-tight">
                سناریو، برگه ماموریت و هدف آموزشی
              </h2>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                  خلاصه سناریو انگلیسی (Briefing)
                </label>
                <textarea
                  rows={2}
                  className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                  placeholder="You are at the check-in desk at Tehran airport. Request a window seat."
                  value={formData.briefing}
                  onChange={(e) => updateFormField('briefing', e.target.value)}
                  dir="ltr"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                  خلاصه سناریو فارسی (Briefing FA)
                </label>
                <textarea
                  rows={2}
                  className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                  placeholder="شما در باجه پذیرش فرودگاه هستید و باید کارت پرواز بگیرید."
                  value={formData.briefingFa}
                  onChange={(e) => updateFormField('briefingFa', e.target.value)}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                  هدف اصلی ماموریت مکالمه به فارسی (Target Objective FA)
                  <span className="text-red-500 mr-1">*</span>
                </label>
                <textarea
                  rows={2}
                  className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                  placeholder="گرفتن کارت پرواز و اطمینان از صندلی کنار پنجره"
                  value={formData.targetObjectiveFa}
                  onChange={(e) => updateFormField('targetObjectiveFa', e.target.value)}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                  هدف اصلی ماموریت مکالمه به انگلیسی (Target Objective EN)
                  <span className="text-red-500 mr-1">*</span>
                </label>
                <textarea
                  rows={2}
                  className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                  placeholder="Obtain a boarding pass and confirm window seat"
                  value={formData.targetObjective}
                  onChange={(e) => updateFormField('targetObjective', e.target.value)}
                  dir="ltr"
                />
              </div>
            </div>

            {/* Character & AI Speech Config Card */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs space-y-5">
              <h2 className="text-base font-bold text-slate-900 dark:text-slate-100 pb-4 border-b border-slate-200 dark:border-slate-800 tracking-tight">
                شخصیت هم‌صحبت و تنظیمات هوش مصنوعی
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <div>
                  <Input
                    label="نام کاراکتر هوش مصنوعی (Character Name)"
                    placeholder="Sarah"
                    value={formData.characterName}
                    onChange={(e) => updateFormField('characterName', e.target.value)}
                    dir="ltr"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    جنسیت کاراکتر
                  </label>
                  <select
                    value={formData.characterGender}
                    onChange={(e) => updateFormField('characterGender', e.target.value)}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all"
                  >
                    <option value="Woman">زن (Woman)</option>
                    <option value="Man">مرد (Man)</option>
                  </select>
                </div>
              </div>

              {/* Character Behavior & Friction */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                  رفتار کاراکتر و منطق چالش هوش مصنوعی (Character Behavior & Friction)
                </label>
                <textarea
                  rows={3}
                  className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                  placeholder="مثال: کاراکتر باید محتاط باشد و تا زمانی که کاربر کد تایید یا نام کامل را نگفته، کلید را تحویل ندهد. از راهنمایی مستقیم خودداری کند."
                  value={formData.characterBehavior || ''}
                  onChange={(e) => updateFormField('characterBehavior', e.target.value)}
                />
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                  لحن، میزان سخت‌گیری، شروط راستی‌آزمایی و نحوه پاسخگویی AI بدون راهنمایی مستقیم (Anti-Spoon-feeding).
                </p>
              </div>

              {/* Voice Dropdown */}
              <VoiceDropdown
                value={formData.voiceId}
                onChange={(voiceId) => updateFormField('voiceId', voiceId)}
              />

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 pt-2">
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    آغازگر مکالمه
                  </label>
                  <select
                    value={formData.initialSpeaker}
                    onChange={(e) => updateFormField('initialSpeaker', e.target.value)}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 dark:focus:ring-indigo-500/25 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all"
                  >
                    <option value="Model">هوش مصنوعی (Model)</option>
                    <option value="User">کاربر زبان‌آموز (User)</option>
                  </select>
                </div>

                <div>
                  <Input
                    label="حداکثر دفعات تبادل (Max Turns)"
                    type="number"
                    min={4}
                    max={50}
                    value={formData.maxTurns}
                    onChange={(e) => updateFormField('maxTurns', parseInt(e.target.value) || 12)}
                    dir="ltr"
                  />
                </div>
              </div>
            </div>

            {/* Media Uploads Card - Main Cover First, Avatar Optional */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs space-y-6">
              <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800">
                <div className="flex items-center gap-2.5">
                  <ImageIcon className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                  <div>
                    <h2 className="text-base font-bold text-slate-900 dark:text-slate-100 tracking-tight">
                      تصویر شاخص محیط سناریو (اصلی)
                    </h2>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                      این تصویر به عنوان کاور اصلی در لیست مراحل و پس‌زمینه عمودی تمام‌صفحه موبایل نمایش داده می‌شود.
                    </p>
                  </div>
                </div>
              </div>

              {/* Main Stage Background Image */}
              <ImageDropzone
                label="تصویر پس‌زمینه سناریو (محیط مکالمه)"
                value={formData.backgroundUrl}
                onChange={(url) => updateFormField('backgroundUrl', url)}
                aspectRatio="portrait"
                helperText="تصویر عمودی تمام‌صفحه مخصوص گوشی موبایل (نسبت ۹:۱۶ مانند 1080x1920 WebP, PNG, JPG)"
              />

              {/* Optional Avatar Section */}
              <div className="pt-2 border-t border-slate-100 dark:border-slate-800/80">
                <button
                  type="button"
                  onClick={() => setShowOptionalAvatar(!showOptionalAvatar)}
                  className="flex items-center justify-between w-full py-2 text-right group"
                >
                  <div className="flex items-center gap-2">
                    <User className="w-4 h-4 text-indigo-500" />
                    <span className="text-sm font-semibold text-slate-800 dark:text-slate-200 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                      تصویر آواتار کاراکتر (اختیاری)
                    </span>
                    <Badge variant="neutral" size="sm">
                      اختیاری
                    </Badge>
                  </div>
                  <div className="flex items-center gap-1.5 text-xs text-slate-400 group-hover:text-slate-600 dark:group-hover:text-slate-300">
                    <span>{showOptionalAvatar ? 'بستن' : 'افزودن آواتار اختصاصی'}</span>
                    {showOptionalAvatar ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                  </div>
                </button>

                {showOptionalAvatar && (
                  <div className="mt-4 pt-2">
                    <ImageDropzone
                      label="تصویر آواتار کاراکتر (اختیاری)"
                      value={formData.characterAvatarUrl}
                      onChange={(url) => updateFormField('characterAvatarUrl', url)}
                      aspectRatio="square"
                      helperText="در صورت خالی بودن، تصویر اصلی سناریو استفاده می‌شود."
                    />
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* LEFT: Live JSON Two-Way Synchronizer */}
        {(activeTab === 'both' || activeTab === 'json') && (
          <div className={`${activeTab === 'both' ? 'lg:col-span-5' : 'w-full'} space-y-4`}>
            <div className="sticky top-20 bg-white dark:bg-[#111726] border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-xs flex flex-col h-[calc(100vh-7.5rem)]">
              {/* JSON Header & Tools */}
              <div className="flex items-center justify-between pb-3.5 border-b border-slate-200 dark:border-slate-800">
                <div className="flex items-center gap-2">
                  <Code2 className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                  <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">کد JSON همگام‌ساز</span>
                  <span className="text-[11px] bg-indigo-50 text-indigo-700 dark:bg-indigo-500/10 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-500/20 px-2 py-0.5 rounded-md font-mono">
                    Live
                  </span>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={handleFormatJson}
                    className="p-1.5 rounded-lg text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800/60 text-xs flex items-center gap-1 transition-colors"
                    title="مرتب‌سازی JSON"
                  >
                    <RotateCcw className="w-3.5 h-3.5" />
                    <span className="hidden sm:inline">مرتب‌سازی</span>
                  </button>

                  <button
                    type="button"
                    onClick={handleCopyJson}
                    className="p-1.5 rounded-lg text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800/60 text-xs flex items-center gap-1 transition-colors"
                    title="کپی در کلیپ‌بورد"
                  >
                    {copied ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                    <span className="hidden sm:inline">{copied ? 'کپی شد' : 'کپی'}</span>
                  </button>
                </div>
              </div>

              {/* AI Prompt Guidance Callout */}
              <div className="my-3 p-3 rounded-xl bg-indigo-50 dark:bg-indigo-500/10 border border-indigo-200 dark:border-indigo-500/20 text-xs text-slate-700 dark:text-slate-300 flex items-start gap-2.5">
                <Sparkles className="w-4 h-4 text-indigo-600 dark:text-indigo-400 shrink-0 mt-0.5" />
                <p className="leading-relaxed">
                  می‌توانید خروجی پرامپت هوش مصنوعی (ChatGPT یا Claude) را مستقیماً در این باکس پیست کنید تا فرم به صورت خودکار پر شود.
                </p>
              </div>

              {/* JSON Syntax Error Alert */}
              {jsonError && (
                <div className="mb-2.5 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-500/30 text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 text-rose-500 shrink-0" />
                  <span className="truncate">{jsonError}</span>
                </div>
              )}

              {/* JSON Textarea Editor */}
              <div className="flex-1 relative font-mono text-xs rounded-xl overflow-hidden border border-slate-300 dark:border-slate-800 bg-slate-900 shadow-inner">
                <textarea
                  value={jsonText}
                  onChange={(e) => handleJsonChange(e.target.value)}
                  className="w-full h-full p-4 bg-transparent text-emerald-400 focus:outline-none resize-none font-mono text-xs leading-relaxed selection:bg-indigo-500/30"
                  dir="ltr"
                  spellCheck={false}
                />
              </div>

              {/* Live Status Footer */}
              <div className="pt-3 border-t border-slate-200 dark:border-slate-800 flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 font-mono">
                <span className="flex items-center gap-1.5">
                  <span className={`w-2 h-2 rounded-full ${jsonError ? 'bg-rose-500' : 'bg-emerald-500 animate-pulse'}`} />
                  {jsonError ? 'خطا در اسکیما' : 'همگام‌سازی بلادرنگ فعال'}
                </span>
                <span>UTF-8 JSON</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
