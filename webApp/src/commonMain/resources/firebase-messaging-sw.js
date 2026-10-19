// Firebase Cloud Messaging Service Worker for AI Speaking Plus
// Handles background push notifications and deep linking navigation

importScripts('https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.14.1/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyBixaaHlw2PcUHuFqFTni5R09fs4jECEn4",
  projectId: "aispeaking-132a3",
  messagingSenderId: "160657659274",
  storageBucket: "aispeaking-132a3.firebasestorage.app",
  appId: "1:160657659274:web:332012813a23198cfc6473",
  authDomain: "aispeaking-132a3.firebaseapp.com"
});

const messaging = firebase.messaging();

self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim());
});

// Background message handler
messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Received background message:', payload);
  const notificationTitle = payload.notification?.title || payload.data?.title || 'AI Speaking Plus';
  const notificationOptions = {
    body: payload.notification?.body || payload.data?.body || payload.data?.message || '',
    icon: payload.notification?.icon || payload.data?.icon || '/android-chrome-192x192.png',
    badge: '/favicon-16x16.png',
    data: payload.data || {}
  };

  return self.registration.showNotification(notificationTitle, notificationOptions);
});

// Notification click event with deep linking support
self.addEventListener('notificationclick', function(event) {
  console.log('[firebase-messaging-sw.js] Notification clicked:', event.notification);
  event.notification.close();

  const data = event.notification.data || {};
  const screen = (data.screen || '').toLowerCase().trim();

  // If screen is 'none' or no link/destination provided, simply focus existing window
  if (!screen || screen === 'none') {
    if (!data.url && !data.stage_id && !data.stageId) {
      event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function(clientList) {
          for (let i = 0; i < clientList.length; i++) {
            const client = clientList[i];
            if (client.url.startsWith(self.location.origin) && 'focus' in client) {
              return client.focus();
            }
          }
          if (clients.openWindow) {
            return clients.openWindow('/#main');
          }
        })
      );
      return;
    }
  }

  let targetPath = '/#main';

  if (data.url && typeof data.url === 'string' && data.url.trim().length > 0) {
    targetPath = data.url.trim();
  } else if (data.stage_id || data.stageId || data.stage) {
    const stageId = data.stage_id || data.stageId || data.stage;
    targetPath = '/#chat?stageId=' + encodeURIComponent(stageId);
  } else if (screen) {
    if (screen === 'profile') targetPath = '/#profile';
    else if (screen === 'subscription' || screen === 'sub' || screen === 'vip') targetPath = '/#subscription';
    else if (screen === 'stages' || screen === 'stages_list') targetPath = '/#stages';
    else if (screen === 'login' || screen === 'auth') targetPath = '/#login';
    else if (screen === 'chat') {
      const stageId = data.stage_id || data.stageId || 'stage-01-inflight-london';
      targetPath = '/#chat?stageId=' + encodeURIComponent(stageId);
    } else {
      targetPath = '/#main';
    }
  }

  const fullUrl = new URL(targetPath, self.location.origin).href;

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function(clientList) {
      for (let i = 0; i < clientList.length; i++) {
        const client = clientList[i];
        if (client.url.startsWith(self.location.origin) && 'focus' in client) {
          client.focus();
          if ('navigate' in client) {
            return client.navigate(fullUrl);
          } else {
            client.postMessage({ type: 'FCM_DEEP_LINK', url: fullUrl, data: data });
            return;
          }
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(fullUrl);
      }
    })
  );
});
