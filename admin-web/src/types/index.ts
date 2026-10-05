// API Response Interfaces
export interface ApiResponse<T> {
  data: T;
  message?: string;
  pagingMeta?: PagingMeta | null;
}

export interface ApiFailure {
  errorCode: number;
  errorMessage: string;
}

export interface PagingMeta {
  totalPages: number;
  totalItems: number;
  page: number;
}

// Admin Auth
export interface AdminProfile {
  id: string;
  username: string;
  fullName: string;
  role: string;
}

export interface AdminLoginResponse {
  token: string;
  admin: AdminProfile;
}

// Stage Models
export type StageStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface Stage {
  id: string;
  orderIndex: number;
  title: string;
  titleFa: string;
  briefing: string;
  briefingFa: string;
  targetObjective: string;
  targetObjectiveFa: string;
  characterBehavior?: string | null;
  backgroundUrl: string;
  characterName: string;
  characterAvatarUrl?: string | null;
  characterGender: 'Woman' | 'Man' | string;
  voiceId?: string | null;
  initialSpeaker: 'Model' | 'User' | string;
  maxTurns: number;
  status: StageStatus;
  createdAt?: string;
}

export interface StageUpsertPayload {
  id: string;
  orderIndex: number;
  title: string;
  titleFa: string;
  briefing: string;
  briefingFa: string;
  targetObjective: string;
  targetObjectiveFa: string;
  characterBehavior?: string | null;
  backgroundUrl: string;
  characterName: string;
  characterAvatarUrl?: string | null;
  characterGender: string;
  voiceId?: string | null;
  initialSpeaker: string;
  maxTurns: number;
  status: StageStatus;
  shiftSubsequent?: boolean;
}

// Kokoro TTS Voice Model
export interface KokoroVoice {
  id: string;
  name: string;
  gender: 'Woman' | 'Man';
  accent: 'US' | 'UK';
  description: string;
}

export const KOKORO_VOICES: KokoroVoice[] = [
  { id: 'af_heart', name: 'Heart', gender: 'Woman', accent: 'US', description: 'صدای زن آمریکایی (صمیمی و گرم)' },
  { id: 'af_bella', name: 'Bella', gender: 'Woman', accent: 'US', description: 'صدای زن آمریکایی (واضح و پرانرژی)' },
  { id: 'af_nicole', name: 'Nicole', gender: 'Woman', accent: 'US', description: 'صدای زن آمریکایی (آرام و رسمی)' },
  { id: 'af_sarah', name: 'Sarah', gender: 'Woman', accent: 'US', description: 'صدای زن آمریکایی (حرفه‌ای و طبیعی)' },
  { id: 'af_sky', name: 'Sky', gender: 'Woman', accent: 'US', description: 'صدای زن آمریکایی (جوان و پویا)' },
  { id: 'am_adam', name: 'Adam', gender: 'Man', accent: 'US', description: 'صدای مرد آمریکایی (عمیق و باصلابت)' },
  { id: 'am_michael', name: 'Michael', gender: 'Man', accent: 'US', description: 'صدای مرد آمریکایی (دوستانه و استاندارد)' },
  { id: 'bf_emma', name: 'Emma', gender: 'Woman', accent: 'UK', description: 'صدای زن بریتانیایی (مؤدبانه و کریستالی)' },
  { id: 'bf_isabella', name: 'Isabella', gender: 'Woman', accent: 'UK', description: 'صدای زن بریتانیایی (آهسته و جذاب)' },
  { id: 'bm_george', name: 'George', gender: 'Man', accent: 'UK', description: 'صدای مرد بریتانیایی (کلاسیک و گوینده)' },
  { id: 'bm_lewis', name: 'Lewis', gender: 'Man', accent: 'UK', description: 'صدای مرد بریتانیایی (مدرن و صریح)' },
];

// User Models
export type UserStatus = 'ACTIVE' | 'SUSPENDED';

export interface UserItem {
  id: string;
  mobile: string;
  nickName: string;
  score: number;
  avatar: string;
  status: UserStatus;
  hasActiveSubscription: boolean;
  subscriptionExpiresAt?: string | null;
  createdAt: string;
}

export interface UserDetail {
  id: string;
  mobile: string;
  nickName: string;
  firstName?: string | null;
  lastName?: string | null;
  gender?: string | null;
  score: number;
  avatar: string;
  status: UserStatus;
  suspendedReason?: string | null;
  createdAt: string;
  updatedAt: string;
  completedStagesCount: number;
  activeSubscription?: SubscriptionItem | null;
}

export interface SubscriptionItem {
  id: string;
  planType: string;
  status: string;
  grantSource: string;
  grantedBy?: string | null;
  grantedByAdminName?: string | null;
  grantReason?: string | null;
  startedAt: string;
  expiresAt: string;
  createdAt: string;
}

// Dashboard Stats & Audit Log
export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  activeSubscriptions: number;
  totalStages: number;
  publishedStages: number;
}

export interface AuditLogItem {
  id: string;
  adminId?: string | null;
  adminName?: string | null;
  action: string;
  targetType: string;
  targetId: string;
  detailsJson: string;
  createdAt: string;
}

export interface MediaUploadResponse {
  url: string;
  filename: string;
}

// TTS & STT Testing Models
export interface TtsVoiceInfo {
  id: number;
  code: string;
  name: string;
  gender: 'FEMALE' | 'MALE' | 'Woman' | 'Man' | string;
  accent: 'AMERICAN' | 'BRITISH' | 'US' | 'UK' | string;
  language?: string;
  description: string;
}

export interface TtsSynthesizePayload {
  text: string;
  voiceId?: number;
  speed?: number;
}

export interface TtsSynthesizeResponse {
  audioUrl: string;
  durationMs: number;
  sampleRate: number;
  voiceId: number;
  voiceName: string;
}

export interface SttTranscribeResponse {
  text: string;
  durationMs: number;
}

export type SttWsMessage =
  | { type: 'ready'; message: string }
  | { type: 'partial'; text: string }
  | { type: 'final'; text: string }
  | { type: 'error'; message: string };

