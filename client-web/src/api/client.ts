/**
 * AI Speaking Plus - Web API Client
 * Compatible with Ktor Backend & Staging Environment
 */

export const BASE_API_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const TEST_ACCOUNT = {
  mobile: '09152413498',
  otp: '87799',
} as const;

export interface NetworkResponse<T> {
  data?: T;
  message?: string;
  errorMessage?: string | null;
  errorCode?: number | null;
}

export class ApiError extends Error {
  status: number;
  data: unknown;

  constructor(status: number, message: string, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export function getAuthToken(): string | null {
  try {
    return localStorage.getItem('token');
  } catch {
    return null;
  }
}

export function setAuthToken(token: string): void {
  try {
    localStorage.setItem('token', token);
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('auth-token-changed'));
    }
  } catch (err) {
    console.error('Failed to save auth token in localStorage', err);
  }
}

export function clearAuthToken(): void {
  try {
    localStorage.removeItem('token');
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('auth-token-changed'));
    }
  } catch (err) {
    console.error('Failed to clear auth token from localStorage', err);
  }
}

export async function request<T = unknown>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getAuthToken();
  const headers = new Headers(options.headers);

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const url = `${BASE_API_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorData: unknown;
    try {
      errorData = await response.json();
    } catch {
      errorData = await response.text();
    }
    throw new ApiError(response.status, `Request failed: ${response.statusText}`, errorData);
  }

  // 204 No Content
  if (response.status === 204) {
    return {} as T;
  }

  const json = await response.json();
  // If response matches Ktor's NetworkResponse structure { data: T }, unpack data
  if (json && typeof json === 'object' && 'data' in json && json.data !== undefined) {
    return json.data as T;
  }

  return json as T;
}

export const api = {
  get: <T = unknown>(url: string) => request<T>(url, { method: 'GET' }),
  post: <T = unknown>(url: string, body?: unknown) =>
    request<T>(url, {
      method: 'POST',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),
  put: <T = unknown>(url: string, body?: unknown) =>
    request<T>(url, {
      method: 'PUT',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),
  delete: <T = unknown>(url: string) => request<T>(url, { method: 'DELETE' }),
};
