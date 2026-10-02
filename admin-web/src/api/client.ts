import {
  AdminLoginResponse,
  AdminProfile,
  ApiResponse,
  AuditLogItem,
  DashboardStats,
  MediaUploadResponse,
  Stage,
  StageStatus,
  StageUpsertPayload,
  SubscriptionItem,
  UserDetail,
  UserItem,
  UserStatus,
} from '../types';

const BASE_URL = '';
const TOKEN_KEY = 'admin_token';
const ADMIN_KEY = 'admin_profile';

export class ApiError extends Error {
  errorCode: number;
  constructor(message: string, errorCode: number = 400) {
    super(message);
    this.name = 'ApiError';
    this.errorCode = errorCode;
  }
}

class ApiClient {
  private token: string | null = null;

  constructor() {
    this.token = localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string | null, admin?: AdminProfile) {
    this.token = token;
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }

    if (admin) {
      localStorage.setItem(ADMIN_KEY, JSON.stringify(admin));
    } else if (!token) {
      localStorage.removeItem(ADMIN_KEY);
    }
  }

  getToken(): string | null {
    return this.token || localStorage.getItem(TOKEN_KEY);
  }

  getSavedAdmin(): AdminProfile | null {
    const raw = localStorage.getItem(ADMIN_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  logout() {
    this.setToken(null);
    window.location.href = '/admin/login';
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    const headers: Record<string, string> = {
      ...(options.headers as Record<string, string>),
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      this.setToken(null);
      if (window.location.pathname !== '/admin/login') {
        window.location.href = '/admin/login';
      }
      throw new ApiError('نشست کاری شما منقضی شده است. لطفاً دوباره وارد شوید.', 401);
    }

    let json: any;
    try {
      json = await response.json();
    } catch (e) {
      if (!response.ok) {
        throw new ApiError(`خطای سرور: ${response.statusText}`, response.status);
      }
      return { data: null as unknown as T };
    }

    if (!response.ok) {
      const errorMsg = json.errorMessage || json.message || 'خطایی در پردازش درخواست رخ داد';
      throw new ApiError(errorMsg, json.errorCode || response.status);
    }

    return json as ApiResponse<T>;
  }

  // --- Auth APIs ---
  async login(username: string, password: string): Promise<AdminLoginResponse> {
    const res = await this.request<AdminLoginResponse>('/api/admin/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    this.setToken(res.data.token, res.data.admin);
    return res.data;
  }

  async getMe(): Promise<AdminProfile> {
    const res = await this.request<AdminProfile>('/api/admin/auth/me');
    if (res.data) {
      localStorage.setItem(ADMIN_KEY, JSON.stringify(res.data));
    }
    return res.data;
  }

  // --- Dashboard & Audit APIs ---
  async getDashboardStats(): Promise<DashboardStats> {
    const res = await this.request<DashboardStats>('/api/admin/dashboard/stats');
    return res.data;
  }

  async getAuditLogs(limit: number = 50): Promise<AuditLogItem[]> {
    const res = await this.request<AuditLogItem[]>(`/api/admin/audit-logs?limit=${limit}`);
    return res.data || [];
  }

  // --- Stages APIs ---
  async getStages(status?: StageStatus | 'ALL'): Promise<Stage[]> {
    const query = status && status !== 'ALL' ? `?status=${status}` : '';
    const res = await this.request<Stage[]>(`/api/admin/stages${query}`);
    return res.data || [];
  }

  async getStage(id: string): Promise<Stage> {
    const res = await this.request<Stage>(`/api/admin/stages/${encodeURIComponent(id)}`);
    return res.data;
  }

  async upsertStage(stage: StageUpsertPayload): Promise<Stage> {
    const res = await this.request<Stage>('/api/admin/stages', {
      method: 'POST',
      body: JSON.stringify(stage),
    });
    return res.data;
  }

  async deleteStage(id: string): Promise<void> {
    await this.request<void>(`/api/admin/stages/${encodeURIComponent(id)}`, {
      method: 'DELETE',
    });
  }

  async reorderStage(id: string, newOrderIndex: number, shiftSubsequent: boolean = true): Promise<void> {
    await this.request<void>(`/api/admin/stages/${encodeURIComponent(id)}/reorder`, {
      method: 'POST',
      body: JSON.stringify({ newOrderIndex, shiftSubsequent }),
    });
  }

  // --- Users APIs ---
  async getUsers(page: number = 1, limit: number = 20, search?: string, status?: string): Promise<{ users: UserItem[]; totalPages: number; totalItems: number }> {
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('limit', limit.toString());
    if (search && search.trim()) {
      params.set('search', search.trim());
    }
    if (status && status !== 'ALL') {
      params.set('status', status);
    }

    const res = await this.request<UserItem[]>(`/api/admin/users?${params.toString()}`);
    return {
      users: res.data || [],
      totalPages: res.pagingMeta?.totalPages || 1,
      totalItems: res.pagingMeta?.totalItems || 0,
    };
  }

  async getUser(userId: string): Promise<UserDetail> {
    const res = await this.request<UserDetail>(`/api/admin/users/${userId}`);
    return res.data;
  }

  async updateUserStatus(userId: string, status: UserStatus, reason?: string): Promise<{ userId: string; status: string }> {
    const res = await this.request<{ userId: string; status: string }>(`/api/admin/users/${userId}/status`, {
      method: 'POST',
      body: JSON.stringify({ status, reason }),
    });
    return res.data;
  }

  // --- Subscriptions APIs ---
  async getUserSubscriptions(userId: string): Promise<SubscriptionItem[]> {
    const res = await this.request<SubscriptionItem[]>(`/api/admin/users/${userId}/subscriptions`);
    return res.data || [];
  }

  async grantSubscription(userId: string, planType: string, durationDays: number, reason?: string): Promise<SubscriptionItem> {
    const res = await this.request<SubscriptionItem>(`/api/admin/users/${userId}/subscriptions/grant`, {
      method: 'POST',
      body: JSON.stringify({ planType, durationDays, reason }),
    });
    return res.data;
  }

  async cancelSubscription(subscriptionId: string, reason?: string): Promise<SubscriptionItem> {
    const res = await this.request<SubscriptionItem>(`/api/admin/subscriptions/${subscriptionId}/cancel`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    });
    return res.data;
  }

  // --- Media APIs ---
  async uploadMedia(file: File): Promise<MediaUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    const res = await this.request<MediaUploadResponse>('/api/admin/media/upload', {
      method: 'POST',
      body: formData,
    });
    return res.data;
  }
}

export const api = new ApiClient();
