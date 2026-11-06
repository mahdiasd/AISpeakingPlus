export type StageLockStatus =
  | 'UNLOCKED'
  | 'LOCKED_PREVIOUS_STAGE'
  | 'LOCKED_REGISTRATION'
  | 'LOCKED_SUBSCRIPTION';

export interface StageProgress {
  stars: number; // 0 to 3
  bestScore?: number;
  repeatCount?: number;
  completedAt?: string;
}

export interface Stage {
  id: string;
  orderIndex: number;
  seasonNumber: number;
  seasonTitleFa: string;
  phaseNumber: number;
  phaseTitleFa: string;
  title: string;
  titleFa: string;
  briefing?: string;
  briefingFa: string;
  targetObjective?: string;
  targetObjectiveFa?: string;
  backgroundUrl: string;
  characterName: string;
  characterAvatarUrl?: string | null;
  characterGender?: 'Woman' | 'Man' | string;
  initialSpeaker?: string;
  maxTurns?: number;
  lockStatus: StageLockStatus;
  progress?: StageProgress | null;
}

export interface PhaseGroup {
  phaseNumber: number;
  phaseTitleFa: string;
  stages: Stage[];
  completedStages: number;
  totalStages: number;
  isCompleted: boolean;
  progressPercentage: number;
}

export interface JourneyStats {
  totalStars: number;
  maxStars: number;
  completedStagesCount: number;
  totalStages: number;
  progressPercentage: number;
  currentTier: 'GUEST' | 'REGISTERED' | 'SUBSCRIBER';
}

export interface StageCatalogApiResponse {
  currentTier: 'GUEST' | 'REGISTERED' | 'SUBSCRIBER';
  stages: Stage[];
}
