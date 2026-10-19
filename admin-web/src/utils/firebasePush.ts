// Client-side Firebase Cloud Messaging utility for Admin Web & Browsers
// Handles Service Worker registration, token acquisition, and topic registration

declare global {
  interface Window {
    firebase?: any;
    aiSpeakingPushToken?: string;
  }
}

export const FIREBASE_WEB_CONFIG = {
  apiKey: "AIzaSyBixaaHlw2PcUHuFqFTni5R09fs4jECEn4",
  projectId: "aispeaking-132a3",
  messagingSenderId: "160657659274",
  storageBucket: "aispeaking-132a3.firebasestorage.app",
  appId: "1:160657659274:web:332012813a23198cfc6473",
  authDomain: "aispeaking-132a3.firebaseapp.com"
};

const FCM_TOKEN_STORAGE_KEY = 'fcm_web_token';

function loadScript(src: string): Promise<void> {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) {
      resolve();
      return;
    }
    const script = document.createElement('script');
    script.src = src;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = (e) => reject(new Error(`Failed to load script ${src}: ${e}`));
    document.head.appendChild(script);
  });
}

export async function ensureFirebaseLoaded(): Promise<any> {
  if (typeof window === 'undefined') return null;

  if (!window.firebase) {
    await loadScript('https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js');
    await loadScript('https://www.gstatic.com/firebasejs/10.14.1/firebase-messaging-compat.js');
  }

  if (window.firebase && !window.firebase.apps.length) {
    window.firebase.initializeApp(FIREBASE_WEB_CONFIG);
  }

  return window.firebase;
}

export function isNotificationSupported(): boolean {
  return typeof window !== 'undefined' && 'serviceWorker' in navigator && 'Notification' in window;
}

export function getStoredFcmToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(FCM_TOKEN_STORAGE_KEY);
}

export async function registerBrowserTokenWithServer(token: string, topic: string = 'aispeaking_plus_v1'): Promise<boolean> {
  try {
    const res = await fetch('/api/v1/notifications/register-token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, topic }),
    });
    return res.ok;
  } catch (err) {
    console.warn('[FCM] Failed to register token with backend server:', err);
    return false;
  }
}

export async function setupBrowserNotifications(onMessageReceived?: (payload: any) => void): Promise<{
  supported: boolean;
  permission: NotificationPermission | 'unsupported';
  token: string | null;
  error?: string;
}> {
  if (!isNotificationSupported()) {
    return { supported: false, permission: 'unsupported', token: null, error: 'این مرورگر از نوتیفیکیشن پشتیبانی نمی‌کند' };
  }

  try {
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      return { supported: true, permission, token: null };
    }

    const swReg = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
    await navigator.serviceWorker.ready;

    const fb = await ensureFirebaseLoaded();
    if (!fb) {
      throw new Error('کتابخانه فایربیس لود نشد');
    }

    const messaging = fb.messaging();

    // Listen for foreground messages
    messaging.onMessage((payload: any) => {
      console.log('[FCM] Foreground notification received:', payload);
      if (onMessageReceived) {
        onMessageReceived(payload);
      }
      // Display local Notification if page is focused
      try {
        const title = payload.notification?.title || payload.data?.title || 'AI Speaking Plus';
        const body = payload.notification?.body || payload.data?.body || '';
        new Notification(title, {
          body,
          icon: payload.notification?.icon || payload.data?.icon || '/vite.svg',
          data: payload.data || {},
        });
      } catch (e) {
        console.warn('[FCM] Local notification banner error:', e);
      }
    });

    const token = await messaging.getToken({
      serviceWorkerRegistration: swReg,
    });

    if (token) {
      localStorage.setItem(FCM_TOKEN_STORAGE_KEY, token);
      window.aiSpeakingPushToken = token;
      // Auto register to topic on server
      await registerBrowserTokenWithServer(token, 'aispeaking_plus_v1');
      return { supported: true, permission: 'granted', token };
    } else {
      return { supported: true, permission: 'granted', token: null, error: 'توکن از فایربیس دریافت نشد' };
    }
  } catch (err: any) {
    console.error('[FCM] Setup error:', err);
    return {
      supported: true,
      permission: Notification.permission,
      token: null,
      error: err?.message || 'خطا در دریافت توکن نوتیفیکیشن مرورگر',
    };
  }
}
