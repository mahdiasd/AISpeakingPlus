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
  // US Female Voices (11)
  { id: 'af_heart', name: 'Heart (Default)', gender: 'Woman', accent: 'US', description: 'فوق‌العاده طبیعی، انسانی و پرفکت برای مکالمه (پیش‌فرض)' },
  { id: 'af_bella', name: 'Bella', gender: 'Woman', accent: 'US', description: 'گرم، صمیمی و دوستانه (رتبه کیفی برتر)' },
  { id: 'af_nicole', name: 'Nicole', gender: 'Woman', accent: 'US', description: 'پرانرژی، روشن و واضح' },
  { id: 'af_sarah', name: 'Sarah', gender: 'Woman', accent: 'US', description: 'شمرده، آرام و متین' },
  { id: 'af_sky', name: 'Sky', gender: 'Woman', accent: 'US', description: 'نرم، شفاف و ملایم' },
  { id: 'af_alloy', name: 'Alloy', gender: 'Woman', accent: 'US', description: 'شفاف، خنثی و بدون اغراق' },
  { id: 'af_aoede', name: 'Aoede', gender: 'Woman', accent: 'US', description: 'آهنگین و محاوره‌ای' },
  { id: 'af_jessica', name: 'Jessica', gender: 'Woman', accent: 'US', description: 'زنده، فعال و پویا' },
  { id: 'af_kore', name: 'Kore', gender: 'Woman', accent: 'US', description: 'متین، متمرکز و آرامش‌بخش' },
  { id: 'af_nova', name: 'Nova', gender: 'Woman', accent: 'US', description: 'درخشان و مدرن' },
  { id: 'af_river', name: 'River', gender: 'Woman', accent: 'US', description: 'آرام، رها و طبیعی' },

  // US Male Voices (9)
  { id: 'am_fenrir', name: 'Fenrir', gender: 'Man', accent: 'US', description: 'بسیار باکیفیت، بم و روایی (رتبه برتر ۵ ستاره)' },
  { id: 'am_onyx', name: 'Onyx', gender: 'Man', accent: 'US', description: 'بم، کاریزماتیک، قدرتمند با لحن سینمایی (محبوب‌ترین)' },
  { id: 'am_michael', name: 'Michael', gender: 'Man', accent: 'US', description: 'گرم، طبیعی، دوستانه با لحن بم ملایم (۵ ستاره)' },
  { id: 'am_puck', name: 'Puck', gender: 'Man', accent: 'US', description: 'رسا، پرانرژی، گیرا و پرحرارت (۵ ستاره)' },
  { id: 'am_eric', name: 'Eric', gender: 'Man', accent: 'US', description: 'مکالمه‌ای، طبیعی، روان و روزمره' },
  { id: 'am_liam', name: 'Liam', gender: 'Man', accent: 'US', description: 'جوان، صمیمی، دوستانه و شفاف' },
  { id: 'am_echo', name: 'Echo', gender: 'Man', accent: 'US', description: 'متعادل، مدرن و رسا' },
  { id: 'am_adam', name: 'Adam', gender: 'Man', accent: 'US', description: 'با اعتماد به نفس و شفاف' },
  { id: 'am_santa', name: 'Santa', gender: 'Man', accent: 'US', description: 'کاراکتر بم، مسن و مهربان' },

  // UK Female Voices (4)
  { id: 'bf_emma', name: 'Emma', gender: 'Woman', accent: 'UK', description: 'فصیح، شفاف، رسمی و حرفه‌ای' },
  { id: 'bf_isabella', name: 'Isabella', gender: 'Woman', accent: 'UK', description: 'دلنشین، ملایم و آرامش‌بخش' },
  { id: 'bf_alice', name: 'Alice', gender: 'Woman', accent: 'UK', description: 'متین، باوقار، کتابی و با سابقه' },
  { id: 'bf_lily', name: 'Lily', gender: 'Woman', accent: 'UK', description: 'شاداب، جوان، آهنگین و پرانرژی' },

  // UK Male Voices (4)
  { id: 'bm_fable', name: 'Fable', gender: 'Man', accent: 'UK', description: 'صدای راوی برجسته، لهجه اصیل بریتانیایی، بسیار پخته (۵ ستاره)' },
  { id: 'bm_george', name: 'George', gender: 'Man', accent: 'UK', description: 'کلاسیک، باوقار و جنتلمن انگلیسی (۵ ستاره)' },
  { id: 'bm_daniel', name: 'Daniel', gender: 'Man', accent: 'UK', description: 'فصیح، مقتدر، رسمی و شمرده با لهجه RP' },
  { id: 'bm_lewis', name: 'Lewis', gender: 'Man', accent: 'UK', description: 'مدرن، جوان، سریع و پرانرژی' },
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
  mimeType: string;
  sizeBytes: number;
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

// Promo / Discount Code Models
export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT';

export interface PromoCodeItem {
  id: string;
  code: string;
  title?: string | null;
  discountType: DiscountType;
  discountValue: number;
  maxDiscountCap?: number | null;
  minOrderAmount?: number | null;
  applicablePlans: string[];
  maxUses?: number | null;
  usedCount: number;
  isOncePerUser: boolean;
  startsAt?: string | null;
  expiresAt?: string | null;
  isActive: boolean;
  isExpired: boolean;
  createdAt: string;
}

export interface PromoCodeUpsertPayload {
  code: string;
  title?: string;
  discountType: DiscountType;
  discountValue: number;
  maxDiscountCap?: number | null;
  minOrderAmount?: number | null;
  applicablePlans?: string[];
  maxUses?: number | null;
  isOncePerUser?: boolean;
  startsAt?: string | null;
  expiresAt?: string | null;
  isActive?: boolean;
}

export interface PromoRedemptionItem {
  id: string;
  userId: string;
  phoneNumber: string;
  nickName?: string | null;
  planId: string;
  originalPrice: number;
  discountAmount: number;
  finalPrice: number;
  redeemedAt: string;
}

export interface PromoStats {
  totalCodes: number;
  activeCodes: number;
  totalRedemptions: number;
  totalDiscountGiven: number;
}

export interface PromoListResponse {
  items: PromoCodeItem[];
  stats: PromoStats;
}

// Push Notification Models
export interface AdminNotificationPayload {
  title: string;
  body: string;
  imageUrl?: string;
  targetType?: 'topic' | 'token';
  targetValue?: string;
  screen?: string;
  stageId?: string;
  url?: string;
}

export interface AdminNotificationResponse {
  success: boolean;
  messageId: string;
  target: string;
  targetType: string;
}

// In-App Announcements Models
export type AnnouncementType = 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS';
export type AnnouncementActionType = 'CLOSE' | 'EXIT_APP' | 'OPEN_URL' | 'NAVIGATE' | 'RELOAD';

export interface AnnouncementItem {
  id: string;
  title: string;
  message: string;
  type: AnnouncementType;
  imageUrl?: string | null;
  isBlocking: boolean;
  isActive: boolean;
  primaryButtonText: string;
  primaryButtonAction: AnnouncementActionType;
  primaryButtonValue?: string | null;
  secondaryButtonText?: string | null;
  secondaryButtonAction?: AnnouncementActionType | null;
  secondaryButtonValue?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AnnouncementUpsertPayload {
  title: string;
  message: string;
  type: AnnouncementType;
  imageUrl?: string | null;
  isBlocking: boolean;
  isActive: boolean;
  primaryButtonText: string;
  primaryButtonAction: AnnouncementActionType;
  primaryButtonValue?: string | null;
  secondaryButtonText?: string | null;
  secondaryButtonAction?: AnnouncementActionType | null;
  secondaryButtonValue?: string | null;
}

export interface AnnouncementListResponse {
  announcements: AnnouncementItem[];
  activeAnnouncementId?: string | null;
}


