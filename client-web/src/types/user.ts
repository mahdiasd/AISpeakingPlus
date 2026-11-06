export interface SubscriptionSummary {
  isSubscriber: boolean;
  planType?: string | null;
  planTitleFa?: string | null;
  startedAt?: string | null;
  expiresAt?: string | null;
  remainingDays: number;
}

export interface UserProfile {
  id: string;
  phoneNumber: string;
  nickName?: string;
  firstName?: string | null;
  lastName?: string | null;
  avatar: string;
  score: number;
  totalStars: number;
  completedStagesCount: number;
  languageLevel?: string;
  subscription: SubscriptionSummary;
}

export interface UpdateProfileRequest {
  nickName?: string;
  avatar?: string;
  firstName?: string;
  lastName?: string;
  languageLevel?: string;
}

export interface AuthUserSummary {
  id: string;
  phoneNumber: string;
  nickName: string;
  avatar: string;
  score: number;
}

export interface AuthResponse {
  token: string;
  user: AuthUserSummary;
}
