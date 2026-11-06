import { api } from './client';

export interface SubscriptionPlan {
  id: string;
  type: string;
  titleFa: string;
  durationDays: number;
  priceTomans: number;
  discountPercent?: number;
  badge?: string | null;
}

export interface SubscriptionStatus {
  isSubscriber: boolean;
  planType?: string | null;
  expiresAt?: string | null;
  remainingDays: number;
}

export interface ValidatePromoRequest {
  promoCode: string;
  planId?: string;
  market?: string;
}

export interface ValidatePromoResponse {
  promoCode: string;
  discountPercent: number;
  discountType?: string;
  discountAmountTomans?: number;
  originalPriceTomans?: number;
  finalPriceTomans?: number;
  bonusDays?: number;
  userMessage?: string | null;
  message?: string;
}

export interface DirectCheckoutRequest {
  planId: string;
  promoCode?: string | null;
}

export interface DirectCheckoutResponse {
  paymentUrl: string;
  transactionId?: string;
  amountTomans?: number;
}

export const subscriptionApi = {
  /**
   * Fetches the list of active subscription plans.
   */
  async getPlans(): Promise<SubscriptionPlan[]> {
    try {
      const response = await api.get<SubscriptionPlan[]>('/v2/subscriptions/plans');
      if (Array.isArray(response) && response.length > 0) {
        return response;
      }
      return defaultFallbackPlans;
    } catch (err) {
      console.warn('Failed to fetch subscription plans from server, using fallback', err);
      return defaultFallbackPlans;
    }
  },

  /**
   * Fetches the current user's subscription status.
   */
  async getStatus(): Promise<SubscriptionStatus> {
    try {
      return await api.get<SubscriptionStatus>('/v2/subscriptions/status');
    } catch {
      return {
        isSubscriber: false,
        remainingDays: 0,
      };
    }
  },

  /**
   * Validates a promotional discount code.
   */
  async validatePromo(request: ValidatePromoRequest): Promise<ValidatePromoResponse> {
    return api.post<ValidatePromoResponse>('/v2/subscriptions/validate-promo', request);
  },

  /**
   * Creates a direct gateway payment URL.
   */
  async createDirectCheckout(request: DirectCheckoutRequest): Promise<DirectCheckoutResponse> {
    return api.post<DirectCheckoutResponse>('/v2/subscriptions/direct-checkout', request);
  },
};

export const defaultFallbackPlans: SubscriptionPlan[] = [
  {
    id: 'monthly_plan',
    type: 'monthly',
    titleFa: 'اشتراک ۱ ماهه',
    durationDays: 30,
    priceTomans: 189000,
    discountPercent: 30,
    badge: 'طرح مقدماتی',
  },
  {
    id: 'quarterly_plan',
    type: 'quarterly',
    titleFa: 'اشتراک ۳ ماهه (پیشنهادی)',
    durationDays: 90,
    priceTomans: 390000,
    discountPercent: 45,
    badge: 'محبوب‌ترین',
  },
  {
    id: 'annual_plan',
    type: 'annual',
    titleFa: 'اشتراک ۱ ساله طلایی',
    durationDays: 365,
    priceTomans: 790000,
    discountPercent: 60,
    badge: 'بهترین قیمت',
  },
];
