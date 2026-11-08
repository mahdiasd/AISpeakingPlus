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
    priceTomans: 199000,
    discountPercent: 0,
    badge: undefined,
  },
  {
    id: 'quarterly_plan',
    type: 'quarterly',
    titleFa: 'اشتراک ۳ ماهه ویژه',
    durationDays: 90,
    priceTomans: 499000,
    discountPercent: 15,
    badge: 'محبوب‌ترین',
  },
  {
    id: 'semi_annual_plan',
    type: 'semi_annual',
    titleFa: 'اشتراک ۶ ماهه طلایی',
    durationDays: 180,
    priceTomans: 899000,
    discountPercent: 25,
    badge: 'بهترین ارزش',
  },
];
