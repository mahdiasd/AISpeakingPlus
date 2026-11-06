import { api } from './client';
import { Stage, StageCatalogApiResponse, StageProgress } from '../types/stage';

const LOCAL_GUEST_PROGRESS_KEY = 'aispeaking_guest_progress';

export function getLocalGuestProgress(): Record<string, StageProgress> {
  try {
    const raw = localStorage.getItem(LOCAL_GUEST_PROGRESS_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

export function saveLocalGuestProgress(stageId: string, progress: StageProgress): void {
  try {
    const current = getLocalGuestProgress();
    current[stageId] = progress;
    localStorage.setItem(LOCAL_GUEST_PROGRESS_KEY, JSON.stringify(current));
  } catch (err) {
    console.error('Failed to save guest progress in localStorage', err);
  }
}

export const stageApi = {
  /**
   * Fetches the full stages curriculum catalog.
   * Merges local guest progress for unauthenticated sessions.
   */
  async getStages(): Promise<StageCatalogApiResponse> {
    const response = await api.get<StageCatalogApiResponse>('/v2/stages');
    const localProgress = getLocalGuestProgress();

    // If there's local guest progress, merge it into unauthenticated stages
    if (response.currentTier === 'GUEST' && Object.keys(localProgress).length > 0) {
      const mergedStages = response.stages.map((stg) => {
        const local = localProgress[stg.id];
        if (local && (!stg.progress || local.stars > (stg.progress.stars || 0))) {
          return {
            ...stg,
            progress: local,
          };
        }
        return stg;
      });

      return {
        ...response,
        stages: mergedStages,
      };
    }

    return response;
  },

  /**
   * Retrieves single stage detail.
   */
  async getStageDetail(stageId: string): Promise<Stage> {
    return api.get<Stage>(`/v2/stages/${stageId}`);
  },
};
