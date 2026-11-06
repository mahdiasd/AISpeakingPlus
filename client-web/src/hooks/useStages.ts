import { useState, useEffect, useCallback, useMemo } from 'react';
import { stageApi, saveLocalGuestProgress } from '../api/stageApi';
import { Stage, PhaseGroup, JourneyStats } from '../types/stage';

export function useStages() {
  const [stages, setStages] = useState<Stage[]>([]);
  const [currentTier, setCurrentTier] = useState<'GUEST' | 'REGISTERED' | 'SUBSCRIBER'>('GUEST');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedStage, setSelectedStage] = useState<Stage | null>(null);

  const fetchStages = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await stageApi.getStages();
      const sorted = [...(data.stages || [])].sort((a, b) => a.orderIndex - b.orderIndex);
      setStages(sorted);
      setCurrentTier(data.currentTier || 'GUEST');
    } catch (err: unknown) {
      console.error('Error fetching stages:', err);
      setError('خطا در دریافت مرحله‌های آموزشی. لطفاً اتصال اینترنت را بررسی کنید.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStages();
  }, [fetchStages]);

  // Derived state computed during render (Vercel React Best Practices: rerender-derived-state-no-effect)
  const currentStage = useMemo(() => {
    if (!stages.length) return null;
    // Current stage is the first unlocked stage with 0 stars, or first uncompleted stage
    return stages.find((s) => (s.progress?.stars || 0) === 0) || stages[0];
  }, [stages]);

  const currentActiveStageIndex = useMemo(() => {
    if (!currentStage) return 0;
    const idx = stages.findIndex((s) => s.id === currentStage.id);
    return idx >= 0 ? idx : 0;
  }, [stages, currentStage]);

  const stats: JourneyStats = useMemo(() => {
    const totalStars = stages.reduce((acc, s) => acc + (s.progress?.stars || 0), 0);
    const maxStars = stages.length * 3;
    const completedStagesCount = stages.filter((s) => (s.progress?.stars || 0) > 0).length;
    const progressPercentage = stages.length > 0 ? completedStagesCount / stages.length : 0;

    return {
      totalStars,
      maxStars,
      completedStagesCount,
      totalStages: stages.length,
      progressPercentage,
      currentTier,
    };
  }, [stages, currentTier]);

  const phaseGroups: PhaseGroup[] = useMemo(() => {
    if (!stages.length) return [];

    const map = new Map<number, Stage[]>();
    for (const stage of stages) {
      const list = map.get(stage.phaseNumber) || [];
      list.push(stage);
      map.set(stage.phaseNumber, list);
    }

    const groups: PhaseGroup[] = [];
    map.forEach((phaseStages, phaseNum) => {
      const completedCount = phaseStages.filter((s) => (s.progress?.stars || 0) > 0).length;
      const totalCount = phaseStages.length;
      const first = phaseStages[0];
      groups.push({
        phaseNumber: phaseNum,
        phaseTitleFa: first?.phaseTitleFa || `فاز ${phaseNum}`,
        stages: phaseStages,
        completedStages: completedCount,
        totalStages: totalCount,
        isCompleted: completedCount === totalCount && totalCount > 0,
        progressPercentage: totalCount > 0 ? completedCount / totalCount : 0,
      });
    });

    return groups.sort((a, b) => a.phaseNumber - b.phaseNumber);
  }, [stages]);

  const seasonTitleFa = useMemo(() => {
    return stages[0]?.seasonTitleFa || 'فصل اول: روزهای آغازین در لندن';
  }, [stages]);

  // Mark local progress for guest demo/testing
  const recordGuestCompletion = useCallback((stageId: string, stars: number = 3, score: number = 95) => {
    const progress = {
      stars,
      bestScore: score,
      repeatCount: 1,
      completedAt: new Date().toISOString(),
    };
    saveLocalGuestProgress(stageId, progress);
    setStages((prev) =>
      prev.map((s) => (s.id === stageId ? { ...s, progress } : s))
    );
  }, []);

  return {
    stages,
    currentStage,
    currentActiveStageIndex,
    stats,
    phaseGroups,
    seasonTitleFa,
    isLoading,
    error,
    selectedStage,
    setSelectedStage,
    refreshStages: fetchStages,
    recordGuestCompletion,
  };
}
