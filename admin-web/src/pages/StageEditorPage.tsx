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
  Copy,
  Check,
  AlertTriangle,
  RotateCcw,
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
  const [activeTab, setActiveTab] = useState<'form' | 'json' | 'both'>('form');

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
    } catch {
      setJsonError('فرمت JSON نامعتبر است');
    }
  };

  const handleSubmit = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();

    if (jsonError) {
      error('لطفاً خطای ساختار JSON را پیش از ذخیره برطرف کنید');
      return;
    }

    if (!formData.id.trim()) {
      error('شناسه مرحله الزامی است');
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
      error('هدف انگلیسی مرحله الزامی است');
      return;
    }

    if (!formData.targetObjectiveFa.trim()) {
      error('هدف فارسی مرحله الزامی است');
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
        <div className="w-8 h-8 border-2 border-indigo-500/20 border-t-indigo-600 rounded-full animate-spin" />
        <p className="text-xs text-slate-400">در حال دریافت اطلاعات...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Header: Focused and Quiet */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200/80 dark:border-slate-800/80">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/admin/stages')}
            className="p-2 rounded-xl text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800/60 transition-colors"
            title="بازگشت به لیست مراحل"
          >
            <ArrowRight className="w-5 h-5" />
          </button>
          <div className="flex items-center gap-2.5">
            <h1 className="text-lg sm:text-xl font-bold text-slate-900 dark:text-white tracking-tight">
              {isEditing ? formData.titleFa || formData.title || 'ویرایش مرحله' : 'مرحله جدید'}
            </h1>
            <Badge
              variant={formData.status === 'PUBLISHED' ? 'success' : formData.status === 'DRAFT' ? 'warning' : 'neutral'}
              size="sm"
            >
              {formData.status === 'PUBLISHED' ? 'منتشر شده' : formData.status === 'DRAFT' ? 'پیش‌نویس' : 'آرشیو'}
            </Badge>
          </div>
        </div>

        {/* View Switcher & Action Button */}
        <div className="flex items-center gap-3 self-end sm:self-auto">
          {/* Segmented Control */}
          <div className="bg-slate-100 dark:bg-[#0E1422] p-1 rounded-xl text-xs flex items-center border border-slate-200/60 dark:border-slate-800/60">
            <button
              type="button"
              onClick={() => setActiveTab('form')}
              className={`px-3 py-1.5 rounded-lg font-medium transition-all ${
                activeTab === 'form'
                  ? 'bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-2xs'
                  : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
              }`}
            >
              فرم
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('json')}
              className={`px-3 py-1.5 rounded-lg font-medium transition-all ${
                activeTab === 'json'
                  ? 'bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-2xs'
                  : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
              }`}
            >
              JSON
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('both')}
              className={`hidden lg:inline-block px-3 py-1.5 rounded-lg font-medium transition-all ${
                activeTab === 'both'
                  ? 'bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-2xs'
                  : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
              }`}
            >
              هر دو
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

      {/* Main Grid Container */}
      <div className={`grid gap-6 ${activeTab === 'both' ? 'lg:grid-cols-12' : 'grid-cols-1'}`}>
        {/* Form Column */}
        {(activeTab === 'both' || activeTab === 'form') && (
          <div className={`${activeTab === 'both' ? 'lg:col-span-7' : 'max-w-4xl mx-auto w-full'} space-y-6`}>
            {/* Card 1: Basic Info */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200/80 dark:border-slate-800/80 rounded-2xl p-6 shadow-2xs space-y-5">
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 tracking-tight pb-3 border-b border-slate-100 dark:border-slate-800/60">
                مشخصات اصلی
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="عنوان فارسی"
                  placeholder="خروج از فرودگاه"
                  value={formData.titleFa}
                  onChange={(e) => updateFormField('titleFa', e.target.value)}
                />

                <Input
                  label="عنوان انگلیسی"
                  placeholder="Airport Departure"
                  value={formData.title}
                  onChange={(e) => updateFormField('title', e.target.value)}
                  dir="ltr"
                />

                <Input
                  label="شناسه یکتا (ID)"
                  placeholder="stage-01-airport"
                  value={formData.id}
                  onChange={(e) => updateFormField('id', e.target.value)}
                  disabled={isEditing}
                  dir="ltr"
                />

                <Input
                  label="ترتیب نمایش"
                  type="number"
                  min={1}
                  value={formData.orderIndex}
                  onChange={(e) => updateFormField('orderIndex', parseInt(e.target.value) || 1)}
                  dir="ltr"
                />

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    وضعیت مرحله
                  </label>
                  <select
                    value={formData.status}
                    onChange={(e) => updateFormField('status', e.target.value as StageStatus)}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all"
                  >
                    <option value="DRAFT">پیش‌نویس</option>
                    <option value="PUBLISHED">منتشر شده</option>
                    <option value="ARCHIVED">بایگانی</option>
                  </select>
                </div>

                <div className="flex items-center gap-2.5 pt-6">
                  <input
                    id="shiftSubsequent"
                    type="checkbox"
                    checked={formData.shiftSubsequent || false}
                    onChange={(e) => updateFormField('shiftSubsequent', e.target.checked)}
                    className="w-4 h-4 rounded text-indigo-600 bg-slate-100 dark:bg-slate-800 border-slate-300 dark:border-slate-700 focus:ring-indigo-500"
                  />
                  <label htmlFor="shiftSubsequent" className="text-xs text-slate-700 dark:text-slate-300 cursor-pointer select-none">
                    جابجایی خودکار مراحل بعدی
                  </label>
                </div>
              </div>
            </div>

            {/* Card 2: Scenario & Objectives */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200/80 dark:border-slate-800/80 rounded-2xl p-6 shadow-2xs space-y-4">
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 tracking-tight pb-3 border-b border-slate-100 dark:border-slate-800/60">
                سناریو و اهداف گفتگو
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    خلاصه سناریو (فارسی)
                  </label>
                  <textarea
                    rows={2}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                    placeholder="موقعیت فرودگاه، تحویل بار و..."
                    value={formData.briefingFa}
                    onChange={(e) => updateFormField('briefingFa', e.target.value)}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    خلاصه سناریو (انگلیسی)
                  </label>
                  <textarea
                    rows={2}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                    placeholder="Airport check-in scenario..."
                    value={formData.briefing}
                    onChange={(e) => updateFormField('briefing', e.target.value)}
                    dir="ltr"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    هدف اصلی گفتگو (فارسی)
                  </label>
                  <textarea
                    rows={2}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                    placeholder="دریافت کارت پرواز"
                    value={formData.targetObjectiveFa}
                    onChange={(e) => updateFormField('targetObjectiveFa', e.target.value)}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    هدف اصلی گفتگو (انگلیسی)
                  </label>
                  <textarea
                    rows={2}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                    placeholder="Obtain a boarding pass"
                    value={formData.targetObjective}
                    onChange={(e) => updateFormField('targetObjective', e.target.value)}
                    dir="ltr"
                  />
                </div>
              </div>
            </div>

            {/* Card 3: Character & Speech */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200/80 dark:border-slate-800/80 rounded-2xl p-6 shadow-2xs space-y-4">
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 tracking-tight pb-3 border-b border-slate-100 dark:border-slate-800/60">
                کاراکتر هوش مصنوعی و تنظیمات صدا
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="نام کاراکتر"
                  placeholder="Sarah"
                  value={formData.characterName}
                  onChange={(e) => updateFormField('characterName', e.target.value)}
                  dir="ltr"
                />

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    جنسیت کاراکتر
                  </label>
                  <select
                    value={formData.characterGender}
                    onChange={(e) => updateFormField('characterGender', e.target.value)}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all"
                  >
                    <option value="Woman">زن (Woman)</option>
                    <option value="Man">مرد (Man)</option>
                  </select>
                </div>

                <div className="sm:col-span-2">
                  <VoiceDropdown
                    value={formData.voiceId}
                    onChange={(voiceId) => updateFormField('voiceId', voiceId)}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    آغازگر مکالمه
                  </label>
                  <select
                    value={formData.initialSpeaker}
                    onChange={(e) => updateFormField('initialSpeaker', e.target.value)}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm px-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all"
                  >
                    <option value="Model">هوش مصنوعی (Model)</option>
                    <option value="User">کاربر زبان‌آموز (User)</option>
                  </select>
                </div>

                <Input
                  label="حداکثر دفعات تبادل (نوبت)"
                  type="number"
                  min={4}
                  max={50}
                  value={formData.maxTurns}
                  onChange={(e) => updateFormField('maxTurns', parseInt(e.target.value) || 12)}
                  dir="ltr"
                />

                <div className="sm:col-span-2">
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 select-none">
                    رفتار و لحن کاراکتر
                  </label>
                  <textarea
                    rows={2}
                    className="w-full rounded-xl bg-white dark:bg-[#0E1422] border border-slate-300 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 dark:focus:border-indigo-500 transition-all leading-relaxed"
                    placeholder="میزان سخت‌گیری، شروط راستی‌آزمایی و نحوه پاسخگویی کاراکتر..."
                    value={formData.characterBehavior || ''}
                    onChange={(e) => updateFormField('characterBehavior', e.target.value)}
                  />
                </div>
              </div>
            </div>

            {/* Card 4: Clean Media Section (Side-by-Side Images) */}
            <div className="bg-white dark:bg-[#111726] border border-slate-200/80 dark:border-slate-800/80 rounded-2xl p-6 shadow-2xs space-y-4">
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 tracking-tight pb-3 border-b border-slate-100 dark:border-slate-800/60">
                تصاویر مرحله
              </h2>

              <div className="flex flex-wrap items-start gap-8 pt-1">
                {/* Scenario Background (Vertical 9:16) */}
                <ImageDropzone
                  label="تصویر پس‌زمینه سناریو"
                  value={formData.backgroundUrl}
                  onChange={(url) => updateFormField('backgroundUrl', url)}
                  aspectRatio="portrait"
                />

                {/* Character Avatar (Square 1:1) */}
                <ImageDropzone
                  label="آواتار کاراکتر (اختیاری)"
                  value={formData.characterAvatarUrl}
                  onChange={(url) => updateFormField('characterAvatarUrl', url)}
                  aspectRatio="square"
                />
              </div>
            </div>
          </div>
        )}

        {/* JSON Column */}
        {(activeTab === 'both' || activeTab === 'json') && (
          <div className={`${activeTab === 'both' ? 'lg:col-span-5' : 'max-w-4xl mx-auto w-full'} space-y-4`}>
            <div className="sticky top-20 bg-white dark:bg-[#111726] border border-slate-200/80 dark:border-slate-800/80 rounded-2xl p-5 shadow-2xs flex flex-col h-[calc(100vh-7.5rem)]">
              {/* JSON Toolbar */}
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800/60">
                <div className="flex items-center gap-2">
                  <Code2 className="w-4 h-4 text-indigo-500" />
                  <span className="text-xs font-semibold text-slate-800 dark:text-slate-200">کد JSON</span>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={handleFormatJson}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 text-xs flex items-center gap-1 transition-colors"
                    title="مرتب‌سازی"
                  >
                    <RotateCcw className="w-3.5 h-3.5" />
                    <span>مرتب‌سازی</span>
                  </button>

                  <button
                    type="button"
                    onClick={handleCopyJson}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 text-xs flex items-center gap-1 transition-colors"
                    title="کپی"
                  >
                    {copied ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copied ? 'کپی شد' : 'کپی'}</span>
                  </button>
                </div>
              </div>

              {/* JSON Error */}
              {jsonError && (
                <div className="my-2 p-2.5 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-500/30 text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 text-rose-500 shrink-0" />
                  <span className="truncate">{jsonError}</span>
                </div>
              )}

              {/* Textarea */}
              <div className="flex-1 mt-3 relative rounded-xl overflow-hidden border border-slate-200 dark:border-slate-800 bg-slate-900 shadow-inner">
                <textarea
                  value={jsonText}
                  onChange={(e) => handleJsonChange(e.target.value)}
                  className="w-full h-full p-3.5 bg-transparent text-emerald-400 focus:outline-none resize-none font-mono text-xs leading-relaxed selection:bg-indigo-500/30"
                  dir="ltr"
                  spellCheck={false}
                />
              </div>

              {/* Status Footer */}
              <div className="pt-2.5 mt-2 border-t border-slate-100 dark:border-slate-800/60 flex items-center justify-between text-[11px] text-slate-400 font-mono">
                <span className="flex items-center gap-1.5">
                  <span className={`w-1.5 h-1.5 rounded-full ${jsonError ? 'bg-rose-500' : 'bg-emerald-500'}`} />
                  {jsonError ? 'خطا در JSON' : 'همگام‌سازی فعال'}
                </span>
                <span>UTF-8</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
