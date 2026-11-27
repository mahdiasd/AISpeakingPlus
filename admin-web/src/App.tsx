import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { AppHomePage } from './pages/AppHomePage';

// Lazy-load admin routes so landing page visitors only download landing bundle
const AdminLayout = lazy(() =>
  import('./components/layout/AdminLayout').then((m) => ({ default: m.AdminLayout }))
);
const LoginPage = lazy(() =>
  import('./pages/LoginPage').then((m) => ({ default: m.LoginPage }))
);
const DashboardPage = lazy(() =>
  import('./pages/DashboardPage').then((m) => ({ default: m.DashboardPage }))
);
const StagesListPage = lazy(() =>
  import('./pages/StagesListPage').then((m) => ({ default: m.StagesListPage }))
);
const StageEditorPage = lazy(() =>
  import('./pages/StageEditorPage').then((m) => ({ default: m.StageEditorPage }))
);
const UsersPage = lazy(() =>
  import('./pages/UsersPage').then((m) => ({ default: m.UsersPage }))
);
const PromoCodesPage = lazy(() =>
  import('./pages/PromoCodesPage').then((m) => ({ default: m.PromoCodesPage }))
);
const AuditLogsPage = lazy(() =>
  import('./pages/AuditLogsPage').then((m) => ({ default: m.AuditLogsPage }))
);
const VoiceLabPage = lazy(() =>
  import('./pages/VoiceLabPage').then((m) => ({ default: m.VoiceLabPage }))
);
const NotificationsPage = lazy(() =>
  import('./pages/NotificationsPage').then((m) => ({ default: m.NotificationsPage }))
);
const AnnouncementsPage = lazy(() =>
  import('./pages/AnnouncementsPage').then((m) => ({ default: m.AnnouncementsPage }))
);
const AppVersionConfigPage = lazy(() =>
  import('./pages/AppVersionConfigPage').then((m) => ({ default: m.AppVersionConfigPage }))
);

const PageFallback: React.FC = () => (
  <div className="min-h-screen bg-[#0B0F17] flex items-center justify-center">
    <div className="w-8 h-8 rounded-full border-2 border-cyan-400 border-t-transparent animate-spin" />
  </div>
);

export const App: React.FC = () => {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Suspense fallback={<PageFallback />}>
              <Routes>
                {/* Main Application Route (Root) */}
                <Route path="/" element={<AppHomePage />} />

                {/* Admin Auth Route */}
                <Route path="/admin/login" element={<LoginPage />} />

                {/* Redirect /login to /admin/login */}
                <Route path="/login" element={<Navigate to="/admin/login" replace />} />

                {/* Protected Admin Routes Under /admin */}
                <Route path="/admin" element={<AdminLayout />}>
                  <Route index element={<Navigate to="/admin/dashboard" replace />} />
                  <Route path="dashboard" element={<DashboardPage />} />
                  <Route path="stages" element={<StagesListPage />} />
                  <Route path="stages/new" element={<StageEditorPage />} />
                  <Route path="stages/:id" element={<StageEditorPage />} />
                  <Route path="voice-lab" element={<VoiceLabPage />} />
                  <Route path="announcements" element={<AnnouncementsPage />} />
                  <Route path="app-version" element={<AppVersionConfigPage />} />
                  <Route path="notifications" element={<NotificationsPage />} />
                  <Route path="users" element={<UsersPage />} />
                  <Route path="promo-codes" element={<PromoCodesPage />} />
                  <Route path="audit-logs" element={<AuditLogsPage />} />
                </Route>

                {/* Legacy redirect /dashboard -> /admin/dashboard */}
                <Route path="/dashboard" element={<Navigate to="/admin/dashboard" replace />} />
                <Route path="/stages" element={<Navigate to="/admin/stages" replace />} />
                <Route path="/voice-lab" element={<Navigate to="/admin/voice-lab" replace />} />
                <Route path="/announcements" element={<Navigate to="/admin/announcements" replace />} />
                <Route path="/app-version" element={<Navigate to="/admin/app-version" replace />} />
                <Route path="/notifications" element={<Navigate to="/admin/notifications" replace />} />
                <Route path="/users" element={<Navigate to="/admin/users" replace />} />
                <Route path="/promo-codes" element={<Navigate to="/admin/promo-codes" replace />} />
                <Route path="/audit-logs" element={<Navigate to="/admin/audit-logs" replace />} />

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </Suspense>
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
};

export default App;
