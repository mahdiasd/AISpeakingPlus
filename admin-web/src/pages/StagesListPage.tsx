import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useToast } from '../context/ToastContext';
import { Stage, StageStatus, KOKORO_VOICES } from '../types';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { StageThumbnail } from '../components/ui/StageThumbnail';
import {
  Plus,
  Search,
  Filter,
  Layers,
  ChevronUp,
  ChevronDown,
  Edit2,
  Trash2,
  Volume2,
  AlertCircle,
  UserCheck,
} from 'lucide-react';

export const StagesListPage: React.FC = () => {
  const navigate = useNavigate();
  const { success, error } = useToast();

  const [stages, setStages] = useState<Stage[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<StageStatus | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Reorder loading state tracking
  const [reorderingId, setReorderingId] = useState<string | null>(null);

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [stageToDelete, setStageToDelete] = useState<Stage | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchStages = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.getStages(filterStatus);
      // Sort by orderIndex ascending
      data.sort((a, b) => a.orderIndex - b.orderIndex);
      setStages(data);
    } catch (err: any) {
      error(err.message || 'خطا در بارگذاری لیست مراحل');
    } finally {
      setIsLoading(false);
    }
  }, [filterStatus, error]);

  useEffect(() => {
    fetchStages();
  }, [fetchStages]);

  const handleMoveOrder = async (stage: Stage, direction: 'up' | 'down') => {
    const currentIndex = stages.findIndex((s) => s.id === stage.id);
    if (currentIndex === -1) return;

    const targetIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;
    if (targetIndex < 0 || targetIndex >= stages.length) return;

    const targetStage = stages[targetIndex];
    setReorderingId(stage.id);

    try {
      await api.reorderStage(stage.id, targetStage.orderIndex, false);
      success(`ترتیب مرحله ${stage.title} تغییر یافت`);
      await fetchStages();
    } catch (err: any) {
      error(err.message || 'خطا در تغییر ترتیب مرحله');
    } finally {
      setReorderingId(null);
    }
  };

  const confirmDelete = async () => {
    if (!stageToDelete) return;
    setIsDeleting(true);
    try {
      await api.deleteStage(stageToDelete.id);
      success('مرحله با موفقیت حذف یا بایگانی شد');
      setDeleteModalOpen(false);
      setStageToDelete(null);
      await fetchStages();
    } catch (err: any) {
      error(err.message || 'خطا در حذف مرحله');
    } finally {
      setIsDeleting(false);
    }
  };

  const filteredStages = stages.filter((stage) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      stage.title.toLowerCase().includes(q) ||
      stage.titleFa.toLowerCase().includes(q) ||
      stage.id.toLowerCase().includes(q) ||
      stage.characterName.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-4">
      {/* Header and Add Action */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 dark:text-[#f1f3f5] tracking-tight">
            مراحل مکالمه
          </h1>
          <Badge variant="purple" size="md">
            {stages.length} مرحله
          </Badge>
        </div>

        <Button
          onClick={() => navigate('/admin/stages/new')}
          icon={<Plus className="w-4 h-4" />}
          size="md"
        >
          ایجاد مرحله جدید
        </Button>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl p-3 sm:p-4 shadow-2xs flex flex-col md:flex-row items-center justify-between gap-3">
        {/* Filter Pills */}
        <div className="flex items-center gap-2 w-full md:w-auto overflow-x-auto pb-1 md:pb-0">
          <span className="text-xs font-semibold text-slate-400 dark:text-[#858a94] ml-1 hidden sm:inline-flex items-center gap-1.5 uppercase tracking-wider">
            <Filter className="w-3.5 h-3.5 text-indigo-500" /> وضعیت:
          </span>
          {(['ALL', 'PUBLISHED', 'DRAFT', 'ARCHIVED'] as const).map((st) => (
            <button
              key={st}
              onClick={() => setFilterStatus(st)}
              className={`text-xs sm:text-sm px-3.5 py-1.5 rounded-xl font-medium transition-all ${
                filterStatus === st
                  ? 'bg-indigo-50 text-indigo-700 border border-indigo-200 shadow-2xs dark:bg-[#282a30] dark:text-[#f1f3f5] dark:border-[#383b44]'
                  : 'bg-slate-100 dark:bg-[#24262b] text-slate-600 dark:text-[#9ca3af] hover:text-slate-900 dark:hover:text-[#f1f3f5] hover:bg-slate-200/70 dark:hover:bg-[#282a30] border border-transparent'
              }`}
            >
              {st === 'ALL' && 'همه مراحل'}
              {st === 'PUBLISHED' && 'منتشر شده'}
              {st === 'DRAFT' && 'پیش‌نویس'}
              {st === 'ARCHIVED' && 'بایگانی'}
            </button>
          ))}
        </div>

        {/* Search Input */}
        <div className="relative w-full md:w-80">
          <input
            type="text"
            placeholder="جستجوی عنوان، شناسه یا کاراکتر..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full text-sm rounded-xl bg-slate-50 dark:bg-[#24262b] border border-slate-300 dark:border-[#363842] pl-9 pr-4 py-2 text-slate-900 dark:text-[#f1f3f5] placeholder-slate-400 dark:placeholder-[#858a94] focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
        </div>
      </div>

      {/* Table Container */}
      <div className="bg-white dark:bg-[#202125] border border-slate-200 dark:border-[#2d3037] rounded-2xl shadow-2xs overflow-hidden">
        {isLoading ? (
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <div className="w-8 h-8 border-3 border-indigo-500/20 border-t-indigo-600 dark:border-t-indigo-400 rounded-full animate-spin" />
            <p className="text-sm font-medium text-slate-500 dark:text-[#9ca3af]">در حال دریافت فهرست مراحل...</p>
          </div>
        ) : filteredStages.length === 0 ? (
          <div className="text-center py-16 px-4">
            <div className="w-12 h-12 rounded-2xl bg-slate-100 dark:bg-[#282a30] border border-slate-200 dark:border-[#383b44] text-slate-600 dark:text-slate-300 flex items-center justify-center mx-auto mb-3 shadow-2xs">
              <Layers className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-slate-800 dark:text-[#f1f3f5]">هیچ مرحله‌ای یافت نشد</h3>
            <p className="text-sm text-slate-500 dark:text-[#9ca3af] mt-1 max-w-sm mx-auto">
              {searchQuery ? 'موردی با این عبارت جستجو یافت نشد.' : 'هنوز مرحله‌ای در این وضعیت ثبت نشده است.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-right border-collapse">
              <thead>
                <tr className="border-b border-slate-200 dark:border-[#2d3037] bg-slate-50/70 dark:bg-[#1b1c1f] text-xs font-semibold text-slate-500 dark:text-[#9ca3af] uppercase tracking-wider">
                  <th className="py-3.5 px-4 w-16 text-center">ترتیب</th>
                  <th className="py-3.5 px-4">تصویر سناریو</th>
                  <th className="py-3.5 px-4">عنوان سناریو و جزئیات</th>
                  <th className="py-3.5 px-4 hidden lg:table-cell">تنظیمات صدا و هوش مصنوعی</th>
                  <th className="py-3.5 px-4 text-center">وضعیت</th>
                  <th className="py-3.5 px-4 text-left">عملیات</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-[#2d3037] text-sm">
                {filteredStages.map((stage, idx) => {
                  const voice = KOKORO_VOICES.find((v) => v.id === stage.voiceId);
                  const isFirst = idx === 0;
                  const isLast = idx === filteredStages.length - 1;

                  // Use main background image first, with avatar as fallback, or SVG placeholder
                  const primaryImageUrl = stage.backgroundUrl || stage.characterAvatarUrl || null;

                  return (
                    <tr
                      key={stage.id}
                      className="hover:bg-slate-50/80 dark:hover:bg-[#282a30] transition-colors group"
                    >
                      {/* Order Controls */}
                      <td className="py-3.5 px-4 text-center">
                        <div className="flex flex-col items-center justify-center gap-0.5">
                          <button
                            disabled={isFirst || reorderingId === stage.id}
                            onClick={() => handleMoveOrder(stage, 'up')}
                            className="p-1 rounded-lg text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-slate-100 dark:hover:bg-[#24262b] disabled:opacity-20 disabled:hover:bg-transparent transition-colors"
                            title="انتقال به بالا"
                          >
                            <ChevronUp className="w-4 h-4" />
                          </button>
                          <span className="font-mono font-bold text-slate-700 dark:text-[#f1f3f5] text-sm tabular-nums">
                            {stage.orderIndex}
                          </span>
                          <button
                            disabled={isLast || reorderingId === stage.id}
                            onClick={() => handleMoveOrder(stage, 'down')}
                            className="p-1 rounded-lg text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-slate-100 dark:hover:bg-[#24262b] disabled:opacity-20 disabled:hover:bg-transparent transition-colors"
                            title="انتقال به پایین"
                          >
                            <ChevronDown className="w-4 h-4" />
                          </button>
                        </div>
                      </td>

                      {/* Main Scenario Image with robust fallback */}
                      <td className="py-3.5 px-4">
                        <StageThumbnail
                          src={primaryImageUrl}
                          alt={stage.titleFa || stage.title}
                          aspectRatio="portrait"
                          fallbackTitle={stage.titleFa}
                        />
                      </td>

                      {/* Scenario Title, ID and Character Info */}
                      <td className="py-3.5 px-4">
                        <div className="space-y-1">
                          <div className="font-bold text-slate-900 dark:text-[#f1f3f5] text-sm sm:text-base">
                            {stage.titleFa}
                          </div>
                          <div className="text-xs text-slate-500 dark:text-[#9ca3af] font-mono" dir="ltr">
                            {stage.title}
                          </div>
                          <div className="flex flex-wrap items-center gap-2 pt-0.5">
                            <span className="inline-block text-xs text-slate-700 dark:text-[#d1d5db] font-mono bg-slate-100 dark:bg-[#24262b] px-2 py-0.5 rounded-md border border-slate-200 dark:border-[#343741]" dir="ltr">
                              {stage.id}
                            </span>
                            <span className="inline-flex items-center gap-1.5 text-xs text-slate-600 dark:text-[#9ca3af]">
                              {stage.characterAvatarUrl ? (
                                <img
                                  src={stage.characterAvatarUrl}
                                  alt={stage.characterName}
                                  className="w-5 h-5 rounded-full object-cover border border-slate-200 dark:border-slate-700 shadow-2xs"
                                />
                              ) : (
                                <div
                                  className="w-5 h-5 rounded-full bg-slate-200 dark:bg-[#282a30] text-slate-600 dark:text-slate-300 border border-slate-300 dark:border-[#383b44] flex items-center justify-center font-bold text-[10px] select-none shadow-2xs"
                                  title="بدون تصویر آواتار (آواتار پیش‌فرض)"
                                >
                                  {stage.characterName ? stage.characterName.trim()[0].toUpperCase() : <UserCheck className="w-3 h-3" />}
                                </div>
                              )}
                              هم‌صحبت: <strong className="font-medium text-slate-800 dark:text-[#e5e7eb]">{stage.characterName || 'نامشخص'}</strong>
                              <span className="text-slate-400">({stage.characterGender === 'Woman' ? 'زن' : 'مرد'})</span>
                            </span>
                          </div>
                          <p className="text-xs text-slate-500 dark:text-[#9ca3af] line-clamp-1 max-w-md pt-0.5">
                            {stage.briefingFa || stage.briefing}
                          </p>
                        </div>
                      </td>

                      {/* Voice & AI Speech Settings */}
                      <td className="py-3.5 px-4 hidden lg:table-cell">
                        <div className="space-y-1.5">
                          <div className="flex items-center gap-1.5 text-slate-700 dark:text-slate-300">
                            <Volume2 className="w-4 h-4 text-indigo-500" />
                            <span className="font-medium text-sm">
                              {voice ? voice.name : stage.voiceId || 'پیش‌فرض'}
                            </span>
                            {voice && (
                              <Badge variant={voice.accent === 'US' ? 'info' : 'purple'} size="sm">
                                {voice.accent}
                              </Badge>
                            )}
                          </div>
                          <div className="text-xs text-slate-500 dark:text-[#9ca3af]">
                            شروع: <span className="font-medium text-slate-700 dark:text-slate-300">{stage.initialSpeaker === 'Model' ? 'هوش مصنوعی' : 'کاربر'}</span> &bull; حداکثر {stage.maxTurns} دور
                          </div>
                        </div>
                      </td>

                      {/* Status Badge */}
                      <td className="py-3.5 px-4 text-center">
                        <Badge
                          variant={
                            stage.status === 'PUBLISHED'
                              ? 'success'
                              : stage.status === 'DRAFT'
                              ? 'warning'
                              : 'neutral'
                          }
                          pulse={stage.status === 'PUBLISHED'}
                        >
                          {stage.status === 'PUBLISHED' && 'منتشر شده'}
                          {stage.status === 'DRAFT' && 'پیش‌نویس'}
                          {stage.status === 'ARCHIVED' && 'بایگانی'}
                        </Badge>
                      </td>

                      {/* Actions */}
                      <td className="py-3.5 px-4 text-left">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => navigate(`/admin/stages/${stage.id}`)}
                            className="p-2 rounded-xl text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-[#282a30] transition-all"
                            title="ویرایش مرحله"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => {
                              setStageToDelete(stage);
                              setDeleteModalOpen(true);
                            }}
                            className="p-2 rounded-xl text-slate-500 dark:text-slate-400 hover:text-rose-600 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-500/10 transition-all"
                            title="حذف مرحله"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="تأیید حذف مرحله"
        maxWidth="sm"
      >
        <div className="space-y-5">
          <div className="flex items-start gap-3 p-3.5 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-500/30 text-rose-800 dark:text-rose-200 text-sm">
            <AlertCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold">آیا از حذف یا بایگانی این مرحله اطمینان دارید؟</p>
              <p className="mt-1 text-slate-600 dark:text-slate-300 text-xs leading-relaxed">
                مرحله «{stageToDelete?.titleFa || stageToDelete?.title}» از لیست مراحل حذف خواهد شد.
              </p>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2.5 pt-2">
            <Button
              variant="ghost"
              onClick={() => setDeleteModalOpen(false)}
              disabled={isDeleting}
            >
              انصراف
            </Button>
            <Button
              variant="danger"
              onClick={confirmDelete}
              isLoading={isDeleting}
              icon={<Trash2 className="w-4 h-4" />}
            >
              حذف مرحله
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
